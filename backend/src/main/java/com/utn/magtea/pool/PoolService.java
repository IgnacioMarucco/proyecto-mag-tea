package com.utn.magtea.pool;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.caja.CajaRepository;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.suero.Suero;
import com.utn.magtea.suero.SueroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PoolService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "fechaCreacion", "rango");
    private static final double ML_POR_RATON = 0.2;

    private final PoolRepository repository;
    private final PoolMapper mapper;
    private final SueroRepository sueroRepository;
    private final CajaRepository cajaRepository;

    @Transactional(readOnly = true)
    public PageResponse<PoolListDTO> findAll(int page, int size, List<Integer> rangos,
                                             String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir, "createdAt");
        Page<Pool> result = repository.findAll(buildSpec(rangos), PageRequest.of(page, size, sort));
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

        List<Suero> sueros = dto.sueros().stream()
                .map(aporte -> sueroRepository.findById(aporte.sueroId())
                        .filter(Suero::isActivo)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Suero con id " + aporte.sueroId() + " no existe")))
                .toList();

        int rango = sueros.getFirst().getRango();

        if (rango == 0) {
            throw new BusinessRuleException("Los sueros caso control (rango 0) no pueden formar un pool");
        }

        boolean rangosHeterogeneos = sueros.stream().anyMatch(s -> s.getRango() != rango);
        if (rangosHeterogeneos) {
            throw new BusinessRuleException("Todos los sueros del pool deben ser del mismo rango");
        }

        for (SueroAportDTO aporte : dto.sueros()) {
            Suero suero = sueros.stream()
                    .filter(s -> s.getId().equals(aporte.sueroId()))
                    .findFirst()
                    .orElseThrow();
            double disponible = suero.getCantidadTotal() - suero.getCantidadUsada();
            if (aporte.cantidadAportada() > disponible) {
                throw new BusinessRuleException(
                        "El suero " + aporte.sueroId() + " no tiene suficiente volumen disponible. "
                        + "Disponible: " + disponible + " mL, solicitado: " + aporte.cantidadAportada() + " mL");
            }
        }

        double cantidadTotal = dto.sueros().stream()
                .mapToDouble(SueroAportDTO::cantidadAportada)
                .sum();

        if (cantidadTotal < ML_POR_RATON) {
            throw new BusinessRuleException(
                    "El pool debe tener al menos " + ML_POR_RATON + " mL (mínimo para un ratón)");
        }

        for (SueroAportDTO aporte : dto.sueros()) {
            Suero suero = sueros.stream()
                    .filter(s -> s.getId().equals(aporte.sueroId()))
                    .findFirst()
                    .orElseThrow();
            suero.setCantidadUsada(suero.getCantidadUsada() + aporte.cantidadAportada());
            sueroRepository.save(suero);
        }

        Pool pool = new Pool();
        pool.setCaja(caja);
        pool.setTubos(dto.tubos());
        pool.setFechaCreacion(dto.fechaCreacion());
        pool.setRango(rango);
        pool.setCantidadTotal(cantidadTotal);
        pool.setCantidadUsada(0.0);
        pool.setSueros(sueros);

        return mapper.toDTO(repository.save(pool));
    }

    @Transactional
    public PoolResponseDTO update(Long id, PoolUpdateDTO dto) {
        Pool pool = findActiveById(id);

        Caja caja = cajaRepository.findById(dto.cajaId())
                .filter(Caja::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Caja con id " + dto.cajaId() + " no existe"));

        pool.setCaja(caja);
        pool.setTubos(dto.tubos());
        pool.setFechaCreacion(dto.fechaCreacion());

        return mapper.toDTO(repository.save(pool));
    }

    @Transactional
    public void delete(Long id) {
        Pool pool = findActiveById(id);
        pool.setActivo(false);
        repository.save(pool);
    }

    private Specification<Pool> buildSpec(List<Integer> rangos) {
        Specification<Pool> spec = activoTrue();
        if (rangos != null && !rangos.isEmpty()) spec = spec.and(rangoIn(rangos));
        return spec;
    }

    private Specification<Pool> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Specification<Pool> rangoIn(List<Integer> rangos) {
        return (root, query, cb) -> root.get("rango").in(rangos);
    }

    private Sort buildSort(String sortBy, String sortDir, String defaultField) {
        String field = SORT_FIELDS_VALIDOS.contains(sortBy) ? sortBy : defaultField;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private Pool findActiveById(Long id) {
        return repository.findById(id)
                .filter(Pool::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Pool con id " + id + " no existe"));
    }
}
