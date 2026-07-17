package com.utn.magtea.pool;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.caja.CajaRepository;
import com.utn.magtea.common.DomainConstants;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.SpecificationUtils;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.modeloanimal.EstadoProtocolo;
import com.utn.magtea.modeloanimal.ModeloAnimal;
import com.utn.magtea.suero.SueroUso;
import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TipoTubo;
import com.utn.magtea.tubo.TuboInputDTO;
import com.utn.magtea.tubo.TuboRepository;
import com.utn.magtea.tubo.TuboService;
import com.utn.magtea.tubo.VaciarTuboRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.utn.magtea.common.CodigoUtil;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoolService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "fechaCreacion", "rango");

    private final PoolRepository repository;
    private final PoolMapper mapper;
    private final TuboRepository tuboRepository;
    private final PoolSueroAporteRepository poolSueroAporteRepository;
    private final CajaRepository cajaRepository;
    private final TuboService tuboService;

    @Transactional(readOnly = true)
    public PageResponse<PoolListDTO> findAll(int page, int size, String q, List<Integer> rangos, List<SueroUso> usos,
                                             String sortBy, String sortDir) {
        Sort sort = SpecificationUtils.buildSort(sortBy, sortDir, "createdAt", SORT_FIELDS_VALIDOS);
        Page<Pool> result = repository.findAll(buildSpec(q, rangos, usos), PageRequest.of(page, size, sort));
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

    @Transactional(readOnly = true)
    public PoolResponseDTO findByCodigo(String codigo) {
        Pool pool = repository.findByCodigoAndActivoTrue(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Pool con código " + codigo + " no existe"));
        return mapper.toDTO(pool);
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
            if (a.cantidadAportada().compareTo(st.getCantidadRestante()) > 0) {
                throw new BusinessRuleException(
                        "El tubo " + st.getPosicion() + " no tiene suficiente volumen disponible. "
                        + "Disponible: " + st.getCantidadRestante() + " mL, solicitado: " + a.cantidadAportada() + " mL");
            }
        }

        // Validate invariant: Σ(aportes) == Σ(pool tubos)
        BigDecimal totalAportes = dto.aportes().stream()
                .map(SueroTuboAporteInputDTO::cantidadAportada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPoolTubos = dto.tubos().stream()
                .map(TuboInputDTO::cantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalAportes.compareTo(totalPoolTubos) != 0) {
            throw new BusinessRuleException(
                    "La suma de aportes (" + totalAportes + " mL) debe ser igual a la suma de los tubos del pool ("
                    + totalPoolTubos + " mL)");
        }

        if (totalPoolTubos.compareTo(DomainConstants.ML_POR_RATON) < 0) {
            throw new BusinessRuleException(
                    "El pool debe tener al menos " + DomainConstants.ML_POR_RATON + " mL (mínimo para un ratón)");
        }

        Set<Long> sueroTubosAgotados = dto.aportes().stream()
                .filter(a -> {
                    Tubo st = tuboById.get(a.sueroTuboId());
                    return st != null && a.cantidadAportada().compareTo(st.getCantidadRestante()) == 0;
                })
                .map(SueroTuboAporteInputDTO::sueroTuboId)
                .collect(Collectors.toSet());
        tuboService.validarPosicionesSinConflicto(dto.cajaId(), dto.tubos(), null, null, sueroTubosAgotados);

        // Create pool
        Pool pool = new Pool();
        pool.setCodigo(generarCodigo());
        pool.setCaja(caja);
        pool.setFechaCreacion(LocalDate.now());
        pool.setRango(rango);
        pool.setUso(uso);
        Pool saved = repository.save(pool);

        // El constraint UNIQUE(caja_id, posicion) se evalúa por statement en PostgreSQL;
        // hay que vaciar las posiciones liberadas antes de insertar los tubos del pool.
        List<Tubo> tubosALiberar = sueroTubosAgotados.stream()
                .map(tuboById::get)
                .filter(Objects::nonNull)
                .peek(st -> st.setPosicion(null))
                .toList();
        if (!tubosALiberar.isEmpty()) {
            tuboRepository.saveAll(tubosALiberar);
            tuboRepository.flush();
        }

        // Create pool tubos
        for (TuboInputDTO t : dto.tubos()) {
            Tubo tubo = new Tubo();
            tubo.setTipo(TipoTubo.POOL);
            tubo.setCaja(caja);
            tubo.setPool(saved);
            tubo.setPosicion(t.posicion());
            tubo.setCantidadInicial(t.cantidad());
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

            st.setCantidadUsada(st.getCantidadUsada().add(a.cantidadAportada()));
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

        tuboService.validarPosicionesSinConflicto(dto.cajaId(), dto.tubos(), null, id, Set.of());

        Map<String, Tubo> existingByPosicion = tuboRepository.findByPoolId(id).stream()
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

        for (Tubo t : existingByPosicion.values()) {
            if (!newPosiciones.contains(t.getPosicion())) {
                pool.getTubos().remove(t);
            }
        }

        for (TuboInputDTO t : dto.tubos()) {
            Tubo existing = existingByPosicion.get(t.posicion());
            if (existing != null) {
                existing.setCaja(caja);
                // El input es la cantidad ACTUAL deseada: preservamos lo ya consumido y
                // recalculamos la inicial para que el restante quede en t.cantidad().
                existing.setCantidadInicial(t.cantidad().add(existing.getCantidadUsada()));
                tuboRepository.save(existing);
            } else {
                Tubo newTubo = new Tubo();
                newTubo.setTipo(TipoTubo.POOL);
                newTubo.setCaja(caja);
                newTubo.setPool(pool);
                newTubo.setPosicion(t.posicion());
                newTubo.setCantidadInicial(t.cantidad());
                tuboRepository.save(newTubo);
                pool.getTubos().add(newTubo);
            }
        }

        pool.setCaja(caja);
        pool.setFechaCreacion(dto.fechaCreacion());

        return mapper.toDTO(repository.save(pool));
    }

    @Transactional
    public void delete(Long id) {
        Pool pool = findActiveById(id);
        long modelosActivos = pool.getModelosAnimales().stream()
                .filter(m -> m.isActivo() && m.getEstadoProtocolo() != EstadoProtocolo.COMPLETO)
                .count();
        if (modelosActivos > 0)
            throw new BusinessRuleException(
                    "El pool tiene " + modelosActivos + " modelo(s) animal(es) en curso y no puede darse de baja");
        for (Tubo tubo : pool.getTubos()) {
            tubo.setPosicion(null);
            tuboRepository.save(tubo);
        }
        pool.setActivo(false);
        repository.save(pool);
    }

    @Transactional
    public PoolResponseDTO liberarGrilla(Long id, VaciarTuboRequest req) {
        Pool pool = findActiveById(id);
        pool.getTubos().forEach(tubo -> tuboService.vaciar(tubo.getId(), req));
        return mapper.toDTO(repository.findById(id).orElseThrow());
    }

    private Specification<Pool> buildSpec(String q, List<Integer> rangos, List<SueroUso> usos) {
        Specification<Pool> spec = SpecificationUtils.activoTrue();
        if (q      != null && !q.isBlank())       spec = spec.and(searchText(q));
        if (rangos != null && !rangos.isEmpty())   spec = spec.and(rangoIn(rangos));
        if (usos   != null && !usos.isEmpty())     spec = spec.and(usoIn(usos));
        return spec;
    }

    private Specification<Pool> searchText(String q) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("codigo")), "%" + q.toLowerCase() + "%");
    }

    private Specification<Pool> rangoIn(List<Integer> rangos) {
        return (root, query, cb) -> root.get("rango").in(rangos);
    }

    private Specification<Pool> usoIn(List<SueroUso> usos) {
        return (root, query, cb) -> root.get("uso").in(usos);
    }

    private String generarCodigo() {
        return CodigoUtil.generarCodigo(repository::existsByCodigo);
    }

    private Pool findActiveById(Long id) {
        return repository.findById(id)
                .filter(Pool::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Pool con id " + id + " no existe"));
    }
}
