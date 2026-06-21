package com.utn.magtea.pool;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.caja.CajaRepository;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.suero.SueroUso;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoolService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "fechaCreacion", "rango");
    private static final double ML_POR_RATON = 0.2;

    private final PoolRepository repository;
    private final PoolMapper mapper;
    private final TuboRepository tuboRepository;
    private final PoolSueroAporteRepository poolSueroAporteRepository;
    private final CajaRepository cajaRepository;

    @Transactional(readOnly = true)
    public PageResponse<PoolListDTO> findAll(int page, int size, List<Integer> rangos, List<SueroUso> usos,
                                             String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir, "createdAt");
        Page<Pool> result = repository.findAll(buildSpec(rangos, usos), PageRequest.of(page, size, sort));
        return new PageResponse<>(
                result.map(mapper::toListDTO).getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Transactional(readOnly = true)
    public PoolResponseDTO findById(Long id) {
        return mapper.toDTO(findActiveById(id));
    }

    @Transactional
    public PoolResponseDTO create(PoolCreateDTO dto) {
        Caja caja = cajaRepository.findById(dto.cajaId())
                .filter(Caja::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Caja con id " + dto.cajaId() + " no existe"));

        // Load and validate suero tubes
        List<Tubo> sueroTubos = dto.aportes().stream()
                .map(a -> {
                    Tubo t = tuboRepository.findById(a.sueroTuboId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Tubo con id " + a.sueroTuboId() + " no existe"));
                    if (t.getTipo() != TipoTubo.SUERO) {
                        throw new BusinessRuleException(
                                "El tubo en posición " + t.getPosicion() + " no es un tubo de suero");
                    }
                    return t;
                })
                .toList();

        for (Tubo st : sueroTubos) {
            if (!st.getSuero().isActivo()) {
                throw new BusinessRuleException(
                        "El suero del tubo en posición " + st.getPosicion() + " no está activo");
            }
        }

        // Validate homogeneity
        int rango = sueroTubos.getFirst().getSuero().getRango();
        SueroUso uso = sueroTubos.getFirst().getSuero().getUso();

        if (sueroTubos.stream().anyMatch(st -> st.getSuero().getRango() != rango)) {
            throw new BusinessRuleException("Todos los tubos aportantes deben ser del mismo rango");
        }
        if (sueroTubos.stream().anyMatch(st -> st.getSuero().getUso() != uso)) {
            throw new BusinessRuleException("Todos los tubos aportantes deben ser del mismo tipo (CONTROL o PROBLEMA)");
        }

        // Validate available quantity per tubo
        Map<Long, Tubo> tuboById = sueroTubos.stream()
                .collect(Collectors.toMap(Tubo::getId, st -> st));
        for (SueroTuboAporteInputDTO a : dto.aportes()) {
            Tubo st = tuboById.get(a.sueroTuboId());
            if (a.cantidadAportada() > st.getCantidadRestante()) {
                throw new BusinessRuleException(
                        "El tubo " + st.getPosicion() + " no tiene suficiente volumen disponible. "
                        + "Disponible: " + st.getCantidadRestante() + " mL, solicitado: " + a.cantidadAportada() + " mL");
            }
        }

        // Validate invariant: Σ(aportes) == Σ(pool tubos)
        double totalAportes = dto.aportes().stream().mapToDouble(SueroTuboAporteInputDTO::cantidadAportada).sum();
        double totalPoolTubos = dto.tubos().stream().mapToDouble(TuboInputDTO::cantidadInicial).sum();
        if (Math.abs(totalAportes - totalPoolTubos) > 0.001) {
            throw new BusinessRuleException(
                    "La suma de aportes (" + totalAportes + " mL) debe ser igual a la suma de los tubos del pool ("
                    + totalPoolTubos + " mL)");
        }

        if (totalPoolTubos < ML_POR_RATON) {
            throw new BusinessRuleException(
                    "El pool debe tener al menos " + ML_POR_RATON + " mL (mínimo para un ratón)");
        }

        validarPosicionesPoolTubos(dto.cajaId(), dto.tubos(), null);

        // Create pool
        Pool pool = new Pool();
        pool.setCodigo(generarCodigo());
        pool.setCaja(caja);
        pool.setFechaCreacion(dto.fechaCreacion());
        pool.setRango(rango);
        pool.setUso(uso);
        Pool saved = repository.save(pool);

        // Create pool tubos
        for (TuboInputDTO t : dto.tubos()) {
            Tubo tubo = new Tubo();
            tubo.setTipo(TipoTubo.POOL);
            tubo.setCaja(caja);
            tubo.setPool(saved);
            tubo.setPosicion(t.posicion());
            tubo.setCantidadInicial(t.cantidadInicial());
            tuboRepository.save(tubo);
        }

        // Create aportes and update suero tubo usage
        for (SueroTuboAporteInputDTO a : dto.aportes()) {
            Tubo st = tuboById.get(a.sueroTuboId());

            PoolSueroAporte aporte = new PoolSueroAporte();
            aporte.setPool(saved);
            aporte.setTubo(st);
            aporte.setCantidadAportada(a.cantidadAportada());
            poolSueroAporteRepository.save(aporte);

            st.setCantidadUsada(st.getCantidadUsada() + a.cantidadAportada());
            tuboRepository.save(st);
        }

        return mapper.toDTO(repository.findById(saved.getId()).orElseThrow());
    }

    @Transactional
    public PoolResponseDTO update(Long id, PoolUpdateDTO dto) {
        Pool pool = findActiveById(id);

        Caja caja = cajaRepository.findById(dto.cajaId())
                .filter(Caja::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Caja con id " + dto.cajaId() + " no existe"));

        validarPosicionesPoolTubos(dto.cajaId(), dto.tubos(), id);

        Map<String, Tubo> existingByPosicion = tuboRepository.findByPoolId(id).stream()
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
                newTubo.setTipo(TipoTubo.POOL);
                newTubo.setCaja(caja);
                newTubo.setPool(pool);
                newTubo.setPosicion(t.posicion());
                newTubo.setCantidadInicial(t.cantidadInicial());
                tuboRepository.save(newTubo);
            }
        }

        pool.setCaja(caja);
        pool.setFechaCreacion(dto.fechaCreacion());

        return mapper.toDTO(repository.save(pool));
    }

    @Transactional
    public void delete(Long id) {
        Pool pool = findActiveById(id);
        pool.setActivo(false);
        repository.save(pool);
    }

    private void validarPosicionesPoolTubos(Long cajaId, List<TuboInputDTO> nuevos, Long excludePoolId) {
        Set<String> nuevasPos = nuevos.stream()
                .map(TuboInputDTO::posicion)
                .collect(Collectors.toSet());

        Set<String> ocupadasPool = tuboRepository.findByCajaIdAndPoolActivoTrue(cajaId).stream()
                .filter(t -> excludePoolId == null || !t.getPool().getId().equals(excludePoolId))
                .filter(t -> t.getCantidadRestante() > 0)
                .map(Tubo::getPosicion)
                .collect(Collectors.toSet());

        Set<String> conflictoPool = new HashSet<>(nuevasPos);
        conflictoPool.retainAll(ocupadasPool);
        if (!conflictoPool.isEmpty()) {
            throw new BusinessRuleException(
                    "Las posiciones " + String.join(", ", conflictoPool) + " ya están ocupadas en esta caja");
        }

        Set<String> ocupadasSuero = tuboRepository.findByCajaIdAndSueroActivoTrue(cajaId).stream()
                .filter(t -> t.getCantidadRestante() > 0)
                .map(Tubo::getPosicion)
                .collect(Collectors.toSet());

        Set<String> conflictoSuero = new HashSet<>(nuevasPos);
        conflictoSuero.retainAll(ocupadasSuero);
        if (!conflictoSuero.isEmpty()) {
            throw new BusinessRuleException(
                    "Las posiciones " + String.join(", ", conflictoSuero) + " ya están ocupadas por un suero en esta caja");
        }
    }

    private Specification<Pool> buildSpec(List<Integer> rangos, List<SueroUso> usos) {
        Specification<Pool> spec = activoTrue();
        if (rangos != null && !rangos.isEmpty()) spec = spec.and(rangoIn(rangos));
        if (usos   != null && !usos.isEmpty())   spec = spec.and(usoIn(usos));
        return spec;
    }

    private Specification<Pool> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Specification<Pool> rangoIn(List<Integer> rangos) {
        return (root, query, cb) -> root.get("rango").in(rangos);
    }

    private Specification<Pool> usoIn(List<SueroUso> usos) {
        return (root, query, cb) -> root.get("uso").in(usos);
    }

    private Sort buildSort(String sortBy, String sortDir, String defaultField) {
        String field = SORT_FIELDS_VALIDOS.contains(sortBy) ? sortBy : defaultField;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private String generarCodigo() {
        String codigo;
        do {
            codigo = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        } while (repository.existsByCodigo(codigo));
        return codigo;
    }

    private Pool findActiveById(Long id) {
        return repository.findById(id)
                .filter(Pool::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Pool con id " + id + " no existe"));
    }
}
