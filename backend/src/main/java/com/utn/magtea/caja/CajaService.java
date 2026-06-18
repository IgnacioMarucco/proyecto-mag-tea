package com.utn.magtea.caja;

import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.DuplicateResourceException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.pool.PoolRepository;
import com.utn.magtea.suero.SueroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CajaService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "freezer", "cajon", "numero");

    private final CajaRepository repository;
    private final CajaMapper mapper;
    private final SueroRepository sueroRepository;
    private final PoolRepository poolRepository;

    @Transactional(readOnly = true)
    public PageResponse<CajaListDTO> findAll(int page, int size, String freezer,
                                              String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir, "freezer");
        Page<Caja> result = repository.findAll(
                buildSpec(freezer),
                PageRequest.of(page, size, sort));
        return new PageResponse<>(
                result.map(mapper::toListDTO).getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Transactional(readOnly = true)
    public CajaResponseDTO findById(Long id) {
        return mapper.toDTO(findActiveById(id));
    }

    @Transactional
    public CajaResponseDTO create(CajaCreateDTO dto) {
        if (repository.existsByFreezerAndCajonAndNumeroAndActivoTrue(dto.freezer(), dto.cajon(), dto.numero())) {
            throw new DuplicateResourceException(
                    "Ya existe una caja activa en freezer " + dto.freezer()
                    + ", cajón " + dto.cajon() + ", número " + dto.numero());
        }
        Caja caja = mapper.toEntity(dto);
        return mapper.toDTO(repository.save(caja));
    }

    @Transactional
    public CajaResponseDTO update(Long id, CajaCreateDTO dto) {
        Caja caja = findActiveById(id);
        if (repository.existsByFreezerAndCajonAndNumeroAndActivoTrueAndIdNot(dto.freezer(), dto.cajon(), dto.numero(), id)) {
            throw new DuplicateResourceException(
                    "Ya existe una caja activa en freezer " + dto.freezer()
                    + ", cajón " + dto.cajon() + ", número " + dto.numero());
        }
        caja.setFreezer(dto.freezer());
        caja.setCajon(dto.cajon());
        caja.setNumero(dto.numero());
        return mapper.toDTO(repository.save(caja));
    }

    @Transactional
    public void delete(Long id) {
        Caja caja = findActiveById(id);
        caja.setActivo(false);
        repository.save(caja);
    }

    @Transactional(readOnly = true)
    public CajaOcupacionDTO getOcupacion(Long id) {
        findActiveById(id);

        java.util.stream.Stream<String> tubosDesueros = sueroRepository.findByCajaIdAndActivoTrue(id).stream()
                .filter(s -> s.getTubos() != null && !s.getTubos().isBlank())
                .flatMap(s -> Arrays.stream(s.getTubos().split(",")));

        java.util.stream.Stream<String> tubosDePools = poolRepository.findByCajaIdAndActivoTrue(id).stream()
                .filter(p -> p.getTubos() != null && !p.getTubos().isBlank())
                .flatMap(p -> Arrays.stream(p.getTubos().split(",")));

        List<String> ocupadas = java.util.stream.Stream.concat(tubosDesueros, tubosDePools)
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .distinct()
                .sorted()
                .toList();

        return new CajaOcupacionDTO(ocupadas);
    }

    private Specification<Caja> buildSpec(String freezer) {
        Specification<Caja> spec = activoTrue();
        if (freezer != null && !freezer.isBlank()) spec = spec.and(freezerEquals(freezer));
        return spec;
    }

    private Specification<Caja> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Specification<Caja> freezerEquals(String freezer) {
        return (root, query, cb) -> cb.equal(cb.upper(root.get("freezer")), freezer.toUpperCase());
    }

    private Sort buildSort(String sortBy, String sortDir, String defaultField) {
        String field = SORT_FIELDS_VALIDOS.contains(sortBy) ? sortBy : defaultField;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private Caja findActiveById(Long id) {
        return repository.findById(id)
                .filter(Caja::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Caja con id " + id + " no existe"));
    }
}
