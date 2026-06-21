package com.utn.magtea.suero;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.caja.CajaRepository;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteEstado;
import com.utn.magtea.paciente.PacienteRepository;
import com.utn.magtea.paciente.TipoPaciente;
import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TipoTubo;
import com.utn.magtea.tubo.TuboInputDTO;
import com.utn.magtea.tubo.TuboRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SueroService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "fechaExtraccion", "rango", "codigoNumerico");

    private final SueroRepository repository;
    private final SueroMapper mapper;
    private final TuboRepository tuboRepository;
    private final PacienteRepository pacienteRepository;
    private final CajaRepository cajaRepository;

    @Transactional(readOnly = true)
    public PageResponse<SueroListDTO> findAll(int page, int size, String q, List<Integer> rangos,
                                              SueroUso uso, String codigoPaciente,
                                              String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir, "createdAt");
        Page<Suero> result = repository.findAll(
                buildSpec(q, rangos, uso, codigoPaciente),
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

        if (repository.existsByPacienteIdAndActivoTrue(dto.pacienteId())) {
            throw new BusinessRuleException("El paciente ya tiene un suero registrado");
        }

        Caja caja = cajaRepository.findById(dto.cajaId())
                .filter(Caja::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Caja con id " + dto.cajaId() + " no existe"));

        validarPosicionesSinSuperposicion(dto.cajaId(), dto.tubos(), null);

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

        paciente.setEstadoClinico(PacienteEstado.EXTRACCION_REALIZADA);
        pacienteRepository.save(paciente);

        return mapper.toDTO(repository.findById(saved.getId()).orElseThrow());
    }

    @Transactional
    public SueroResponseDTO update(Long id, SueroUpdateDTO dto) {
        Suero suero = findActiveById(id);

        Caja caja = cajaRepository.findById(dto.cajaId())
                .filter(Caja::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Caja con id " + dto.cajaId() + " no existe"));

        validarPosicionesSinSuperposicion(dto.cajaId(), dto.tubos(), id);

        Map<String, Tubo> existingByPosicion = tuboRepository.findBySueroId(id).stream()
                .collect(Collectors.toMap(Tubo::getPosicion, t -> t));
        Set<String> newPosiciones = dto.tubos().stream()
                .map(TuboInputDTO::posicion)
                .collect(Collectors.toSet());

        for (Tubo t : existingByPosicion.values()) {
            if (!newPosiciones.contains(t.getPosicion()) && t.getCantidadUsada() > 0) {
                throw new BusinessRuleException(
                        "El tubo " + t.getPosicion() + " tiene " + t.getCantidadUsada()
                        + " mL usados y no puede eliminarse");
            }
        }

        for (TuboInputDTO t : dto.tubos()) {
            Tubo existing = existingByPosicion.get(t.posicion());
            if (existing != null && t.cantidadInicial() < existing.getCantidadUsada()) {
                throw new BusinessRuleException(
                        "El tubo " + t.posicion() + " ya tiene " + existing.getCantidadUsada()
                        + " mL usados; la nueva cantidad inicial no puede ser menor");
            }
        }

        for (Tubo t : existingByPosicion.values()) {
            if (!newPosiciones.contains(t.getPosicion())) {
                tuboRepository.delete(t);
            }
        }

        for (TuboInputDTO t : dto.tubos()) {
            Tubo existing = existingByPosicion.get(t.posicion());
            if (existing != null) {
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
        suero.setActivo(false);
        repository.save(suero);
    }

    @Transactional(readOnly = true)
    public List<SueroDisponibilidadDTO> getDisponibilidadPool() {
        List<Tubo> tubos = tuboRepository.findBySueroActivoTrue();

        record Grupo(SueroUso uso, Integer rango) {}
        Map<Grupo, List<Tubo>> porGrupo = tubos.stream()
                .filter(t -> t.getSuero().getUso() != null && t.getSuero().getRango() != null)
                .filter(t -> t.getCantidadRestante() > 0)
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
                double mlDisponibles = grupo.stream()
                        .mapToDouble(Tubo::getCantidadRestante)
                        .sum();
                int ratonesPosibles = (int) Math.floor(mlDisponibles / 0.2);
                result.add(new SueroDisponibilidadDTO(uso, rango, cantidadSueros, mlDisponibles, ratonesPosibles));
            }
        }
        return result;
    }

    private void validarPosicionesSinSuperposicion(Long cajaId, List<TuboInputDTO> nuevos, Long excludeSueroId) {
        Set<String> nuevasPos = nuevos.stream()
                .map(TuboInputDTO::posicion)
                .collect(Collectors.toSet());

        Set<String> ocupadasSuero = tuboRepository.findByCajaIdAndSueroActivoTrue(cajaId).stream()
                .filter(t -> excludeSueroId == null || !t.getSuero().getId().equals(excludeSueroId))
                .filter(t -> t.getCantidadRestante() > 0)
                .map(Tubo::getPosicion)
                .collect(Collectors.toSet());

        Set<String> conflictoSuero = new HashSet<>(nuevasPos);
        conflictoSuero.retainAll(ocupadasSuero);
        if (!conflictoSuero.isEmpty()) {
            throw new BusinessRuleException(
                    "Las posiciones " + String.join(", ", conflictoSuero) + " ya están ocupadas en esta caja");
        }

        Set<String> ocupadasPool = tuboRepository.findByCajaIdAndPoolActivoTrue(cajaId).stream()
                .filter(t -> t.getCantidadRestante() > 0)
                .map(Tubo::getPosicion)
                .collect(Collectors.toSet());

        Set<String> conflictoPool = new HashSet<>(nuevasPos);
        conflictoPool.retainAll(ocupadasPool);
        if (!conflictoPool.isEmpty()) {
            throw new BusinessRuleException(
                    "Las posiciones " + String.join(", ", conflictoPool) + " ya están ocupadas por un pool en esta caja");
        }
    }

    private Specification<Suero> buildSpec(String q, List<Integer> rangos, SueroUso uso, String codigoPaciente) {
        Specification<Suero> spec = activoTrue();
        if (q != null && !q.isBlank())       spec = spec.and(searchByCodigo(q));
        if (rangos != null && !rangos.isEmpty()) spec = spec.and(rangoIn(rangos));
        if (uso != null)              spec = spec.and(usoEquals(uso));
        if (codigoPaciente != null)   spec = spec.and(codigoPacienteEquals(codigoPaciente));
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

    private Specification<Suero> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Specification<Suero> rangoIn(List<Integer> rangos) {
        return (root, query, cb) -> root.get("rango").in(rangos);
    }

    private Specification<Suero> usoEquals(SueroUso uso) {
        return (root, query, cb) -> cb.equal(root.get("uso"), uso);
    }

    private Sort buildSort(String sortBy, String sortDir, String defaultField) {
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String field = SORT_FIELDS_VALIDOS.contains(sortBy) ? sortBy : defaultField;
        if ("codigoNumerico".equals(field)) {
            String nestedField = "paciente.codigoNumerico";
            return Sort.by(dir, nestedField);
        }
        return Sort.by(dir, field);
    }

    private Suero findActiveById(Long id) {
        return repository.findById(id)
                .filter(Suero::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Suero con id " + id + " no existe"));
    }
}
