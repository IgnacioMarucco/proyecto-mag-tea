package com.utn.magtea.caja;

import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.DuplicateResourceException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TuboRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CajaService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "freezer", "cajon", "numero");

    private final CajaRepository repository;
    private final CajaMapper mapper;
    private final TuboRepository tuboRepository;

    @Transactional(readOnly = true)
    public PageResponse<CajaListDTO> findAll(int page, int size, String q, String freezer,
                                              String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir, "freezer");
        Page<Caja> result = repository.findAll(
                buildSpec(q, freezer),
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
    public CajaOcupacionDTO getOcupacion(Long id, Long excludeSueroId) {
        findActiveById(id);

        Stream<String> tubosSueros = tuboRepository.findByCajaIdAndSueroActivoTrue(id).stream()
                .filter(t -> excludeSueroId == null || !t.getSuero().getId().equals(excludeSueroId))
                .filter(t -> t.getCantidadRestante() > 0)
                .map(Tubo::getPosicion);

        Stream<String> tubosPools = tuboRepository.findByCajaIdAndPoolActivoTrue(id).stream()
                .filter(t -> t.getCantidadRestante() > 0)
                .map(Tubo::getPosicion);

        List<String> ocupadas = Stream.concat(tubosSueros, tubosPools)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return new CajaOcupacionDTO(ocupadas);
    }

    private Specification<Caja> buildSpec(String q, String freezer) {
        Specification<Caja> spec = activoTrue();
        if (q != null && !q.isBlank())             spec = spec.and(searchText(q));
        if (freezer != null && !freezer.isBlank()) spec = spec.and(freezerEquals(freezer));
        return spec;
    }

    private Specification<Caja> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Specification<Caja> searchText(String q) {
        return (root, query, cb) -> cb.like(
                cb.upper(root.get("freezer")), "%" + q.toUpperCase() + "%");
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
