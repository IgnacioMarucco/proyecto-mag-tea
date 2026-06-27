package com.utn.magtea.inicio;

import com.utn.magtea.common.DomainConstants;
import com.utn.magtea.formulariointeres.EstadoFormulario;
import com.utn.magtea.formulariointeres.FormularioInteresRepository;
import com.utn.magtea.modeloanimal.ModeloAnimal;
import com.utn.magtea.modeloanimal.ModeloAnimalPoolAporteRepository;
import com.utn.magtea.modeloanimal.ModeloAnimalRepository;
import com.utn.magtea.paciente.PacienteEstado;
import com.utn.magtea.paciente.PacienteRepository;
import com.utn.magtea.pool.PoolRepository;
import com.utn.magtea.profesional.Profesional;
import com.utn.magtea.profesional.ProfesionalRepository;
import com.utn.magtea.suero.SueroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InicioService {

    private static final DayOfWeek INICIO_SEMANA = DayOfWeek.SUNDAY;
    private static final int DIAS_INOCULACION    = 4;
    private static final int MAX_ACTIVIDAD       = 7;

    private static final Set<String> ROLES_CLINICOS = Set.of("CUERPO_MEDICO", "INVESTIGADOR_PRINCIPAL");
    private static final Set<String> ROLES_TECNICOS = Set.of("CUERPO_TECNICO", "INVESTIGADOR_PRINCIPAL");

    private final FormularioInteresRepository      formularioRepo;
    private final PacienteRepository               pacienteRepo;
    private final ModeloAnimalRepository           modeloAnimalRepo;
    private final ModeloAnimalPoolAporteRepository aporteRepo;
    private final SueroRepository                  sueroRepo;
    private final PoolRepository                   poolRepo;
    private final ProfesionalRepository            profesionalRepo;
    private final Clock                            clock;

    @Transactional(readOnly = true)
    public InicioResponseDTO getInicio(String role) {
        LocalDate hoy = LocalDate.now(clock);

        Integer formulariosPendientes = ROLES_CLINICOS.contains(role) ? contarFormulariosPendientes() : null;
        List<AgendaEventoDTO>           agenda        = buildAgendaSemana(role, hoy);
        List<InoculacionSemanalItemDTO> inoculaciones = ROLES_TECNICOS.contains(role) ? buildAgendaInoculacion(hoy) : null;
        List<AlertaConductualItemDTO>   alertas       = ROLES_TECNICOS.contains(role) ? buildAlertasConductuales(hoy) : null;
        List<ActividadRecienteItemDTO>  actividad     = "INVESTIGADOR_PRINCIPAL".equals(role) ? buildActividadReciente() : null;

        return new InicioResponseDTO(formulariosPendientes, agenda, inoculaciones, alertas, actividad);
    }

    // ── Formularios ─────────────────────────────────────────────────────────────

    private int contarFormulariosPendientes() {
        return (int) formularioRepo.countByEstadoAndActivoTrue(EstadoFormulario.PENDIENTE);
    }

    // ── Agenda semanal ──────────────────────────────────────────────────────────

    private List<AgendaEventoDTO> buildAgendaSemana(String role, LocalDate hoy) {
        LocalDate inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(INICIO_SEMANA));
        LocalDate finSemana    = inicioSemana.plusDays(13);

        List<AgendaEventoDTO> eventos = new ArrayList<>();

        if (ROLES_CLINICOS.contains(role)) {
            LocalDateTime desde = inicioSemana.atStartOfDay();
            LocalDateTime hasta = finSemana.atTime(23, 59, 59);

            pacienteRepo.findByFechaPrimeraVisitaBetweenAndActivoTrue(desde, hasta)
                    .forEach(p -> eventos.add(new AgendaEventoDTO(
                            p.getFechaPrimeraVisita().toLocalDate(),
                            "PRIMERA_VISITA",
                            p.getCodigoNumerico(),
                            p.getId(),
                            "pacientes")));

            pacienteRepo.findByFechaTurnoExtraccionBetweenAndActivoTrue(desde, hasta)
                    .forEach(p -> eventos.add(new AgendaEventoDTO(
                            p.getFechaTurnoExtraccion().toLocalDate(),
                            "EXTRACCION",
                            p.getCodigoNumerico(),
                            p.getId(),
                            "pacientes")));
        }

        if (ROLES_TECNICOS.contains(role)) {
            // Inoculaciones: un evento por día × animal
            modeloAnimalRepo.findForAgendaInoculacion(
                            inicioSemana.minusDays(DIAS_INOCULACION - 1), finSemana)
                    .forEach(ma -> {
                        LocalDate dia1 = ma.getFechaDia1Inoculacion();
                        Set<Integer> hechos = new HashSet<>();
                        ma.getAportes().forEach(a -> hechos.add(a.getDia()));
                        for (int d = 1; d <= DIAS_INOCULACION; d++) {
                            LocalDate fechaDia = dia1.plusDays(d - 1);
                            if (!fechaDia.isBefore(inicioSemana) && !fechaDia.isAfter(finSemana) && !hechos.contains(d)) {
                                eventos.add(new AgendaEventoDTO(fechaDia, "INOCULACION",
                                        ma.getIdentificador(), ma.getId(), "modelos-animales"));
                            }
                        }
                    });

            // Vocalizaciones: animales cuyo día 7 cae en la semana y aún no se registró
            modeloAnimalRepo.findByCamadaFechaNacimientoBetween(
                            inicioSemana.minusDays(DomainConstants.DIA_VOCALIZACIONES),
                            finSemana.minusDays(DomainConstants.DIA_VOCALIZACIONES))
                    .stream()
                    .filter(ma -> ma.getVocalizaciones() == null)
                    .forEach(ma -> eventos.add(new AgendaEventoDTO(
                            ma.getCamada().getFechaNacimiento().plusDays(DomainConstants.DIA_VOCALIZACIONES),
                            "VOCALIZACIONES", ma.getIdentificador(), ma.getId(), "modelos-animales")));

            // Tres cámaras + Microscopía: animales cuyo día 21 cae en la semana y aún no se registró
            modeloAnimalRepo.findByCamadaFechaNacimientoBetween(
                            inicioSemana.minusDays(DomainConstants.DIA_TRES_CAMARAS),
                            finSemana.minusDays(DomainConstants.DIA_TRES_CAMARAS))
                    .stream()
                    .filter(ma -> ma.getTresCamaras() == null)
                    .forEach(ma -> {
                        LocalDate fechaTest = ma.getCamada().getFechaNacimiento().plusDays(DomainConstants.DIA_TRES_CAMARAS);
                        eventos.add(new AgendaEventoDTO(fechaTest, "TRES_CAMARAS",
                                ma.getIdentificador(), ma.getId(), "modelos-animales"));
                        eventos.add(new AgendaEventoDTO(fechaTest, "MICROSCOPIA",
                                ma.getIdentificador(), ma.getId(), "modelos-animales"));
                    });
        }

        return eventos;
    }

    // ── Agenda de inoculaciones (sección secundaria) ────────────────────────────

    private List<InoculacionSemanalItemDTO> buildAgendaInoculacion(LocalDate hoy) {
        LocalDate inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(INICIO_SEMANA));
        LocalDate finSemana    = inicioSemana.plusDays(6);
        LocalDate desde        = inicioSemana.minusDays(DIAS_INOCULACION - 1);

        return modeloAnimalRepo.findForAgendaInoculacion(desde, finSemana).stream()
                .map(ma -> {
                    LocalDate dia1 = ma.getFechaDia1Inoculacion();
                    Set<Integer> diasHechosSet = new HashSet<>();
                    ma.getAportes().forEach(a -> diasHechosSet.add(a.getDia()));

                    List<Integer> diasPendientes = new ArrayList<>();
                    for (int d = 1; d <= DIAS_INOCULACION; d++) {
                        LocalDate fechaDia = dia1.plusDays(d - 1);
                        if (!diasHechosSet.contains(d)
                                && !fechaDia.isBefore(inicioSemana)
                                && !fechaDia.isAfter(finSemana)) {
                            diasPendientes.add(d);
                        }
                    }
                    if (diasPendientes.isEmpty()) return null;

                    List<Integer> diasHechos = new ArrayList<>(diasHechosSet);

                    return new InoculacionSemanalItemDTO(
                            ma.getId(), ma.getIdentificador(),
                            ma.getCamada().getNombre(), ma.getPool().getCodigo(),
                            dia1, diasPendientes, diasHechos);
                })
                .filter(item -> item != null)
                .toList();
    }

    // ── Alertas conductuales ────────────────────────────────────────────────────

    private List<AlertaConductualItemDTO> buildAlertasConductuales(LocalDate hoy) {
        LocalDate umbralVocalizaciones = hoy.minusDays(DomainConstants.DIA_VOCALIZACIONES - DomainConstants.VENTANA_ALERTA_DIAS);
        LocalDate umbralTresCamaras    = hoy.minusDays(DomainConstants.DIA_TRES_CAMARAS - DomainConstants.VENTANA_ALERTA_DIAS);

        List<AlertaConductualItemDTO> alertas = new ArrayList<>();

        modeloAnimalRepo.findForAlertasConductuales(umbralVocalizaciones, umbralTresCamaras)
                .forEach(ma -> {
                    LocalDate fn = ma.getCamada().getFechaNacimiento();

                    if (ma.getVocalizaciones() == null && !fn.isAfter(umbralVocalizaciones)) {
                        LocalDate fechaTest = fn.plusDays(DomainConstants.DIA_VOCALIZACIONES);
                        alertas.add(new AlertaConductualItemDTO(
                                ma.getId(), ma.getIdentificador(), ma.getCamada().getNombre(),
                                "VOCALIZACIONES", fechaTest,
                                (int) ChronoUnit.DAYS.between(hoy, fechaTest)));
                    }

                    if (ma.getTresCamaras() == null && !fn.isAfter(umbralTresCamaras)) {
                        LocalDate fechaTest = fn.plusDays(DomainConstants.DIA_TRES_CAMARAS);
                        alertas.add(new AlertaConductualItemDTO(
                                ma.getId(), ma.getIdentificador(), ma.getCamada().getNombre(),
                                "TRES_CAMARAS", fechaTest,
                                (int) ChronoUnit.DAYS.between(hoy, fechaTest)));
                        alertas.add(new AlertaConductualItemDTO(
                                ma.getId(), ma.getIdentificador(), ma.getCamada().getNombre(),
                                "SACRIFICIO", fechaTest,
                                (int) ChronoUnit.DAYS.between(hoy, fechaTest)));
                    }
                });

        alertas.sort(Comparator.comparingInt(AlertaConductualItemDTO::diasRestantes));
        return alertas;
    }

    // ── Actividad reciente ──────────────────────────────────────────────────────

    private record ActividadRaw(
            String tipo, String descripcion, LocalDateTime fecha,
            Long entityId, String identificador, String entityPath, String createdBy) {}

    private List<ActividadRecienteItemDTO> buildActividadReciente() {
        List<ActividadRaw> raws = new ArrayList<>();

        pacienteRepo.findTop3ByActivoTrueOrderByCreatedAtDesc().forEach(p ->
                raws.add(new ActividadRaw("PACIENTE", "admitido",
                        p.getCreatedAt(), p.getId(), p.getCodigoNumerico(), "pacientes", p.getCreatedBy())));

        pacienteRepo.findTop3ByActivoTrueAndEstadoClinicoOrderByUpdatedAtDesc(
                        PacienteEstado.MCHAT_RESPONDIDO).forEach(p ->
                raws.add(new ActividadRaw("MCHAT", "M-CHAT recibido",
                        p.getUpdatedAt(), p.getId(), p.getCodigoNumerico(), "pacientes", null)));

        pacienteRepo.findTop3ByActivoTrueAndEstadoClinicoOrderByUpdatedAtDesc(
                        PacienteEstado.EXTRACCION_PENDIENTE).forEach(p ->
                raws.add(new ActividadRaw("PACIENTE", "extracción pendiente",
                        p.getUpdatedAt(), p.getId(), p.getCodigoNumerico(), "pacientes", p.getLastModifiedBy())));

        sueroRepo.findTop3ByActivoTrueOrderByCreatedAtDesc().forEach(s ->
                raws.add(new ActividadRaw("SUERO",
                        "registrado (rango " + s.getRango() + ")",
                        s.getCreatedAt(), s.getId(),
                        s.getPaciente().getCodigoNumerico(), "sueros", s.getCreatedBy())));

        poolRepo.findTop3ByActivoTrueOrderByCreatedAtDesc().forEach(p ->
                raws.add(new ActividadRaw("POOL",
                        "registrado",
                        p.getCreatedAt(), p.getId(),
                        p.getCodigo(), "pools", p.getCreatedBy())));

        modeloAnimalRepo.findTop3ByActivoTrueOrderByCreatedAtDesc().forEach(m ->
                raws.add(new ActividadRaw("MODELO_ANIMAL",
                        "registrado",
                        m.getCreatedAt(), m.getId(),
                        m.getIdentificador(), "modelos-animales", m.getCreatedBy())));

        aporteRepo.findTop3ForActividad().forEach(a ->
                raws.add(new ActividadRaw("INOCULACION",
                        "inoculación día " + a.getDia(),
                        a.getCreatedAt(), a.getModeloAnimal().getId(),
                        a.getModeloAnimal().getIdentificador(), "modelos-animales", a.getCreatedBy())));

        List<ActividadRaw> top = raws.stream()
                .filter(r -> r.fecha() != null)
                .sorted(Comparator.comparing(ActividadRaw::fecha).reversed())
                .limit(MAX_ACTIVIDAD)
                .toList();

        Set<String> emails = top.stream()
                .filter(r -> r.createdBy() != null)
                .map(ActividadRaw::createdBy)
                .collect(Collectors.toSet());

        Map<String, Profesional> profsByEmail = profesionalRepo.findByEmailIn(emails).stream()
                .collect(Collectors.toMap(Profesional::getEmail, p -> p));

        return top.stream().map(r -> {
            Profesional p = r.createdBy() != null ? profsByEmail.get(r.createdBy()) : null;
            String nombre = p != null ? p.getNombre() + " " + p.getApellido() : null;
            String rol    = p != null ? p.getRole().name() : null;
            return new ActividadRecienteItemDTO(r.tipo(), r.descripcion(), r.fecha(),
                    r.entityId(), r.identificador(), r.entityPath(), nombre, rol);
        }).toList();
    }
}
