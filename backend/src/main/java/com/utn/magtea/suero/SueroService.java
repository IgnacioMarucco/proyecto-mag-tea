package com.utn.magtea.suero;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.caja.CajaRepository;
import com.utn.magtea.common.DomainConstants;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.SpecificationUtils;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteEvents;
import com.utn.magtea.paciente.PacienteRepository;
import com.utn.magtea.paciente.PacienteEstado;
import com.utn.magtea.paciente.TipoPaciente;
import com.utn.magtea.paciente.criterios.CriteriosAptitud;
import com.utn.magtea.paciente.criterios.CriteriosUtil;
import com.utn.magtea.pool.PoolSueroAporteRepository;
import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TipoTubo;
import com.utn.magtea.tubo.TuboInputDTO;
import com.utn.magtea.tubo.TuboRepository;
import com.utn.magtea.tubo.TuboService;
import com.utn.magtea.tubo.VaciarTuboRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SueroService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "fechaExtraccion", "rango", "codigoNumerico");
    private static final Map<String, String> SORT_FIELD_ALIASES = Map.of("codigoNumerico", "paciente.codigoNumerico");

    private final SueroRepository repository;
    private final SueroMapper mapper;
    private final TuboRepository tuboRepository;
    private final PacienteRepository pacienteRepository;
    private final PoolSueroAporteRepository poolSueroAporteRepository;
    private final ApplicationEventPublisher events;
    private final CajaRepository cajaRepository;
    private final TuboService tuboService;

    @Transactional(readOnly = true)
    public PageResponse<SueroListDTO> findAll(int page, int size, String q, List<Integer> rangos,
                                              List<SueroUso> usos, String codigoPaciente,
                                              String sortBy, String sortDir) {
        Sort sort = SpecificationUtils.buildSort(sortBy, sortDir, "createdAt", SORT_FIELDS_VALIDOS, SORT_FIELD_ALIASES);
        Page<Suero> result = repository.findAll(
                buildSpec(q, rangos, usos, codigoPaciente),
                PageRequest.of(page, size, sort));
        return new PageResponse<>(
                result.map(mapper::toListDTO).getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Transactional(readOnly = true)
    public SueroResponseDTO findById(Long id) {
        return mapper.toDTO(findActiveById(id));
    }

    @Transactional(readOnly = true)
    public SueroResponseDTO findByCodigoNumerico(String codigoNumerico) {
        Suero suero = repository.findByPacienteCodigoNumericoAndActivoTrue(codigoNumerico)
                .orElseThrow(() -> new ResourceNotFoundException("Suero con código " + codigoNumerico + " no existe"));
        return mapper.toDTO(suero);
    }

    @Transactional
    public SueroResponseDTO create(SueroCreateDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.pacienteId())
                .filter(Paciente::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente con id " + dto.pacienteId() + " no existe"));

        if (!paciente.isConsentimientoFirmado()) {
            throw new BusinessRuleException("El paciente no tiene consentimiento informado firmado");
        }

        if (paciente.getEstadoClinico() != PacienteEstado.EXTRACCION_PENDIENTE) {
            throw new BusinessRuleException("El paciente no tiene extracción pendiente");
        }

        CriteriosAptitud aptitudPaciente = CriteriosUtil.calcularAptitud(paciente);
        if (aptitudPaciente != null && aptitudPaciente != CriteriosAptitud.APTO) {
            throw new BusinessRuleException(
                aptitudPaciente == CriteriosAptitud.EXCLUIDO
                    ? "El paciente presenta un criterio de exclusión y no puede participar en el protocolo"
                    : "El paciente no tiene criterios de aptitud completos para el protocolo"
            );
        }

        if (repository.existsByPacienteIdAndActivoTrue(dto.pacienteId())) {
            throw new BusinessRuleException("El paciente ya tiene un suero registrado");
        }

        Caja caja = cajaRepository.findById(dto.cajaId())
                .filter(Caja::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Caja con id " + dto.cajaId() + " no existe"));

        tuboService.validarPosicionesSinConflicto(dto.cajaId(), dto.tubos(), null, null, Set.of());

        Suero suero = new Suero();
        suero.setPaciente(paciente);
        suero.setCaja(caja);
        suero.setFechaExtraccion(dto.fechaExtraccion());
        suero.setValorAnticuerpos(dto.valorAnticuerpos());
        suero.setRango(SueroRangoUtil.calcularRango(dto.valorAnticuerpos()));
        suero.setUso(paciente.getTipoPaciente() == TipoPaciente.CONTROL ? SueroUso.CONTROL : SueroUso.PROBLEMA);
        Suero saved = repository.save(suero);

        for (TuboInputDTO t : dto.tubos()) {
            Tubo tubo = new Tubo();
            tubo.setTipo(TipoTubo.SUERO);
            tubo.setCaja(caja);
            tubo.setSuero(saved);
            tubo.setPosicion(t.posicion());
            tubo.setCantidadInicial(t.cantidadInicial());
            tuboRepository.save(tubo);
        }

        events.publishEvent(new PacienteEvents.SueroRegistradoEvent(paciente.getId()));

        return mapper.toDTO(repository.findById(saved.getId()).orElseThrow());
    }

    @Transactional
    public SueroResponseDTO update(Long id, SueroUpdateDTO dto) {
        Suero suero = findActiveById(id);

        Caja caja = cajaRepository.findById(dto.cajaId())
                .filter(Caja::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Caja con id " + dto.cajaId() + " no existe"));

        tuboService.validarPosicionesSinConflicto(dto.cajaId(), dto.tubos(), id, null, Set.of());

        Map<String, Tubo> existingByPosicion = tuboRepository.findBySueroId(id).stream()
                .collect(Collectors.toMap(Tubo::getPosicion, t -> t));
        Set<String> newPosiciones = dto.tubos().stream()
                .map(TuboInputDTO::posicion)
                .collect(Collectors.toSet());

        for (Tubo t : existingByPosicion.values()) {
            if (!newPosiciones.contains(t.getPosicion()) && t.getCantidadUsada().compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessRuleException(
                        "El tubo " + t.getPosicion() + " tiene " + t.getCantidadUsada()
                        + " mL usados y no puede eliminarse");
            }
        }

        for (TuboInputDTO t : dto.tubos()) {
            Tubo existing = existingByPosicion.get(t.posicion());
            if (existing != null && t.cantidadInicial().compareTo(existing.getCantidadUsada()) < 0) {
                throw new BusinessRuleException(
                        "El tubo " + t.posicion() + " ya tiene " + existing.getCantidadUsada()
                        + " mL usados; la nueva cantidad inicial no puede ser menor");
            }
        }

        for (Tubo t : existingByPosicion.values()) {
            if (!newPosiciones.contains(t.getPosicion())) {
                suero.getTubos().remove(t);
            }
        }

        for (TuboInputDTO t : dto.tubos()) {
            Tubo existing = existingByPosicion.get(t.posicion());
            if (existing != null) {
                existing.setCaja(caja);
                existing.setCantidadInicial(t.cantidadInicial());
                tuboRepository.save(existing);
            } else {
                Tubo newTubo = new Tubo();
                newTubo.setTipo(TipoTubo.SUERO);
                newTubo.setCaja(caja);
                newTubo.setSuero(suero);
                newTubo.setPosicion(t.posicion());
                newTubo.setCantidadInicial(t.cantidadInicial());
                tuboRepository.save(newTubo);
                suero.getTubos().add(newTubo);
            }
        }

        suero.setCaja(caja);
        suero.setFechaExtraccion(dto.fechaExtraccion());
        suero.setValorAnticuerpos(dto.valorAnticuerpos());
        suero.setRango(SueroRangoUtil.calcularRango(dto.valorAnticuerpos()));

        return mapper.toDTO(repository.save(suero));
    }

    @Transactional
    public void delete(Long id) {
        Suero suero = findActiveById(id);
        boolean tieneAportesActivos = suero.getTubos().stream()
                .anyMatch(tubo -> poolSueroAporteRepository.existsByTuboIdAndPool_ActivoTrue(tubo.getId()));
        if (tieneAportesActivos)
            throw new BusinessRuleException("El suero tiene aportes en pools activos y no puede darse de baja");
        for (Tubo tubo : suero.getTubos()) {
            tubo.setPosicion(null);
            tuboRepository.save(tubo);
        }
        suero.setActivo(false);
        repository.save(suero);
        events.publishEvent(new PacienteEvents.SueroEliminadoEvent(suero.getPaciente().getId()));
    }

    @Transactional
    public SueroResponseDTO liberarGrilla(Long id, VaciarTuboRequest req) {
        Suero suero = findActiveById(id);
        suero.getTubos().forEach(tubo -> tuboService.vaciar(tubo.getId(), req));
        return mapper.toDTO(repository.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public List<SueroDisponibilidadDTO> getDisponibilidadPool() {
        List<Tubo> tubos = tuboRepository.findBySueroActivoTrue();

        record Grupo(SueroUso uso, Integer rango) {}
        Map<Grupo, List<Tubo>> porGrupo = tubos.stream()
                .filter(t -> t.getSuero().getUso() != null && t.getSuero().getRango() != null)
                .filter(t -> t.getCantidadRestante().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.groupingBy(t -> new Grupo(t.getSuero().getUso(), t.getSuero().getRango())));

        List<SueroDisponibilidadDTO> result = new ArrayList<>();
        for (SueroUso uso : SueroUso.values()) {
            for (int rango = 0; rango <= 3; rango++) {
                List<Tubo> grupo = porGrupo.getOrDefault(new Grupo(uso, rango), List.of());
                if (grupo.isEmpty()) continue;
                long cantidadSueros = grupo.stream()
                        .map(t -> t.getSuero().getId())
                        .distinct()
                        .count();
                BigDecimal mlDisponibles = grupo.stream()
                        .map(Tubo::getCantidadRestante)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                int ratonesPosibles = mlDisponibles.divide(DomainConstants.ML_POR_RATON, 0, RoundingMode.FLOOR).intValue();
                result.add(new SueroDisponibilidadDTO(uso, rango, cantidadSueros, mlDisponibles, ratonesPosibles));
            }
        }
        return result;
    }

    private Specification<Suero> buildSpec(String q, List<Integer> rangos, List<SueroUso> usos, String codigoPaciente) {
        Specification<Suero> spec = SpecificationUtils.activoTrue();
        if (q != null && !q.isBlank())           spec = spec.and(searchByCodigo(q));
        if (rangos != null && !rangos.isEmpty()) spec = spec.and(rangoIn(rangos));
        if (usos != null && !usos.isEmpty())     spec = spec.and(usoIn(usos));
        if (codigoPaciente != null)              spec = spec.and(codigoPacienteEquals(codigoPaciente));
        return spec;
    }

    private Specification<Suero> searchByCodigo(String q) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("paciente").get("codigoNumerico")),
                "%" + q.toLowerCase() + "%");
    }

    private Specification<Suero> codigoPacienteEquals(String codigo) {
        return (root, query, cb) -> cb.equal(root.get("paciente").get("codigoNumerico"), codigo);
    }

    private Specification<Suero> rangoIn(List<Integer> rangos) {
        return (root, query, cb) -> root.get("rango").in(rangos);
    }

    private Specification<Suero> usoIn(List<SueroUso> usos) {
        return (root, query, cb) -> root.get("uso").in(usos);
    }

    private Suero findActiveById(Long id) {
        return repository.findById(id)
                .filter(Suero::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Suero con id " + id + " no existe"));
    }
}
