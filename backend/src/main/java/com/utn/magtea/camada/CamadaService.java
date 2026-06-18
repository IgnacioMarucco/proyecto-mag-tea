package com.utn.magtea.camada;

import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.DuplicateResourceException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CamadaService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "nombre");

    private final CamadaRepository repository;
    private final CamadaMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<CamadaListDTO> findAll(int page, int size, String q,
                                               String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir, "nombre");
        Page<Camada> result = repository.findAll(buildSpec(q), PageRequest.of(page, size, sort));
        return new PageResponse<>(
                result.map(mapper::toListDTO).getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    @Transactional(readOnly = true)
    public CamadaResponseDTO findById(Long id) {
        return mapper.toDTO(findActiveById(id));
    }

    @Transactional
    public CamadaResponseDTO create(CamadaCreateDTO dto) {
        if (repository.existsByNombreAndActivoTrue(dto.nombre())) {
            throw new DuplicateResourceException("Ya existe una camada activa con el nombre \"" + dto.nombre() + "\"");
        }
        return mapper.toDTO(repository.save(mapper.toEntity(dto)));
    }

    @Transactional
    public CamadaResponseDTO update(Long id, CamadaCreateDTO dto) {
        Camada camada = findActiveById(id);
        if (repository.existsByNombreAndActivoTrueAndIdNot(dto.nombre(), id)) {
            throw new DuplicateResourceException("Ya existe una camada activa con el nombre \"" + dto.nombre() + "\"");
        }
        camada.setNombre(dto.nombre());
        return mapper.toDTO(repository.save(camada));
    }

    @Transactional
    public void delete(Long id) {
        Camada camada = findActiveById(id);
        camada.setActivo(false);
        repository.save(camada);
    }

    private Specification<Camada> buildSpec(String q) {
        Specification<Camada> spec = activoTrue();
        if (q != null && !q.isBlank()) spec = spec.and(searchText(q));
        return spec;
    }

    private Specification<Camada> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Specification<Camada> searchText(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("nombre")), like);
    }

    private Sort buildSort(String sortBy, String sortDir, String defaultField) {
        String field = SORT_FIELDS_VALIDOS.contains(sortBy) ? sortBy : defaultField;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private Camada findActiveById(Long id) {
        return repository.findById(id)
                .filter(Camada::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Camada con id " + id + " no existe"));
    }
}
