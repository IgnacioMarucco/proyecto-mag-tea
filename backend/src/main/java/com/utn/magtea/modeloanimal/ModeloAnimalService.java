package com.utn.magtea.modeloanimal;

import com.utn.magtea.camada.Camada;
import com.utn.magtea.camada.CamadaRepository;
import com.utn.magtea.common.PageResponse;
import com.utn.magtea.common.SpecificationUtils;
import com.utn.magtea.suero.SueroUso;
import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.common.exception.ResourceNotFoundException;
import com.utn.magtea.modeloanimal.estudios.TresCamaras;
import com.utn.magtea.modeloanimal.estudios.TresCamarasDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesUltrasonicas;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.cars.CarsItemsResponseDTO;
import com.utn.magtea.paciente.cars.CarsResultado;
import com.utn.magtea.paciente.cars.EvaluacionCars;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatRiesgo;
import com.utn.magtea.paciente.mchat.MchatSeguimiento;
import com.utn.magtea.paciente.vineland.EvaluacionVineland;
import com.utn.magtea.suero.Suero;
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

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModeloAnimalService {

    private static final Set<String> SORT_FIELDS_VALIDOS = Set.of("createdAt", "identificador", "fechaNacimiento", "camadaNombre");
    private static final Map<String, String> SORT_ALIASES = Map.of(
            "fechaNacimiento", "camada.fechaNacimiento",
            "camadaNombre",    "camada.nombre"
    );
    private final ModeloAnimalRepository repository;
    private final ModeloAnimalMapper mapper;
    private final PoolRepository poolRepository;
    private final TuboRepository tuboRepository;
    private final ModeloAnimalPoolAporteRepository modeloAnimalPoolAporteRepository;
    private final CamadaRepository camadaRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PageResponse<ModeloAnimalListDTO> findAll(int page, int size, String q, Long poolId,
                                                     SexoRaton sexo, SueroUso uso, Integer rango,
                                                     String sortBy, String sortDir) {
        Sort sort = SpecificationUtils.buildSort(sortBy, sortDir, "fechaNacimiento", SORT_FIELDS_VALIDOS, SORT_ALIASES);
        Page<ModeloAnimal> result = repository.findAll(
                buildSpec(q, poolId, sexo, uso, rango), PageRequest.of(page, size, sort));
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
    public ModeloAnimalResponseDTO findByIdentificador(String identificador) {
        ModeloAnimal m = repository.findByIdentificadorAndActivoTrue(identificador)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo animal " + identificador + " no existe"));
        LocalDate hoy = LocalDate.now(clock);
        VocalizacionesDTO vusDTO = m.getVocalizaciones() != null
                ? mapper.toVocalizacionesDTO(m.getVocalizaciones()) : null;
        TresCamarasDTO tcDTO = m.getTresCamaras() != null
                ? mapper.toTresCamarasDTO(m.getTresCamaras()) : null;
        return mapper.toDTO(m, calcularNecesitaVocalizaciones(m, hoy), calcularNecesitaTresCamaras(m, hoy), vusDTO, tcDTO);
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
        Pool pool = poolRepository.findByIdForUpdate(dto.poolId())
                .filter(Pool::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Pool con id " + dto.poolId() + " no existe"));

        Camada camada = camadaRepository.findById(dto.camadaId())
                .filter(Camada::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Camada con id " + dto.camadaId() + " no existe"));

        long countTotal = repository.countByPool_Id(dto.poolId());
        String identificador = pool.getCodigo() + "-" + (countTotal + 1);

        ModeloAnimal m = mapper.toEntity(dto);
        m.setIdentificador(identificador);
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
                    if (a.cantidadConsumida().compareTo(tubo.getCantidadRestante()) > 0) {
                        throw new BusinessRuleException(
                                "El tubo en posición " + tubo.getPosicion()
                                + " no tiene suficiente volumen. Disponible: " + tubo.getCantidadRestante()
                                + " mL, solicitado: " + a.cantidadConsumida() + " mL");
                    }
                    tubo.setCantidadUsada(tubo.getCantidadUsada().add(a.cantidadConsumida()));
                    if (tubo.getCantidadRestante().compareTo(BigDecimal.ZERO) == 0) {
                        tubo.setPosicion(null);
                    }
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

        if (!m.getPool().getId().equals(dto.poolId())) {
            throw new BusinessRuleException(
                "No se puede cambiar el pool de un modelo animal. El identificador está ligado al pool de origen.");
        }

        Pool pool = poolRepository.findById(dto.poolId())
                .filter(Pool::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Pool con id " + dto.poolId() + " no existe"));

        Camada camada = camadaRepository.findById(dto.camadaId())
                .filter(Camada::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Camada con id " + dto.camadaId() + " no existe"));

        m.setPool(pool);
        m.setCamada(camada);
        m.setSexo(dto.sexo());

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
        if (dto.m1TiempoRatonNovedad() != null) {
            tc.setM1TiempoRatonNovedad(dto.m1TiempoRatonNovedad());
            tc.setM1TiempoObjetoNovedoso(dto.m1TiempoObjetoNovedoso());
        }
        if (dto.m2TiempoRatonDesconocido() != null) {
            tc.setM2TiempoRatonDesconocido(dto.m2TiempoRatonDesconocido());
            tc.setM2TiempoRatonFamiliar(dto.m2TiempoRatonFamiliar());
        }
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
    public ModeloAnimalResponseDTO registrarInoculacion(Long id, ModeloAnimalInoculacionDTO dto) {
        ModeloAnimal m = findActiveById(id);
        m.setFechaDia1Inoculacion(dto.fechaDia1Inoculacion());
        if (dto.aportes() != null) {
            for (ModeloAnimalPoolAporteInputDTO a : dto.aportes()) {
                Tubo tubo = tuboRepository.findById(a.poolTuboId())
                        .orElseThrow(() -> new ResourceNotFoundException("Tubo con id " + a.poolTuboId() + " no existe"));
                if (tubo.getTipo() != TipoTubo.POOL) {
                    throw new BusinessRuleException(
                            "El tubo en posición " + tubo.getPosicion() + " no es un tubo de pool");
                }
                if (!tubo.getPool().getId().equals(m.getPool().getId())) {
                    throw new BusinessRuleException(
                            "El tubo " + tubo.getPosicion() + " no pertenece al pool de este modelo animal");
                }
                // Upsert por día: si ya existe, revertir su volumen del tubo antes de reemplazarlo
                modeloAnimalPoolAporteRepository.findByModeloAnimal_IdAndDia(m.getId(), a.dia())
                        .ifPresent(prev -> {
                            if (prev.getCantidadConsumida() != null) {
                                Tubo tuboAnterior = prev.getTubo();
                                tuboAnterior.setCantidadUsada(tuboAnterior.getCantidadUsada().subtract(prev.getCantidadConsumida()));
                                tuboRepository.save(tuboAnterior);
                            }
                            modeloAnimalPoolAporteRepository.delete(prev);
                        });
                ModeloAnimalPoolAporte aporte = new ModeloAnimalPoolAporte();
                aporte.setModeloAnimal(m);
                aporte.setTubo(tubo);
                aporte.setCantidadConsumida(a.cantidadConsumida());
                aporte.setDia(a.dia());
                modeloAnimalPoolAporteRepository.save(aporte);
                if (a.cantidadConsumida() != null) {
                    if (a.cantidadConsumida().compareTo(tubo.getCantidadRestante()) > 0) {
                        throw new BusinessRuleException(
                                "El tubo en posición " + tubo.getPosicion()
                                + " no tiene suficiente volumen. Disponible: " + tubo.getCantidadRestante()
                                + " mL, solicitado: " + a.cantidadConsumida() + " mL");
                    }
                    tubo.setCantidadUsada(tubo.getCantidadUsada().add(a.cantidadConsumida()));
                    if (tubo.getCantidadRestante().compareTo(BigDecimal.ZERO) == 0) {
                        tubo.setPosicion(null);
                    }
                    tuboRepository.save(tubo);
                }
            }
        }
        repository.save(m);
        LocalDate hoy = LocalDate.now(clock);
        ModeloAnimal refreshed = repository.findById(m.getId()).orElseThrow();
        VocalizacionesDTO vusDTO = refreshed.getVocalizaciones() != null
                ? mapper.toVocalizacionesDTO(refreshed.getVocalizaciones()) : null;
        TresCamarasDTO tcDTO = refreshed.getTresCamaras() != null
                ? mapper.toTresCamarasDTO(refreshed.getTresCamaras()) : null;
        return mapper.toDTO(refreshed,
                calcularNecesitaVocalizaciones(refreshed, hoy),
                calcularNecesitaTresCamaras(refreshed, hoy),
                vusDTO, tcDTO);
    }

    @Transactional(readOnly = true)
    public ModeloAnimalReporteDTO getReporte(String identificador) {
        ModeloAnimal m = repository.findByIdentificadorForReporte(identificador)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo animal " + identificador + " no existe"));

        Pool pool = m.getPool();
        ModeloAnimalReporteDTO.PoolReporteDTO poolDTO = new ModeloAnimalReporteDTO.PoolReporteDTO(
                pool.getCodigo(),
                pool.getRango(),
                pool.getUso(),
                pool.getFechaCreacion(),
                pool.getCaja().getFreezer(),
                pool.getCaja().getCajon(),
                pool.getCaja().getNumero()
        );

        List<ModeloAnimalReporteDTO.InoculacionReporteDTO> inoculaciones = m.getAportes().stream()
                .filter(a -> a.getDia() != null)
                .sorted(Comparator.comparing(ModeloAnimalPoolAporte::getDia))
                .map(a -> {
                    LocalDate fecha = m.getFechaDia1Inoculacion() != null
                            ? m.getFechaDia1Inoculacion().plusDays(a.getDia() - 1)
                            : null;
                    return new ModeloAnimalReporteDTO.InoculacionReporteDTO(a.getDia(), fecha, a.getCantidadConsumida());
                })
                .toList();

        List<ModeloAnimalReporteDTO.SueroReporteDTO> sueros = pool.getAportes().stream()
                .map(a -> a.getTubo().getSuero())
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Suero::getId,
                        s -> s,
                        (a, b) -> a,
                        LinkedHashMap::new
                ))
                .values().stream()
                .map(this::toSueroReporteDTO)
                .toList();

        VocalizacionesDTO vusDTO = m.getVocalizaciones() != null
                ? mapper.toVocalizacionesDTO(m.getVocalizaciones()) : null;
        TresCamarasDTO tcDTO = m.getTresCamaras() != null
                ? mapper.toTresCamarasDTO(m.getTresCamaras()) : null;

        return new ModeloAnimalReporteDTO(
                m.getId(),
                m.getIdentificador(),
                m.getSexo(),
                m.getCamada().getNombre(),
                m.getCamada().getFechaNacimiento(),
                m.getFechaDia1Inoculacion(),
                inoculaciones,
                poolDTO,
                sueros,
                vusDTO,
                tcDTO,
                m.getNumCelulasGanglionares(),
                m.getNumCelulasPurkinje(),
                m.getCreatedAt()
        );
    }

    private ModeloAnimalReporteDTO.SueroReporteDTO toSueroReporteDTO(Suero s) {
        return new ModeloAnimalReporteDTO.SueroReporteDTO(
                s.getValorAnticuerpos(),
                s.getRango(),
                s.getFechaExtraccion(),
                s.getPaciente() != null ? toPacienteReporteDTO(s.getPaciente()) : null
        );
    }

    private ModeloAnimalReporteDTO.PacienteReporteDTO toPacienteReporteDTO(Paciente p) {
        MchatFamilia mchat   = p.getMchatFamilia();
        MchatSeguimiento seg = p.getMchatSeguimiento();
        EvaluacionCars cars  = p.getEvaluacionCars();
        EvaluacionVineland v = p.getEvaluacionVineland();

        List<Boolean> familiaItems = mchat != null ? List.of(
            mchat.isP1(),  mchat.isP2(),  mchat.isP3(),  mchat.isP4(),  mchat.isP5(),
            mchat.isP6(),  mchat.isP7(),  mchat.isP8(),  mchat.isP9(),  mchat.isP10(),
            mchat.isP11(), mchat.isP12(), mchat.isP13(), mchat.isP14(), mchat.isP15(),
            mchat.isP16(), mchat.isP17(), mchat.isP18(), mchat.isP19(), mchat.isP20()
        ) : null;

        List<Boolean> seguimientoItems = seg != null ? List.of(
            seg.isItem1(),  seg.isItem2(),  seg.isItem3(),  seg.isItem4(),  seg.isItem5(),
            seg.isItem6(),  seg.isItem7(),  seg.isItem8(),  seg.isItem9(),  seg.isItem10(),
            seg.isItem11(), seg.isItem12(), seg.isItem13(), seg.isItem14(), seg.isItem15(),
            seg.isItem16(), seg.isItem17(), seg.isItem18(), seg.isItem19(), seg.isItem20()
        ) : null;

        CarsItemsResponseDTO carsItems = cars != null ? new CarsItemsResponseDTO(
            cars.getItem1(),  cars.getItem2(),  cars.getItem3(),  cars.getItem4(),  cars.getItem5(),
            cars.getItem6(),  cars.getItem7(),  cars.getItem8(),  cars.getItem9(),  cars.getItem10(),
            cars.getItem11(), cars.getItem12(), cars.getItem13(), cars.getItem14(), cars.getItem15(),
            cars.getObs1(),  cars.getObs2(),  cars.getObs3(),  cars.getObs4(),  cars.getObs5(),
            cars.getObs6(),  cars.getObs7(),  cars.getObs8(),  cars.getObs9(),  cars.getObs10(),
            cars.getObs11(), cars.getObs12(), cars.getObs13(), cars.getObs14(), cars.getObs15()
        ) : null;

        return new ModeloAnimalReporteDTO.PacienteReporteDTO(
                p.getCodigoNumerico(),
                p.getTipoPaciente(),
                p.getFechaPrimeraVisita(),
                familiaItems,
                mchat != null ? mchat.getScoreTotal() : null,
                mchat != null ? MchatRiesgo.from(mchat.getScoreTotal()) : null,
                mchat != null ? mchat.getResultadoFinal() : null,
                seguimientoItems,
                seg != null ? seg.getFallas() : null,
                carsItems,
                cars != null ? cars.getRawScore() : null,
                cars != null ? cars.getTScore() : null,
                cars != null ? cars.getPercentil() : null,
                cars != null ? CarsResultado.from(cars.getRawScore()) : null,
                v != null ? v.getComunicacion() : null,
                v != null ? v.getAutovalimiento() : null,
                v != null ? v.getSocial() : null,
                v != null ? v.getMotor() : null,
                v != null ? v.getCocienteFinal() : null,
                v != null ? v.getConductaDesadaptativa() : null,
                v != null ? v.getInternalizante() : null,
                v != null ? v.getExternalizante() : null
        );
    }

    @Transactional
    public void delete(Long id) {
        ModeloAnimal m = findActiveById(id);
        m.setActivo(false);
        repository.save(m);
    }

    private boolean calcularNecesitaVocalizaciones(ModeloAnimal m, LocalDate hoy) {
        LocalDate fn = m.getCamada() != null ? m.getCamada().getFechaNacimiento() : null;
        return m.getVocalizaciones() == null
                && fn != null
                && fn.plusDays(5).equals(hoy);
    }

    private boolean calcularNecesitaTresCamaras(ModeloAnimal m, LocalDate hoy) {
        LocalDate fn = m.getCamada() != null ? m.getCamada().getFechaNacimiento() : null;
        return m.getTresCamaras() == null
                && fn != null
                && fn.plusDays(19).equals(hoy);
    }

    private Specification<ModeloAnimal> buildSpec(String q, Long poolId, SexoRaton sexo, SueroUso uso, Integer rango) {
        Specification<ModeloAnimal> spec = SpecificationUtils.activoTrue();
        if (q      != null && !q.isBlank()) spec = spec.and(searchCamada(q));
        if (poolId != null) spec = spec.and(poolEquals(poolId));
        if (sexo   != null) spec = spec.and(sexoEquals(sexo));
        if (uso    != null) spec = spec.and(usoEquals(uso));
        if (rango  != null) spec = spec.and(rangoEquals(rango));
        return spec;
    }

    private Specification<ModeloAnimal> searchCamada(String q) {
        String pattern = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("camada").get("nombre")), pattern),
                cb.like(cb.lower(root.get("identificador")), pattern)
        );
    }

    private Specification<ModeloAnimal> poolEquals(Long poolId) {
        return (root, query, cb) -> cb.equal(root.get("pool").get("id"), poolId);
    }

    private Specification<ModeloAnimal> sexoEquals(SexoRaton sexo) {
        return (root, query, cb) -> cb.equal(root.get("sexo"), sexo);
    }

    private Specification<ModeloAnimal> usoEquals(SueroUso uso) {
        return (root, query, cb) -> cb.equal(root.get("pool").get("uso"), uso);
    }

    private Specification<ModeloAnimal> rangoEquals(Integer rango) {
        return (root, query, cb) -> cb.equal(root.get("pool").get("rango"), rango);
    }

    private ModeloAnimal findActiveById(Long id) {
        return repository.findById(id)
                .filter(ModeloAnimal::isActivo)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo animal con id " + id + " no existe"));
    }
}
