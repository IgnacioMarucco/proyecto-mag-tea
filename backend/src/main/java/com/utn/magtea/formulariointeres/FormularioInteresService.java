package com.utn.magtea.formulariointeres;

import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FormularioInteresService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "fechaContacto", "apellidoTutor", "apellidoNino");

    private final FormularioInteresRepository repository;
    private final FormularioInteresMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<FormularioInteresResponseDTO> findAll(int page, int size, String q,
                                                              List<EstadoFormulario> estados,
                                                              String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir, "fechaContacto");
        Page<FormularioInteres> result = repository.findAll(buildSpec(q, estados), PageRequest.of(page, size, sort));
        return new PageResponse<>(
                result.map(mapper::toDTO).getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        );
    }

    @Transactional(readOnly = true)
    public FormularioInteresResponseDTO findById(Long id) {
        return mapper.toDTO(findActiveById(id));
    }

    @Transactional
    public FormularioInteresResponseDTO create(FormularioInteresCreateDTO dto) {
        FormularioInteres formulario = mapper.toEntity(dto);
        formulario.setFechaContacto(LocalDate.now());
        formulario.setEstado(EstadoFormulario.PENDIENTE);
        return mapper.toDTO(repository.save(formulario));
    }

    @Transactional
    public FormularioInteresResponseDTO update(Long id, FormularioInteresCreateDTO dto) {
        FormularioInteres formulario = findActiveById(id);
        if (formulario.getEstado() == EstadoFormulario.ADMITIDO ||
                formulario.getEstado() == EstadoFormulario.DESCARTADO) {
            throw new BusinessRuleException(
                    "No se puede modificar un formulario en estado " + formulario.getEstado());
        }
        formulario.setApellidoTutor(dto.apellidoTutor());
        formulario.setNombreTutor(dto.nombreTutor());
        formulario.setCorreoTutor(dto.correoTutor());
        formulario.setTelefono(dto.telefono());
        formulario.setApellidoNino(dto.apellidoNino());
        formulario.setNombreNino(dto.nombreNino());
        formulario.setFechaNacimientoNino(dto.fechaNacimientoNino());
        formulario.setComoConocioProyecto(dto.comoConocioProyecto());
        formulario.setOtroComoConocio(dto.otroComoConocio());
        formulario.setDiasDisponibles(dto.diasDisponibles());
        return mapper.toDTO(repository.save(formulario));
    }

    @Transactional
    public FormularioInteresResponseDTO cambiarEstado(Long id, EstadoUpdateDTO dto) {
        FormularioInteres formulario = findActiveById(id);
        EstadoFormulario actual = formulario.getEstado();
        EstadoFormulario nuevo  = dto.estado();

        boolean transicionValida = switch (actual) {
            case PENDIENTE  -> nuevo == EstadoFormulario.CONTACTADO || nuevo == EstadoFormulario.DESCARTADO;
            case CONTACTADO -> nuevo == EstadoFormulario.PENDIENTE  || nuevo == EstadoFormulario.DESCARTADO;
            default -> false;
        };

        if (!transicionValida) {
            throw new BusinessRuleException(
                    "Transición de estado no permitida: " + actual + " → " + nuevo);
        }

        formulario.setEstado(nuevo);
        return mapper.toDTO(repository.save(formulario));
    }

    // Llamado por PacienteService al crear un paciente desde un formulario admitido
    @Transactional
    public FormularioInteres admitir(Long id) {
        FormularioInteres formulario = findActiveById(id);
        if (formulario.getEstado() != EstadoFormulario.CONTACTADO) {
            throw new BusinessRuleException(
                    "Solo se puede admitir un formulario en estado CONTACTADO");
        }
        formulario.setEstado(EstadoFormulario.ADMITIDO);
        return repository.save(formulario);
    }

    @Transactional
    public void delete(Long id) {
        FormularioInteres formulario = findActiveById(id);
        formulario.setActivo(false);
        repository.save(formulario);
    }

    private FormularioInteres findActiveById(Long id) {
        return repository.findById(id)
                .filter(FormularioInteres::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Formulario de interés con id " + id + " no existe"));
    }

    private Specification<FormularioInteres> buildSpec(String q, List<EstadoFormulario> estados) {
        Specification<FormularioInteres> spec = activoTrue();
        if (q != null && !q.isBlank()) spec = spec.and(searchText(q));
        if (estados != null && !estados.isEmpty()) spec = spec.and(estadoIn(estados));
        return spec;
    }

    private Specification<FormularioInteres> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Specification<FormularioInteres> searchText(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombreTutor")), like),
                cb.like(cb.lower(root.get("apellidoTutor")), like),
                cb.like(cb.lower(root.get("nombreNino")), like),
                cb.like(cb.lower(root.get("apellidoNino")), like)
        );
    }

    private Specification<FormularioInteres> estadoIn(List<EstadoFormulario> estados) {
        return (root, query, cb) -> root.get("estado").in(estados);
    }

    private Sort buildSort(String sortBy, String sortDir, String defaultField) {
        String field = SORT_FIELDS_VALIDOS.contains(sortBy) ? sortBy : defaultField;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }
}
