package com.utn.magtea.modeloanimal;

import com.utn.magtea.camada.Camada;
import com.utn.magtea.camada.CamadaRepository;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.modeloanimal.estudios.TresCamaras;
import com.utn.magtea.modeloanimal.estudios.TresCamarasDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesUltrasonicas;
import com.utn.magtea.pool.Pool;
import com.utn.magtea.pool.PoolRepository;
import com.utn.magtea.tubo.Tubo;
import com.utn.magtea.tubo.TipoTubo;
import com.utn.magtea.tubo.TuboRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ModeloAnimalService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "fechaNacimiento", "identificador");

    private final ModeloAnimalRepository repository;
    private final ModeloAnimalMapper mapper;
    private final PoolRepository poolRepository;
    private final TuboRepository tuboRepository;
    private final ModeloAnimalPoolAporteRepository modeloAnimalPoolAporteRepository;
    private final CamadaRepository camadaRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PageResponse<ModeloAnimalListDTO> findAll(int page, int size, Long poolId,
                                                     SexoRaton sexo, String sortBy, String sortDir) {
        Sort sort = buildSort(sortBy, sortDir, "createdAt");
        Page<ModeloAnimal> result = repository.findAll(
                buildSpec(poolId, sexo), PageRequest.of(page, size, sort));
        LocalDate hoy = LocalDate.now(clock);
        List<ModeloAnimalListDTO> content = result.getContent().stream()
                .map(m -> mapper.toListDTO(m,
                        calcularNecesitaVocalizaciones(m, hoy),
                        calcularNecesitaTresCamaras(m, hoy)))
                .toList();
        return new PageResponse<>(content, result.getTotalElements(),
                result.getTotalPages(), result.getNumber(), result.getSize());
    }

    @Transactional(readOnly = true)
    public ModeloAnimalResponseDTO findById(Long id) {
        ModeloAnimal m = findActiveById(id);
        LocalDate hoy = LocalDate.now(clock);
        VocalizacionesDTO vusDTO = m.getVocalizaciones() != null
                ? mapper.toVocalizacionesDTO(m.getVocalizaciones()) : null;
        TresCamarasDTO tcDTO = m.getTresCamaras() != null
                ? mapper.toTresCamarasDTO(m.getTresCamaras()) : null;
        return mapper.toDTO(m,
                calcularNecesitaVocalizaciones(m, hoy),
                calcularNecesitaTresCamaras(m, hoy),
                vusDTO, tcDTO);
    }

    @Transactional
    public ModeloAnimalResponseDTO create(ModeloAnimalCreateDTO dto) {
        Pool pool = poolRepository.findById(dto.poolId())
                .filter(Pool::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Pool con id " + dto.poolId() + " no existe"));

        Camada camada = camadaRepository.findById(dto.camadaId())
                .filter(Camada::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Camada con id " + dto.camadaId() + " no existe"));

        ModeloAnimal m = mapper.toEntity(dto);
        m.setPool(pool);
        m.setCamada(camada);
        ModeloAnimal saved = repository.save(m);

        if (dto.aportes() != null && !dto.aportes().isEmpty()) {
            for (ModeloAnimalPoolAporteInputDTO a : dto.aportes()) {
                Tubo tubo = tuboRepository.findById(a.poolTuboId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Tubo con id " + a.poolTuboId() + " no existe"));

                if (tubo.getTipo() != TipoTubo.POOL) {
                    throw new BusinessRuleException(
                            "El tubo en posición " + tubo.getPosicion() + " no es un tubo de pool");
                }

                if (!tubo.getPool().getId().equals(dto.poolId())) {
                    throw new BusinessRuleException(
                            "El tubo " + tubo.getPosicion() + " no pertenece al pool indicado");
                }

                ModeloAnimalPoolAporte aporte = new ModeloAnimalPoolAporte();
                aporte.setModeloAnimal(saved);
                aporte.setTubo(tubo);
                aporte.setCantidadConsumida(a.cantidadConsumida());
                aporte.setDia(a.dia());
                modeloAnimalPoolAporteRepository.save(aporte);

                if (a.cantidadConsumida() != null) {
                    tubo.setCantidadUsada(tubo.getCantidadUsada() + a.cantidadConsumida());
                    tuboRepository.save(tubo);
                }
            }
        }

        LocalDate hoy = LocalDate.now(clock);
        ModeloAnimal refreshed = repository.findById(saved.getId()).orElseThrow();
        return mapper.toDTO(refreshed,
                calcularNecesitaVocalizaciones(refreshed, hoy),
                calcularNecesitaTresCamaras(refreshed, hoy),
                null, null);
    }

    @Transactional
    public ModeloAnimalResponseDTO update(Long id, ModeloAnimalCreateDTO dto) {
        ModeloAnimal m = findActiveById(id);

        Pool pool = poolRepository.findById(dto.poolId())
                .filter(Pool::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Pool con id " + dto.poolId() + " no existe"));

        Camada camada = camadaRepository.findById(dto.camadaId())
                .filter(Camada::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Camada con id " + dto.camadaId() + " no existe"));

        m.setIdentificador(dto.identificador());
        m.setPool(pool);
        m.setCamada(camada);
        m.setFechaNacimiento(dto.fechaNacimiento());
        m.setSexo(dto.sexo());
        m.setFechaDia1Inoculacion(dto.fechaDia1Inoculacion());

        LocalDate hoy = LocalDate.now(clock);
        ModeloAnimal saved = repository.save(m);
        VocalizacionesDTO vusDTO = saved.getVocalizaciones() != null
                ? mapper.toVocalizacionesDTO(saved.getVocalizaciones()) : null;
        TresCamarasDTO tcDTO = saved.getTresCamaras() != null
                ? mapper.toTresCamarasDTO(saved.getTresCamaras()) : null;
        return mapper.toDTO(saved,
                calcularNecesitaVocalizaciones(saved, hoy),
                calcularNecesitaTresCamaras(saved, hoy),
                vusDTO, tcDTO);
    }

    @Transactional
    public ModeloAnimalResponseDTO registrarVocalizaciones(Long id, VocalizacionesDTO dto) {
        ModeloAnimal m = findActiveById(id);

        VocalizacionesUltrasonicas vus = m.getVocalizaciones() != null
                ? m.getVocalizaciones()
                : new VocalizacionesUltrasonicas();
        vus.setModeloAnimal(m);
        vus.setMuestra1Khz(dto.muestra1Khz());
        vus.setMuestra2Khz(dto.muestra2Khz());
        m.setVocalizaciones(vus);

        LocalDate hoy = LocalDate.now(clock);
        ModeloAnimal saved = repository.save(m);
        TresCamarasDTO tcDTO = saved.getTresCamaras() != null
                ? mapper.toTresCamarasDTO(saved.getTresCamaras()) : null;
        return mapper.toDTO(saved,
                calcularNecesitaVocalizaciones(saved, hoy),
                calcularNecesitaTresCamaras(saved, hoy),
                mapper.toVocalizacionesDTO(saved.getVocalizaciones()), tcDTO);
    }

    @Transactional
    public ModeloAnimalResponseDTO registrarTresCamaras(Long id, TresCamarasDTO dto) {
        ModeloAnimal m = findActiveById(id);

        TresCamaras tc = m.getTresCamaras() != null
                ? m.getTresCamaras()
                : new TresCamaras();
        tc.setModeloAnimal(m);
        tc.setM1TiempoRatonNovedad(dto.m1TiempoRatonNovedad());
        tc.setM1TiempoObjetoNovedoso(dto.m1TiempoObjetoNovedoso());
        tc.setM2TiempoRatonDesconocido(dto.m2TiempoRatonDesconocido());
        tc.setM2TiempoRatonFamiliar(dto.m2TiempoRatonFamiliar());
        m.setTresCamaras(tc);

        LocalDate hoy = LocalDate.now(clock);
        ModeloAnimal saved = repository.save(m);
        VocalizacionesDTO vusDTO = saved.getVocalizaciones() != null
                ? mapper.toVocalizacionesDTO(saved.getVocalizaciones()) : null;
        return mapper.toDTO(saved,
                calcularNecesitaVocalizaciones(saved, hoy),
                calcularNecesitaTresCamaras(saved, hoy),
                vusDTO, mapper.toTresCamarasDTO(saved.getTresCamaras()));
    }

    @Transactional
    public ModeloAnimalResponseDTO registrarMicroscopia(Long id, ModeloAnimalMicroscopiaDTO dto) {
        ModeloAnimal m = findActiveById(id);
        m.setNumCelulasGanglionares(dto.numCelulasGanglionares());
        m.setNumCelulasPurkinje(dto.numCelulasPurkinje());

        LocalDate hoy = LocalDate.now(clock);
        ModeloAnimal saved = repository.save(m);
        VocalizacionesDTO vusDTO = saved.getVocalizaciones() != null
                ? mapper.toVocalizacionesDTO(saved.getVocalizaciones()) : null;
        TresCamarasDTO tcDTO = saved.getTresCamaras() != null
                ? mapper.toTresCamarasDTO(saved.getTresCamaras()) : null;
        return mapper.toDTO(saved,
                calcularNecesitaVocalizaciones(saved, hoy),
                calcularNecesitaTresCamaras(saved, hoy),
                vusDTO, tcDTO);
    }

    @Transactional
    public void delete(Long id) {
        ModeloAnimal m = findActiveById(id);
        m.setActivo(false);
        repository.save(m);
    }

    private boolean calcularNecesitaVocalizaciones(ModeloAnimal m, LocalDate hoy) {
        return m.getVocalizaciones() == null
                && m.getFechaNacimiento() != null
                && m.getFechaNacimiento().plusDays(5).equals(hoy);
    }

    private boolean calcularNecesitaTresCamaras(ModeloAnimal m, LocalDate hoy) {
        return m.getTresCamaras() == null
                && m.getFechaNacimiento() != null
                && m.getFechaNacimiento().plusDays(19).equals(hoy);
    }

    private Specification<ModeloAnimal> buildSpec(Long poolId, SexoRaton sexo) {
        Specification<ModeloAnimal> spec = activoTrue();
        if (poolId != null) spec = spec.and(poolEquals(poolId));
        if (sexo != null) spec = spec.and(sexoEquals(sexo));
        return spec;
    }

    private Specification<ModeloAnimal> activoTrue() {
        return (root, query, cb) -> cb.isTrue(root.get("activo"));
    }

    private Specification<ModeloAnimal> poolEquals(Long poolId) {
        return (root, query, cb) -> cb.equal(root.get("pool").get("id"), poolId);
    }

    private Specification<ModeloAnimal> sexoEquals(SexoRaton sexo) {
        return (root, query, cb) -> cb.equal(root.get("sexo"), sexo);
    }

    private Sort buildSort(String sortBy, String sortDir, String defaultField) {
        String field = SORT_FIELDS_VALIDOS.contains(sortBy) ? sortBy : defaultField;
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }

    private ModeloAnimal findActiveById(Long id) {
        return repository.findById(id)
                .filter(ModeloAnimal::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo animal con id " + id + " no existe"));
    }
}
