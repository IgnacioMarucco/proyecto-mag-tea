package com.utn.magtea.profesional;

import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.DuplicateResourceException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfesionalService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("apellido", "nombre", "email", "createdAt");

    private final ProfesionalRepository repository;
    private final ProfesionalMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public PageResponse<ProfesionalResponseDTO> findAll(int page, int size, String q, Role rol,
                                                        String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir, "apellido");
        Page<Profesional> result = repository.findAll(buildSpec(q, rol), PageRequest.of(page, size, sort));
        return new PageResponse<>(
                result.map(mapper::toDTO).getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        );
    }

    @Transactional(readOnly = true)
    public ProfesionalResponseDTO findById(Long id) {
        return mapper.toDTO(findActiveById(id));
    }

    @Transactional
    public ProfesionalResponseDTO create(ProfesionalCreateDTO dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("Email ya registrado: " + dto.email());
        }
        Profesional profesional = mapper.toEntity(dto);
        profesional.setPassword(passwordEncoder.encode(dto.password()));
        return mapper.toDTO(repository.save(profesional));
    }

    @Transactional
    public ProfesionalResponseDTO update(Long id, ProfesionalUpdateDTO dto) {
        Profesional profesional = findActiveById(id);
        if (!profesional.getEmail().equals(dto.email()) && repository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("Email ya registrado: " + dto.email());
        }
        profesional.setNombre(dto.nombre());
        profesional.setApellido(dto.apellido());
        profesional.setEmail(dto.email());
        profesional.setRole(dto.role());
        return mapper.toDTO(repository.save(profesional));
    }

    @Transactional
    public void delete(Long id) {
        Profesional profesional = findActiveById(id);
        profesional.setActivo(false);
        repository.save(profesional);
    }

    private Profesional findActiveById(Long id) {
        return repository.findById(id)
                .filter(Profesional::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional con id " + id + " no existe"));
    }

    private Specification<Profesional> buildSpec(String q, Role rol) {
        Specification<Profesional> spec = activoTrue();
        if (q != null && !q.isBlank()) spec = spec.and(searchText(q));
        if (rol != null) spec = spec.and(rolEquals(rol));
        return spec;
    }

    private Specification<Profesional> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Specification<Profesional> searchText(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombre")), like),
                cb.like(cb.lower(root.get("apellido")), like),
                cb.like(cb.lower(root.get("email")), like)
        );
    }

    private Specification<Profesional> rolEquals(Role rol) {
        return (root, query, cb) -> cb.equal(root.get("role"), rol);
    }

    private Sort buildSort(String sortBy, String sortDir, String defaultField) {
        String field = SORT_FIELDS_VALIDOS.contains(sortBy) ? sortBy : defaultField;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }
}
