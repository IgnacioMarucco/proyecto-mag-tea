package com.utn.magtea.paciente;

import com.utn.magtea.common.MailService;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.formulariointeres.FormularioInteres;
import com.utn.magtea.formulariointeres.FormularioInteresService;
import com.utn.magtea.paciente.cars.CarsDTO;
import com.utn.magtea.paciente.cars.EvaluacionCars;
import com.utn.magtea.paciente.criterios.Criterios;
import com.utn.magtea.paciente.criterios.CriteriosDTO;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatInfoDTO;
import com.utn.magtea.paciente.mchat.MchatResultadoFinal;
import com.utn.magtea.paciente.mchat.MchatScoringUtil;
import com.utn.magtea.paciente.mchat.MchatSeguimiento;
import com.utn.magtea.paciente.mchat.MchatSeguimientoDTO;
import com.utn.magtea.paciente.vineland.EvaluacionVineland;
import com.utn.magtea.paciente.vineland.VinelandDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PacienteService {

    private static final List<BigDecimal> CARS_VALORES_VALIDOS = List.of(
            new BigDecimal("1.0"), new BigDecimal("1.5"), new BigDecimal("2.0"),
            new BigDecimal("2.5"), new BigDecimal("3.0"), new BigDecimal("3.5"),
            new BigDecimal("4.0"));
    private static final Set<String>  SORT_FIELDS_VALIDOS  = Set.of("createdAt", "apellidoNino", "apellidoTutor", "nombreNino");

    private final PacienteRepository repository;
    private final PacienteMapper mapper;
    private final FormularioInteresService formularioService;
    private final MailService mailService;
    private final Clock clock;

    @Value("${app.mchat.token-expiry-days:30}")
    private int tokenExpiryDays;

    @Transactional(readOnly = true)
    public PageResponse<PacienteListDTO> findAll(int page, int size, String q,
                                                  List<PacienteEstado> estados,
                                                  List<TipoPaciente> tipos,
                                                  String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir, "createdAt");
        Page<PacienteListProjection> result = repository.findBy(
                buildSpec(q, estados, tipos),
                fq -> fq.as(PacienteListProjection.class).page(PageRequest.of(page, size, sort))
        );
        List<PacienteListDTO> content = result.getContent().stream()
                .map(p -> new PacienteListDTO(
                        p.getId(), p.getApellidoTutor(), p.getNombreTutor(),
                        p.getApellidoNino(), p.getNombreNino(), p.getFechaNacimientoNino(),
                        p.getTipoPaciente(), p.getEstadoClinico(),
                        p.getFechaPrimeraVisita(), p.getFechaExtraccion()
                ))
                .toList();
        return new PageResponse<>(content, result.getTotalElements(),
                result.getTotalPages(), result.getNumber(), result.getSize());
    }

    @Transactional(readOnly = true)
    public PacienteResponseDTO findById(Long id) {
        return mapper.toDTO(findActiveById(id));
    }

    @Transactional
    public PacienteResponseDTO create(PacienteCreateDTO dto) {
        Paciente paciente = mapper.toEntity(dto);
        paciente.setCodigoNumerico(generarCodigoNumerico());
        paciente.setFechaContacto(LocalDate.now());
        paciente.setFechaPrimeraVisita(dto.fechaPrimeraVisita());
        paciente.setNotas(dto.notas());
        paciente.setConsentimientoFirmado(dto.consentimientoFirmado());

        if (dto.formularioInteresId() != null) {
            FormularioInteres formulario = formularioService.admitir(dto.formularioInteresId());
            paciente.setFormularioInteresId(formulario.getId());
            paciente.setFechaContacto(formulario.getFechaContacto());
            paciente.setComoConocioProyecto(formulario.getComoConocioProyecto());
            paciente.setOtroComoConocio(formulario.getOtroComoConocio());
        }

        Criterios criterios = new Criterios();
        criterios.setPaciente(paciente);
        criterios.setCriterioTEADSMV(dto.criterioTEADSMV());
        criterios.setCriterioTGDDSMIV(dto.criterioTGDDSMIV());
        criterios.setCriterioEdad(dto.criterioEdad());
        criterios.setEpilepsia(dto.epilepsia());
        criterios.setParalisisCerebral(dto.paralisisCerebral());
        criterios.setInfeccionesCongenitas(dto.infeccionesCongenitas());
        criterios.setLesionesEstructuralesSNC(dto.lesionesEstructuralesSNC());
        criterios.setFacomatosis(dto.facomatosis());
        criterios.setPatologiasNeurometabolicas(dto.patologiasNeurometabolicas());
        criterios.setLesionesOcupantesEspacioSNC(dto.lesionesOcupantesEspacioSNC());
        criterios.setPatologiaPsiquiatrica(dto.patologiaPsiquiatrica());
        criterios.setOtrosSindromesGeneticos(dto.otrosSindromesGeneticos());
        criterios.setPubertadPrecoz(dto.pubertadPrecoz());
        paciente.setCriterios(criterios);

        paciente.setEstadoClinico(PacienteEstado.ADMITIDO);

        if (dto.tipoPaciente() == TipoPaciente.PROBLEMA) {
            paciente.setMchatToken(UUID.randomUUID().toString());
            paciente.setMchatTokenExpiry(LocalDateTime.now(clock).plusDays(tokenExpiryDays));
        }

        Paciente saved = repository.save(paciente);

        if (dto.tipoPaciente() == TipoPaciente.PROBLEMA) {
            try {
                mailService.enviarLinkMchat(
                        saved.getCorreoTutor(),
                        saved.getNombreTutor(),
                        saved.getApellidoNino(),
                        saved.getNombreNino(),
                        saved.getMchatToken()
                );
            } catch (Exception e) {
                log.warn("No se pudo enviar el mail M-CHAT para paciente {}: {}", saved.getId(), e.getMessage());
            }
        }

        return mapper.toDTO(saved);
    }

    @Transactional
    public PacienteResponseDTO update(Long id, PacienteUpdateDTO dto) {
        Paciente paciente = findActiveById(id);
        paciente.setApellidoTutor(dto.apellidoTutor());
        paciente.setNombreTutor(dto.nombreTutor());
        paciente.setCorreoTutor(dto.correoTutor());
        paciente.setTelefono(dto.telefono());
        paciente.setApellidoNino(dto.apellidoNino());
        paciente.setNombreNino(dto.nombreNino());
        paciente.setFechaNacimientoNino(dto.fechaNacimientoNino());
        paciente.setSexo(dto.sexo());
        paciente.setNotas(dto.notas());
        if (dto.fechaPrimeraVisita() != null) paciente.setFechaPrimeraVisita(dto.fechaPrimeraVisita());
        if (dto.fechaExtraccion() != null) paciente.setFechaExtraccion(dto.fechaExtraccion());
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updatePrimeraVisita(Long id, PacientePrimeraVisitaDTO dto) {
        Paciente paciente = findActiveById(id);
        paciente.setFechaPrimeraVisita(dto.fechaPrimeraVisita());
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updateConsentimiento(Long id, PacienteConsentimientoDTO dto) {
        Paciente paciente = findActiveById(id);
        paciente.setConsentimientoFirmado(dto.consentimientoFirmado());
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updateCriterios(Long id, CriteriosDTO dto) {
        Paciente paciente = findActiveById(id);
        Criterios criterios = Optional.ofNullable(paciente.getCriterios())
                .orElseGet(() -> { var c = new Criterios(); c.setPaciente(paciente); return c; });
        criterios.setCriterioTEADSMV(dto.criterioTEADSMV());
        criterios.setCriterioTGDDSMIV(dto.criterioTGDDSMIV());
        criterios.setCriterioEdad(dto.criterioEdad());
        criterios.setEpilepsia(dto.epilepsia());
        criterios.setParalisisCerebral(dto.paralisisCerebral());
        criterios.setInfeccionesCongenitas(dto.infeccionesCongenitas());
        criterios.setLesionesEstructuralesSNC(dto.lesionesEstructuralesSNC());
        criterios.setFacomatosis(dto.facomatosis());
        criterios.setPatologiasNeurometabolicas(dto.patologiasNeurometabolicas());
        criterios.setLesionesOcupantesEspacioSNC(dto.lesionesOcupantesEspacioSNC());
        criterios.setPatologiaPsiquiatrica(dto.patologiaPsiquiatrica());
        criterios.setOtrosSindromesGeneticos(dto.otrosSindromesGeneticos());
        criterios.setPubertadPrecoz(dto.pubertadPrecoz());
        paciente.setCriterios(criterios);
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updateMchatSeguimiento(Long id, MchatSeguimientoDTO dto) {
        Paciente paciente = findActiveById(id);

        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL) {
            throw new BusinessRuleException(
                    "El seguimiento M-CHAT no aplica para pacientes caso control");
        }
        MchatFamilia familia = paciente.getMchatFamilia();
        if (familia == null || familia.getScoreTotal() < 3 || familia.getScoreTotal() > 7) {
            throw new BusinessRuleException(
                    "El seguimiento M-CHAT solo aplica cuando el score está entre 3 y 7");
        }

        MchatSeguimiento seg = Optional.ofNullable(paciente.getMchatSeguimiento())
                .orElseGet(() -> { var s = new MchatSeguimiento(); s.setPaciente(paciente); return s; });
        seg.setItem1(dto.item1());   seg.setItem2(dto.item2());   seg.setItem3(dto.item3());
        seg.setItem4(dto.item4());   seg.setItem5(dto.item5());   seg.setItem6(dto.item6());
        seg.setItem7(dto.item7());   seg.setItem8(dto.item8());   seg.setItem9(dto.item9());
        seg.setItem10(dto.item10()); seg.setItem11(dto.item11()); seg.setItem12(dto.item12());
        seg.setItem13(dto.item13()); seg.setItem14(dto.item14()); seg.setItem15(dto.item15());
        seg.setItem16(dto.item16()); seg.setItem17(dto.item17()); seg.setItem18(dto.item18());
        seg.setItem19(dto.item19()); seg.setItem20(dto.item20());

        int fallas = contarFallas(dto);
        seg.setFallas(fallas);
        paciente.setMchatSeguimiento(seg);
        familia.setResultadoFinal(fallas <= 1
                ? MchatResultadoFinal.NEGATIVA
                : MchatResultadoFinal.POSITIVA);

        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updateCars(Long id, CarsDTO dto) {
        validarItemsCars(dto);
        Paciente paciente = findActiveById(id);
        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL) {
            throw new BusinessRuleException("CARS-2 no aplica para pacientes caso control");
        }

        BigDecimal rawScore = dto.item1().add(dto.item2()).add(dto.item3()).add(dto.item4()).add(dto.item5())
                .add(dto.item6()).add(dto.item7()).add(dto.item8()).add(dto.item9()).add(dto.item10())
                .add(dto.item11()).add(dto.item12()).add(dto.item13()).add(dto.item14()).add(dto.item15());

        EvaluacionCars cars = Optional.ofNullable(paciente.getEvaluacionCars())
                .orElseGet(() -> { var c = new EvaluacionCars(); c.setPaciente(paciente); return c; });
        cars.setItem1(dto.item1());   cars.setItem2(dto.item2());   cars.setItem3(dto.item3());
        cars.setItem4(dto.item4());   cars.setItem5(dto.item5());   cars.setItem6(dto.item6());
        cars.setItem7(dto.item7());   cars.setItem8(dto.item8());   cars.setItem9(dto.item9());
        cars.setItem10(dto.item10()); cars.setItem11(dto.item11()); cars.setItem12(dto.item12());
        cars.setItem13(dto.item13()); cars.setItem14(dto.item14()); cars.setItem15(dto.item15());
        cars.setObs1(dto.obs1());     cars.setObs2(dto.obs2());     cars.setObs3(dto.obs3());
        cars.setObs4(dto.obs4());     cars.setObs5(dto.obs5());     cars.setObs6(dto.obs6());
        cars.setObs7(dto.obs7());     cars.setObs8(dto.obs8());     cars.setObs9(dto.obs9());
        cars.setObs10(dto.obs10());   cars.setObs11(dto.obs11());   cars.setObs12(dto.obs12());
        cars.setObs13(dto.obs13());   cars.setObs14(dto.obs14());   cars.setObs15(dto.obs15());
        cars.setRawScore(rawScore);
        cars.setTScore(dto.tScore());
        cars.setPercentil(dto.percentil());
        paciente.setEvaluacionCars(cars);
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updateVineland(Long id, VinelandDTO dto) {
        Paciente paciente = findActiveById(id);
        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL) {
            throw new BusinessRuleException("Vineland no aplica para pacientes caso control");
        }
        EvaluacionVineland vineland = Optional.ofNullable(paciente.getEvaluacionVineland())
                .orElseGet(() -> { var v = new EvaluacionVineland(); v.setPaciente(paciente); return v; });
        vineland.setComunicacion(dto.comunicacion());
        vineland.setAutovalimiento(dto.autovalimiento());
        vineland.setSocial(dto.social());
        vineland.setMotor(dto.motor());
        vineland.setCocienteFinal(dto.cocienteFinal());
        vineland.setConductaDesadaptativa(dto.conductaDesadaptativa());
        vineland.setInternalizante(dto.internalizante());
        vineland.setExternalizante(dto.externalizante());
        paciente.setEvaluacionVineland(vineland);
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO updateSegundaVisita(Long id, PacienteSegundaVisitaDTO dto) {
        Paciente paciente = findActiveById(id);
        paciente.setFechaExtraccion(dto.fechaExtraccion());
        paciente.setEstadoClinico(calcularEstado(paciente));
        return mapper.toDTO(repository.save(paciente));
    }

    @Transactional
    public PacienteResponseDTO reenviarMchat(Long id) {
        Paciente paciente = findActiveById(id);
        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL) {
            throw new BusinessRuleException("El formulario M-CHAT no aplica para pacientes caso control");
        }
        paciente.setMchatToken(UUID.randomUUID().toString());
        paciente.setMchatTokenExpiry(LocalDateTime.now(clock).plusDays(tokenExpiryDays));
        Paciente saved = repository.save(paciente);

        try {
            mailService.enviarLinkMchat(
                    saved.getCorreoTutor(),
                    saved.getNombreTutor(),
                    saved.getApellidoNino(),
                    saved.getNombreNino(),
                    saved.getMchatToken()
            );
        } catch (Exception e) {
            log.warn("No se pudo reenviar el mail M-CHAT para paciente {}: {}", saved.getId(), e.getMessage());
        }

        return mapper.toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        Paciente paciente = findActiveById(id);
        paciente.setActivo(false);
        repository.save(paciente);
    }

    @Transactional(readOnly = true)
    public MchatInfoDTO validarTokenMchat(String token) {
        Paciente p = repository.findByMchatToken(token)
                .filter(paciente -> paciente.getMchatTokenExpiry() != null
                        && paciente.getMchatTokenExpiry().isAfter(LocalDateTime.now(clock)))
                .orElseThrow(() -> new ResourceNotFoundException("El enlace no es válido o ha expirado"));
        return new MchatInfoDTO(p.getId(), p.getNombreNino(), p.getApellidoNino());
    }

    @Transactional
    public void procesarMchatPublico(Long pacienteId) {
        Paciente paciente = findActiveById(pacienteId);
        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL) {
            throw new BusinessRuleException("El formulario M-CHAT no aplica para pacientes caso control");
        }
        paciente.setMchatToken(null);
        paciente.setMchatTokenExpiry(null);
        paciente.setEstadoClinico(calcularEstado(paciente));
        repository.save(paciente);
    }

    @Transactional
    public void actualizarMchatInterno(Long pacienteId) {
        Paciente paciente = findActiveById(pacienteId);
        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL) {
            throw new BusinessRuleException("El formulario M-CHAT no aplica para pacientes caso control");
        }
        MchatFamilia familia = paciente.getMchatFamilia();
        if (familia != null && (familia.getScoreTotal() < 3 || familia.getScoreTotal() > 7)) {
            paciente.setMchatSeguimiento(null);
        }
        paciente.setEstadoClinico(calcularEstado(paciente));
        repository.save(paciente);
    }

    private Paciente findActiveById(Long id) {
        return repository.findById(id)
                .filter(Paciente::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paciente con id " + id + " no existe"));
    }

    private Specification<Paciente> buildSpec(String q, List<PacienteEstado> estados, List<TipoPaciente> tipos) {
        Specification<Paciente> spec = activoTrue();
        if (q != null && !q.isBlank()) spec = spec.and(searchText(q));
        if (estados != null && !estados.isEmpty()) spec = spec.and(estadoIn(estados));
        if (tipos   != null && !tipos.isEmpty())   spec = spec.and(tipoIn(tipos));
        return spec;
    }

    private Specification<Paciente> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Specification<Paciente> searchText(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombreNino")), like),
                cb.like(cb.lower(root.get("apellidoNino")), like),
                cb.like(cb.lower(root.get("codigoNumerico")), like),
                cb.like(cb.lower(root.get("apellidoTutor")), like)
        );
    }

    private Specification<Paciente> estadoIn(List<PacienteEstado> estados) {
        return (root, query, cb) -> root.get("estadoClinico").in(estados);
    }

    private Specification<Paciente> tipoIn(List<TipoPaciente> tipos) {
        return (root, query, cb) -> root.get("tipoPaciente").in(tipos);
    }

    private Sort buildSort(String sortBy, String sortDir, String defaultField) {
        String field = SORT_FIELDS_VALIDOS.contains(sortBy) ? sortBy : defaultField;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private String generarCodigoNumerico() {
        String codigo;
        do {
            codigo = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (repository.existsByCodigoNumerico(codigo));
        return codigo;
    }

    private int contarFallas(MchatSeguimientoDTO dto) {
        return MchatScoringUtil.calcularScore(dto.toBooleanArray());
    }

    private void validarItemsCars(CarsDTO dto) {
        BigDecimal[] items = {
            dto.item1(), dto.item2(), dto.item3(), dto.item4(), dto.item5(),
            dto.item6(), dto.item7(), dto.item8(), dto.item9(), dto.item10(),
            dto.item11(), dto.item12(), dto.item13(), dto.item14(), dto.item15()
        };
        for (int i = 0; i < items.length; i++) {
            BigDecimal item = items[i];
            if (item == null || CARS_VALORES_VALIDOS.stream().noneMatch(v -> v.compareTo(item) == 0)) {
                throw new BusinessRuleException(
                        "Valor inválido en ítem " + (i + 1) + " de CARS-2: " + items[i]
                        + ". Valores permitidos: 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0");
            }
        }
    }

    private PacienteEstado calcularEstado(Paciente p) {
        if (p.getFechaExtraccion() != null)
            return PacienteEstado.EXTRACCION_PENDIENTE;
        if (p.getTipoPaciente() == TipoPaciente.PROBLEMA && p.getMchatFamilia() != null)
            return PacienteEstado.MCHAT_RESPONDIDO;
        return PacienteEstado.ADMITIDO;
    }

}
