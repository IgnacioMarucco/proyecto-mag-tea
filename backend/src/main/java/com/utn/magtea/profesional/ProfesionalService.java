package com.utn.magtea.profesional;

import com.utn.magtea.common.exception.DuplicateResourceException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfesionalService {

    private final ProfesionalRepository repository;
    private final ProfesionalMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<ProfesionalResponseDTO> findAll() {
        return repository.findAllByActivoTrue().stream()
                .map(mapper::toDTO)
                .toList();
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
}
