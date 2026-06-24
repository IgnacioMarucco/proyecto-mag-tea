package com.utn.magtea.paciente;

import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.SpecificationUtils;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.formulariointeres.FormularioInteres;
import com.utn.magtea.formulariointeres.FormularioInteresService;
import com.utn.magtea.paciente.cars.CarsDTO;
import com.utn.magtea.paciente.cars.CarsService;
import com.utn.magtea.paciente.criterios.Criterios;
import com.utn.magtea.paciente.criterios.CriteriosAptitud;
import com.utn.magtea.paciente.criterios.CriteriosDTO;
import com.utn.magtea.paciente.criterios.CriteriosUtil;
import com.utn.magtea.paciente.mchat.MchatEvents;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatService;
import com.utn.magtea.paciente.mchat.MchatTokenService;
import com.utn.magtea.paciente.mchat.MchatInfoDTO;
import com.utn.magtea.paciente.mchat.MchatSeguimientoDTO;
import com.utn.magtea.paciente.vineland.EvaluacionVineland;
import com.utn.magtea.paciente.vineland.VinelandDTO;
import com.utn.magtea.pool.PoolSueroAporteRepository;
import com.utn.magtea.suero.Suero;
import com.utn.magtea.suero.SueroRepository;
import com.utn.magtea.tubo.TuboRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.utn.magtea.common.CodigoUtil;
import com.utn.magtea.paciente.mchat.MchatEstado;

@Slf4j
@Service
@RequiredArgsConstructor
public class PacienteService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "apellidoNino", "apellidoTutor", "nombreNino", "proximaFechaEvento");

    private final PacienteRepository repository;
    private final PacienteMapper mapper;
    private final FormularioInteresService formularioService;
    private final MchatTokenService mchatTokenService;
    private final MchatService mchatService;
    private final CarsService carsService;
    private final ApplicationEventPublisher events;
    private final PoolSueroAporteRepository poolSueroAporteRepository;
    private final SueroRepository sueroRepository;
    private final TuboRepository tuboRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PageResponse<PacienteListDTO> findAll(int page, int size, String q,
                                                  List<PacienteEstado> estados,
                                                  List<TipoPaciente> tipos,
                                                  String sortBy, String sortDir) {
        Sort sort = SpecificationUtils.buildSort(sortBy, sortDir, "proximaFechaEvento", SORT_FIELDS_VALIDOS);
        Page<PacienteListProjection> result = repository.findBy(
                buildSpec(q, estados, tipos),
                fq -> fq.as(PacienteListProjection.class).page(PageRequest.of(page, size, sort))
        );
        List<PacienteListDTO> content = result.getContent().stream()
                .map(p -> new PacienteListDTO(
                        p.getId(), p.getCodigoNumerico(), p.getApellidoTutor(), p.getNombreTutor(),
                        p.getApellidoNino(), p.getNombreNino(), p.getFechaNacimientoNino(),
                        p.getTipoPaciente(), p.getEstadoClinico(),
                        p.getFechaPrimeraVisita(), p.getFechaTurnoExtraccion(), p.getProximaFechaEvento()
                ))
                .toList();
        return new PageResponse<>(content, result.getTotalElements(),
                result.getTotalPages(), result.getNumber(), result.getSize());
    }

    @Transactional(readOnly = true)
    public PacienteResponseDTO findById(Long id) {
        Paciente p = findActiveById(id);
        return mapper.toDTO(p, calcularMchatEstado(p));
    }

    @Transactional(readOnly = true)
    public PacienteResponseDTO findByCodigoFull(String codigo) {
        Paciente p = repository.findActiveByCodigoNumericoWithGraph(codigo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paciente con código " + codigo + " no existe"));
        return mapper.toDTO(p, calcularMchatEstado(p));
    }

    @Transactional(readOnly = true)
    public PacientePorCodigoDTO findByCodigoNumerico(String codigo) {
        Paciente p = repository.findByCodigoNumericoAndActivoTrue(codigo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un paciente con código " + codigo));
        if (p.getEstadoClinico() != PacienteEstado.EXTRACCION_PENDIENTE) {
            throw new BusinessRuleException(
                    "El paciente con código " + codigo + " no tiene extracción pendiente");
        }
        return new PacientePorCodigoDTO(
                p.getId(), p.getCodigoNumerico(),
                p.getNombreNino(), p.getApellidoNino(),
                p.getFechaTurnoExtraccion(), p.getTipoPaciente());
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
        mapCriterios(criterios, new CriteriosDTO(
                dto.criterioTEADSMV(), dto.criterioTGDDSMIV(), dto.criterioEdad(),
                dto.epilepsia(), dto.paralisisCerebral(), dto.infeccionesCongenitas(),
                dto.lesionesEstructuralesSNC(), dto.facomatosis(), dto.patologiasNeurometabolicas(),
                dto.lesionesOcupantesEspacioSNC(), dto.patologiaPsiquiatrica(),
                dto.otrosSindromesGeneticos(), dto.pubertadPrecoz()));
        paciente.setCriterios(criterios);

        CriteriosAptitud aptitud = CriteriosUtil.calcularAptitud(paciente);
        if (aptitud != CriteriosAptitud.APTO) {
            String mensaje = aptitud == CriteriosAptitud.EXCLUIDO
                    ? "El paciente presenta un criterio de exclusión y no puede ser admitido al protocolo."
                    : paciente.getTipoPaciente() == TipoPaciente.CONTROL
                            ? "Para caso control los criterios clínicos TEA y TGD no deben presentarse, y el criterio de edad debe cumplirse."
                            : "Para caso problema los tres criterios de inclusión (TEA DSM-V, TGD DSM-IV y edad) deben cumplirse.";
            throw new BusinessRuleException(mensaje);
        }

        paciente.setEstadoClinico(PacienteEstado.ADMITIDO);

        if (dto.tipoPaciente() == TipoPaciente.PROBLEMA) {
            mchatTokenService.generarToken(paciente);
        }

        Paciente saved = repository.save(paciente);

        if (dto.tipoPaciente() == TipoPaciente.PROBLEMA) {
            events.publishEvent(new MchatEvents.MchatEnviadoEvent(
                    saved.getCorreoTutor(), saved.getNombreTutor(),
                    saved.getApellidoNino(), saved.getNombreNino(), saved.getMchatToken()));
        }

        return mapper.toDTO(saved, calcularMchatEstado(saved));
    }

    @Transactional
    public PacienteResponseDTO update(String codigo, PacienteUpdateDTO dto) {
        Paciente paciente = findActiveByCodigo(codigo);
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
        if (dto.fechaTurnoExtraccion() != null) paciente.setFechaTurnoExtraccion(dto.fechaTurnoExtraccion());
        Paciente saved = repository.save(paciente);
        return mapper.toDTO(saved, calcularMchatEstado(saved));
    }

    @Transactional
    public PacienteResponseDTO updatePrimeraVisita(String codigo, PacientePrimeraVisitaDTO dto) {
        Paciente paciente = findActiveByCodigo(codigo);
        paciente.setFechaPrimeraVisita(dto.fechaPrimeraVisita());
        Paciente saved = repository.save(paciente);
        return mapper.toDTO(saved, calcularMchatEstado(saved));
    }

    @Transactional
    public PacienteResponseDTO updateConsentimiento(String codigo, PacienteConsentimientoDTO dto) {
        Paciente paciente = findActiveByCodigo(codigo);
        paciente.setConsentimientoFirmado(dto.consentimientoFirmado());
        Paciente saved = repository.save(paciente);
        return mapper.toDTO(saved, calcularMchatEstado(saved));
    }

    @Transactional
    public PacienteResponseDTO updateCriterios(String codigo, CriteriosDTO dto) {
        Paciente paciente = findActiveByCodigo(codigo);
        Criterios criterios = Optional.ofNullable(paciente.getCriterios())
                .orElseGet(() -> { var c = new Criterios(); c.setPaciente(paciente); return c; });
        mapCriterios(criterios, dto);
        paciente.setCriterios(criterios);
        Paciente saved = repository.save(paciente);
        return mapper.toDTO(saved, calcularMchatEstado(saved));
    }

    @Transactional
    public PacienteResponseDTO updateMchatSeguimiento(String codigo, MchatSeguimientoDTO dto) {
        Paciente paciente = findActiveByCodigo(codigo);

        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL) {
            throw new BusinessRuleException(
                    "El seguimiento M-CHAT no aplica para pacientes caso control");
        }
        MchatFamilia familia = paciente.getMchatFamilia();
        if (familia == null || familia.getScoreTotal() < 3 || familia.getScoreTotal() > 7) {
            throw new BusinessRuleException(
                    "El seguimiento M-CHAT solo aplica cuando el score está entre 3 y 7");
        }

        mchatService.aplicarSeguimiento(paciente, dto);
        Paciente saved = repository.save(paciente);
        return mapper.toDTO(saved, calcularMchatEstado(saved));
    }

    @Transactional
    public PacienteResponseDTO updateCars(String codigo, CarsDTO dto) {
        carsService.validarItems(dto);
        Paciente paciente = findActiveByCodigo(codigo);
        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL) {
            throw new BusinessRuleException("CARS-2 no aplica para pacientes caso control");
        }
        carsService.aplicar(paciente, dto);
        Paciente saved = repository.save(paciente);
        return mapper.toDTO(saved, calcularMchatEstado(saved));
    }

    @Transactional
    public PacienteResponseDTO updateVineland(String codigo, VinelandDTO dto) {
        Paciente paciente = findActiveByCodigo(codigo);
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
        Paciente saved = repository.save(paciente);
        return mapper.toDTO(saved, calcularMchatEstado(saved));
    }

    @Transactional
    public PacienteResponseDTO updateSegundaVisita(String codigo, PacienteSegundaVisitaDTO dto) {
        Paciente paciente = findActiveByCodigo(codigo);
        paciente.setFechaTurnoExtraccion(dto.fechaTurnoExtraccion());
        paciente.setEstadoClinico(calcularEstado(paciente));
        Paciente saved = repository.save(paciente);
        return mapper.toDTO(saved, calcularMchatEstado(saved));
    }

    @Transactional
    public PacienteResponseDTO reenviarMchat(String codigo) {
        Paciente paciente = findActiveByCodigo(codigo);
        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL) {
            throw new BusinessRuleException("El formulario M-CHAT no aplica para pacientes caso control");
        }
        mchatTokenService.generarToken(paciente);
        Paciente saved = repository.save(paciente);
        events.publishEvent(new MchatEvents.MchatEnviadoEvent(
                saved.getCorreoTutor(), saved.getNombreTutor(),
                saved.getApellidoNino(), saved.getNombreNino(), saved.getMchatToken()));
        return mapper.toDTO(saved, calcularMchatEstado(saved));
    }

    @Transactional
    public void delete(String codigo) {
        Paciente paciente = findActiveByCodigo(codigo);
        Optional<Suero> sueroOpt = sueroRepository.findByPacienteCodigoNumericoAndActivoTrue(codigo);
        if (sueroOpt.isPresent()) {
            Suero suero = sueroOpt.get();
            boolean tieneAportesActivos = suero.getTubos().stream()
                    .anyMatch(t -> poolSueroAporteRepository.existsByTuboIdAndPool_ActivoTrue(t.getId()));
            if (tieneAportesActivos) {
                throw new BusinessRuleException(
                        "El paciente tiene un suero con aportes en pools activos. Dé de baja los pools primero.");
            }
            suero.getTubos().forEach(t -> { t.setPosicion(null); tuboRepository.save(t); });
            suero.setActivo(false);
            sueroRepository.save(suero);
        }
        paciente.setMchatToken(null);
        paciente.setMchatTokenExpiry(null);
        paciente.setActivo(false);
        repository.save(paciente);
    }

    @Transactional(readOnly = true)
    public MchatInfoDTO validarTokenMchat(String token) {
        return mchatTokenService.validarToken(token);
    }

    @EventListener
    void onMchatFamiliaGuardada(MchatEvents.MchatFamiliaGuardadaEvent event) {
        log.debug("Evento {} para paciente {}", "MchatFamiliaGuardadaEvent", event.pacienteId());
        Paciente paciente = findActiveById(event.pacienteId());
        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL) return;
        paciente.setMchatToken(null);
        paciente.setMchatTokenExpiry(null);
        paciente.setEstadoClinico(calcularEstado(paciente));
        repository.save(paciente);
    }

    @EventListener
    void onMchatFamiliaActualizada(MchatEvents.MchatFamiliaActualizadaEvent event) {
        log.debug("Evento {} para paciente {}", "MchatFamiliaActualizadaEvent", event.pacienteId());
        Paciente paciente = findActiveById(event.pacienteId());
        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL) return;
        MchatFamilia familia = paciente.getMchatFamilia();
        if (familia != null && (familia.getScoreTotal() < 3 || familia.getScoreTotal() > 7)) {
            paciente.setMchatSeguimiento(null);
        }
        paciente.setEstadoClinico(calcularEstado(paciente));
        repository.save(paciente);
    }

    @EventListener
    void onSueroRegistrado(PacienteEvents.SueroRegistradoEvent event) {
        log.debug("Evento {} para paciente {}", "SueroRegistradoEvent", event.pacienteId());
        Paciente p = findActiveById(event.pacienteId());
        p.setEstadoClinico(PacienteEstado.EXTRACCION_REALIZADA);
        repository.save(p);
    }

    @EventListener
    void onSueroEliminado(PacienteEvents.SueroEliminadoEvent event) {
        log.debug("Evento {} para paciente {}", "SueroEliminadoEvent", event.pacienteId());
        Paciente p = findActiveById(event.pacienteId());
        p.setEstadoClinico(PacienteEstado.EXTRACCION_PENDIENTE);
        repository.save(p);
    }

    private Paciente findActiveById(Long id) {
        return repository.findById(id)
                .filter(Paciente::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paciente con id " + id + " no existe"));
    }

    private Paciente findActiveByCodigo(String codigo) {
        return repository.findByCodigoNumericoAndActivoTrue(codigo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paciente con código " + codigo + " no existe"));
    }

    private Specification<Paciente> buildSpec(String q, List<PacienteEstado> estados, List<TipoPaciente> tipos) {
        Specification<Paciente> spec = SpecificationUtils.activoTrue();
        if (q != null && !q.isBlank()) spec = spec.and(searchText(q));
        if (estados != null && !estados.isEmpty()) spec = spec.and(estadoIn(estados));
        if (tipos   != null && !tipos.isEmpty())   spec = spec.and(tipoIn(tipos));
        return spec;
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

    private String generarCodigoNumerico() {
        return CodigoUtil.generarCodigo(repository::existsByCodigoNumerico);
    }

    private static void mapCriterios(Criterios c, CriteriosDTO dto) {
        c.setCriterioTEADSMV(dto.criterioTEADSMV());
        c.setCriterioTGDDSMIV(dto.criterioTGDDSMIV());
        c.setCriterioEdad(dto.criterioEdad());
        c.setEpilepsia(dto.epilepsia());
        c.setParalisisCerebral(dto.paralisisCerebral());
        c.setInfeccionesCongenitas(dto.infeccionesCongenitas());
        c.setLesionesEstructuralesSNC(dto.lesionesEstructuralesSNC());
        c.setFacomatosis(dto.facomatosis());
        c.setPatologiasNeurometabolicas(dto.patologiasNeurometabolicas());
        c.setLesionesOcupantesEspacioSNC(dto.lesionesOcupantesEspacioSNC());
        c.setPatologiaPsiquiatrica(dto.patologiaPsiquiatrica());
        c.setOtrosSindromesGeneticos(dto.otrosSindromesGeneticos());
        c.setPubertadPrecoz(dto.pubertadPrecoz());
    }

    private MchatEstado calcularMchatEstado(Paciente p) {
        if (p.getMchatFamilia() != null) return MchatEstado.COMPLETADO;
        if (p.getMchatToken() != null && p.getMchatTokenExpiry() != null
                && p.getMchatTokenExpiry().isAfter(LocalDateTime.now(clock))) return MchatEstado.PENDIENTE;
        if (p.getMchatToken() != null) return MchatEstado.EXPIRADO;
        return MchatEstado.NO_ENVIADO;
    }

    private PacienteEstado calcularEstado(Paciente p) {
        if (p.getEstadoClinico() == PacienteEstado.EXTRACCION_REALIZADA)
            return PacienteEstado.EXTRACCION_REALIZADA;
        if (p.getFechaTurnoExtraccion() != null)
            return PacienteEstado.EXTRACCION_PENDIENTE;
        if (p.getTipoPaciente() == TipoPaciente.CONTROL && p.getMchatFamilia() != null)
            log.warn("Paciente CONTROL {} tiene mchatFamilia asignado — inconsistencia de datos", p.getCodigoNumerico());
        if (p.getTipoPaciente() == TipoPaciente.PROBLEMA && p.getMchatFamilia() != null)
            return PacienteEstado.MCHAT_RESPONDIDO;
        return PacienteEstado.ADMITIDO;
    }

}
