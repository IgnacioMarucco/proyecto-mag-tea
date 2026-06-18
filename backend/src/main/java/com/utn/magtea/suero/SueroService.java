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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SueroService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "fechaExtraccion", "rango");

    private final SueroRepository repository;
    private final SueroMapper mapper;
    private final PacienteRepository pacienteRepository;
    private final CajaRepository cajaRepository;

    @Transactional(readOnly = true)
    public PageResponse<SueroListDTO> findAll(int page, int size, List<Integer> rangos,
                                              SueroUso uso, String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir, "createdAt");
        Page<Suero> result = repository.findAll(
                buildSpec(rangos, uso),
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

    @Transactional
    public SueroResponseDTO create(SueroCreateDTO dto) {
        Paciente paciente = pacienteRepository.findById(dto.pacienteId())
                .filter(Paciente::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente con id " + dto.pacienteId() + " no existe"));

        if (repository.existsByPacienteIdAndActivoTrue(dto.pacienteId())) {
            throw new BusinessRuleException("El paciente ya tiene un suero registrado");
        }

        if (paciente.getTipoPaciente() == TipoPaciente.CONTROL && dto.valorAnticuerpos() != 0.0) {
            throw new BusinessRuleException("Los pacientes caso control deben tener valor de anticuerpos igual a 0");
        }

        Caja caja = cajaRepository.findById(dto.cajaId())
                .filter(Caja::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Caja con id " + dto.cajaId() + " no existe"));

        Suero suero = mapper.toEntity(dto);
        suero.setPaciente(paciente);
        suero.setCaja(caja);
        suero.setRango(SueroRangoUtil.calcularRango(dto.valorAnticuerpos()));
        suero.setCantidadUsada(0.0);

        paciente.setEstadoClinico(PacienteEstado.EXTRACCION_REALIZADA);
        pacienteRepository.save(paciente);

        return mapper.toDTO(repository.save(suero));
    }

    @Transactional
    public SueroResponseDTO update(Long id, SueroCreateDTO dto) {
        Suero suero = findActiveById(id);

        Caja caja = cajaRepository.findById(dto.cajaId())
                .filter(Caja::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Caja con id " + dto.cajaId() + " no existe"));

        suero.setCaja(caja);
        suero.setTubos(dto.tubos());
        suero.setFechaExtraccion(dto.fechaExtraccion());
        suero.setCantidadTotal(dto.cantidadTotal());
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
        List<Suero> sueros = repository.findAll(activoTrue().and(rangoMayorACero()));

        Map<Integer, List<Suero>> porRango = sueros.stream()
                .collect(Collectors.groupingBy(Suero::getRango));

        List<SueroDisponibilidadDTO> result = new ArrayList<>();
        for (int rango = 1; rango <= 3; rango++) {
            List<Suero> grupo = porRango.getOrDefault(rango, List.of()).stream()
                    .filter(s -> s.getCantidadTotal() > s.getCantidadUsada())
                    .toList();
            double mlDisponibles = grupo.stream()
                    .mapToDouble(s -> s.getCantidadTotal() - s.getCantidadUsada())
                    .sum();
            int ratonesPosibles = (int) Math.floor(mlDisponibles / 0.2);
            result.add(new SueroDisponibilidadDTO(rango, (long) grupo.size(), mlDisponibles, ratonesPosibles));
        }
        return result;
    }

    private Specification<Suero> buildSpec(List<Integer> rangos, SueroUso uso) {
        Specification<Suero> spec = activoTrue();
        if (rangos != null && !rangos.isEmpty()) spec = spec.and(rangoIn(rangos));
        if (uso != null) spec = spec.and(usoEquals(uso));
        return spec;
    }

    private Specification<Suero> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Specification<Suero> rangoIn(List<Integer> rangos) {
        return (root, query, cb) -> root.get("rango").in(rangos);
    }

    private Specification<Suero> usoEquals(SueroUso uso) {
        return (root, query, cb) -> uso == SueroUso.CONTROL
                ? cb.equal(root.get("valorAnticuerpos"), 0.0)
                : cb.greaterThan(root.get("valorAnticuerpos"), 0.0);
    }

    private Specification<Suero> rangoMayorACero() {
        return (root, query, cb) -> cb.greaterThan(root.get("rango"), 0);
    }

    private Sort buildSort(String sortBy, String sortDir, String defaultField) {
        String field = SORT_FIELDS_VALIDOS.contains(sortBy) ? sortBy : defaultField;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private Suero findActiveById(Long id) {
        return repository.findById(id)
                .filter(Suero::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Suero con id " + id + " no existe"));
    }
}
