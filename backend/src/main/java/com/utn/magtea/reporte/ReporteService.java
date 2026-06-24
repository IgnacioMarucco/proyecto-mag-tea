package com.utn.magtea.reporte;

import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.formulariointeres.ComoConocioProyecto;
import com.utn.magtea.formulariointeres.EstadoFormulario;
import com.utn.magtea.formulariointeres.FormularioInteres;
import com.utn.magtea.formulariointeres.FormularioInteresRepository;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteEstado;
import com.utn.magtea.paciente.PacienteRepository;
import com.utn.magtea.paciente.Sexo;
import com.utn.magtea.paciente.TipoPaciente;
import com.utn.magtea.paciente.cars.CarsResultado;
import com.utn.magtea.paciente.cars.EvaluacionCars;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatResultadoFinal;
import com.utn.magtea.paciente.mchat.MchatScoringUtil;
import com.utn.magtea.paciente.mchat.MchatSeguimiento;
import com.utn.magtea.paciente.vineland.EvaluacionVineland;
import com.utn.magtea.reporte.dto.AnticuerposDTO;
import com.utn.magtea.reporte.dto.CarsAnaliticaDTO;
import com.utn.magtea.reporte.dto.ComparacionGruposDTO;
import com.utn.magtea.reporte.dto.CorrelacionPuntoDTO;
import com.utn.magtea.reporte.dto.CorrelacionResponseDTO;
import com.utn.magtea.reporte.dto.DashboardAnaliticaDTO;
import com.utn.magtea.reporte.dto.DemograficoDTO;
import com.utn.magtea.reporte.dto.DistribucionDTO;
import com.utn.magtea.reporte.dto.EmbudoDTO;
import com.utn.magtea.reporte.dto.EstadisticasGrupoDTO;
import com.utn.magtea.reporte.dto.EtapaDTO;
import com.utn.magtea.reporte.dto.MchatAnaliticaDTO;
import com.utn.magtea.reporte.dto.ResumenGeneralDTO;
import com.utn.magtea.reporte.dto.VinelandAnaliticaDTO;
import org.apache.commons.math3.distribution.TDistribution;
import com.utn.magtea.suero.Suero;
import com.utn.magtea.suero.SueroRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final PacienteRepository pacienteRepository;
    private final FormularioInteresRepository formularioRepository;
    private final SueroRepository sueroRepository;
    private final Clock clock;

    private static final double[] CARS_BIN_EDGES = {
        15.0, 18.0, 21.0, 24.0, 27.0, 30.0, 33.0, 36.5, 40.0, 44.0, 48.0, 52.0, 56.0, 60.0
    };

    // Límites en meses para cada grupo etario del filtro (clave = año cumplido)
    private static final Map<Integer, int[]> EDAD_GRUPOS_MESES = Map.of(
        2, new int[]{18,  35},
        3, new int[]{36,  47},
        4, new int[]{48,  59},
        5, new int[]{60, 120}
    );

    private static final Set<String> EJES_VALIDOS =
        Set.of("MCHAT_SCORE", "CARS_RAW", "VINELAND_COCIENTE", "BTU_VALUE");

    // ══ Endpoint consolidado ══

    @Transactional(readOnly = true)
    public DashboardAnaliticaDTO getDashboard(String tipoPaciente, List<Integer> edades) {
        List<Paciente> todos = filtrarPacientes("TODOS", edades);
        List<Paciente> filtrados = (tipoPaciente == null || tipoPaciente.isBlank() || tipoPaciente.equals("TODOS"))
                ? todos
                : todos.stream()
                        .filter(p -> p.getTipoPaciente() == TipoPaciente.valueOf(tipoPaciente))
                        .toList();
        List<FormularioInteres> formulariosFiltrados = filtrarFormularios(edades);

        return new DashboardAnaliticaDTO(
                computeResumen(filtrados, formulariosFiltrados),
                computeEmbudo(filtrados),
                computeDemografico(filtrados, formulariosFiltrados),
                computeMchat(filtrados),
                computeCars(filtrados),
                computeVineland(filtrados),
                computeAnticuerpos(filtrados),
                computeComparacionGrupos(todos)
        );
    }

    // ══ Correlaciones (endpoint separado — el par se elige dinámicamente en el frontend) ══

    @Transactional(readOnly = true)
    public CorrelacionResponseDTO getCorrelaciones(String ejeX, String ejeY,
            String tipoPaciente, List<Integer> edades) {
        if (!EJES_VALIDOS.contains(ejeX) || !EJES_VALIDOS.contains(ejeY)) {
            throw new BusinessRuleException("Eje no válido. Opciones: " + EJES_VALIDOS);
        }

        List<Paciente> filtrados = filtrarPacientes(tipoPaciente, edades).stream()
            .filter(p -> p.getFechaNacimientoNino() != null)
            .toList();

        // Pre-fetch sueros solo si es necesario (evita N+1)
        Map<Long, Double> btuMap = Collections.emptyMap();
        if ("BTU_VALUE".equals(ejeX) || "BTU_VALUE".equals(ejeY)) {
            List<Long> ids = filtrados.stream().map(Paciente::getId).toList();
            if (!ids.isEmpty()) {
                btuMap = sueroRepository.findAllByPacienteIdInAndActivoTrue(ids).stream()
                    .filter(s -> s.getValorAnticuerpos() != null)
                    .collect(Collectors.toMap(s -> s.getPaciente().getId(), s -> s.getValorAnticuerpos().doubleValue()));
            }
        }

        final Map<Long, Double> btuMapFinal = btuMap;
        List<double[]> xys = new ArrayList<>();
        List<CorrelacionPuntoDTO> puntos = new ArrayList<>();

        for (Paciente p : filtrados) {
            Double x = extraerValorEje(p, ejeX, btuMapFinal);
            Double y = extraerValorEje(p, ejeY, btuMapFinal);
            if (x != null && y != null) {
                puntos.add(new CorrelacionPuntoDTO(x, y, p.getCodigoNumerico(), p.getTipoPaciente().name()));
                xys.add(new double[]{x, y});
            }
        }

        double[] xs = xys.stream().mapToDouble(a -> a[0]).toArray();
        double[] ys = xys.stream().mapToDouble(a -> a[1]).toArray();
        Double r = calcPearson(xs, ys);
        return new CorrelacionResponseDTO(puntos, r, calcPValue(r, puntos.size()), puntos.size());
    }

    // ══ Filtrado en DB ══

    private List<Paciente> filtrarPacientes(String tipoPaciente, List<Integer> edades) {
        TipoPaciente tipo = (tipoPaciente != null && !tipoPaciente.isBlank()
                             && !tipoPaciente.equals("TODOS"))
                            ? TipoPaciente.valueOf(tipoPaciente) : null;
        return pacienteRepository.findAll(buildReportSpec(tipo, edades));
    }

    private List<FormularioInteres> filtrarFormularios(List<Integer> edades) {
        Specification<FormularioInteres> spec = (root, query, cb) -> cb.isTrue(root.get("activo"));
        if (edades != null && !edades.isEmpty()) {
            LocalDate hoy = LocalDate.now(clock);
            spec = spec.and((root, query, cb) -> {
                List<Predicate> rangos = edades.stream()
                    .map(EDAD_GRUPOS_MESES::get)
                    .filter(Objects::nonNull)
                    .map(r -> cb.between(root.get("fechaNacimientoNino"),
                                         hoy.minusMonths(r[1]),
                                         hoy.minusMonths(r[0])))
                    .map(p -> (Predicate) p)
                    .toList();
                return rangos.size() == 1 ? rangos.get(0) : cb.or(rangos.toArray(new Predicate[0]));
            });
        }
        return formularioRepository.findAll(spec);
    }

    private Specification<Paciente> buildReportSpec(TipoPaciente tipo, List<Integer> edades) {
        Specification<Paciente> spec = (root, query, cb) -> cb.isTrue(root.get("activo"));
        if (tipo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipoPaciente"), tipo));
        }
        if (edades != null && !edades.isEmpty()) {
            LocalDate hoy = LocalDate.now(clock);
            spec = spec.and((root, query, cb) -> {
                List<Predicate> rangos = edades.stream()
                    .map(EDAD_GRUPOS_MESES::get)
                    .filter(Objects::nonNull)
                    .map(r -> cb.between(root.get("fechaNacimientoNino"),
                                         hoy.minusMonths(r[1]),
                                         hoy.minusMonths(r[0])))
                    .map(p -> (Predicate) p)
                    .toList();
                return rangos.size() == 1 ? rangos.get(0) : cb.or(rangos.toArray(new Predicate[0]));
            });
        }
        return spec;
    }

    // ══ Métodos de cómputo privados ══

    private ResumenGeneralDTO computeResumen(List<Paciente> filtrados, List<FormularioInteres> formularios) {
        long totalFormularios = formularios.size();
        long contactados = formularios.stream()
            .filter(f -> f.getEstado() == EstadoFormulario.CONTACTADO
                      || f.getEstado() == EstadoFormulario.ADMITIDO)
            .count();
        long admitidos = formularios.stream()
            .filter(f -> f.getEstado() == EstadoFormulario.ADMITIDO)
            .count();
        long pacientesTotal         = filtrados.size();
        long pacientesProblema      = filtrados.stream().filter(p -> p.getTipoPaciente() == TipoPaciente.PROBLEMA).count();
        long pacientesControl       = filtrados.stream().filter(p -> p.getTipoPaciente() == TipoPaciente.CONTROL).count();
        long mchatCompletados       = filtrados.stream().filter(p -> PacienteEstado.MCHAT_COMPLETADOS.contains(p.getEstadoClinico())).count();
        long extraccionesRealizadas = filtrados.stream().filter(p -> p.getEstadoClinico() == PacienteEstado.EXTRACCION_REALIZADA).count();

        return new ResumenGeneralDTO(
            totalFormularios, contactados, admitidos,
            pacientesTotal, pacientesProblema, pacientesControl,
            mchatCompletados, extraccionesRealizadas
        );
    }

    private EmbudoDTO computeEmbudo(List<Paciente> filtrados) {
        long admitidos    = filtrados.size();
        long primeraVisita = filtrados.stream().filter(p -> PacienteEstado.MCHAT_COMPLETADOS.contains(p.getEstadoClinico())).count();
        long extPendiente = filtrados.stream().filter(p -> PacienteEstado.CON_EXTRACCION.contains(p.getEstadoClinico())).count();
        long extRealizada = filtrados.stream().filter(p -> p.getEstadoClinico() == PacienteEstado.EXTRACCION_REALIZADA).count();

        double base = admitidos > 0 ? admitidos : 1;
        List<EtapaDTO> etapas = List.of(
            new EtapaDTO("Admitidos",                admitidos,     100.0),
            new EtapaDTO("Primera visita realizada", primeraVisita, round(primeraVisita / base * 100)),
            new EtapaDTO("Extracción pendiente",     extPendiente,  round(extPendiente  / base * 100)),
            new EtapaDTO("Extracción realizada",     extRealizada,  round(extRealizada  / base * 100))
        );
        return new EmbudoDTO(etapas);
    }

    private DemograficoDTO computeDemografico(List<Paciente> filtrados, List<FormularioInteres> formularios) {
        Map<Sexo, Long> sexoFreq = filtrados.stream()
            .filter(p -> p.getSexo() != null)
            .collect(Collectors.groupingBy(Paciente::getSexo, Collectors.counting()));
        long totalSexo = sexoFreq.values().stream().mapToLong(Long::longValue).sum();
        List<DistribucionDTO> sexo = sexoFreq.entrySet().stream()
            .map(e -> new DistribucionDTO(e.getKey().name(), e.getValue(),
                totalSexo > 0 ? round(e.getValue() / (double) totalSexo * 100) : 0))
            .toList();

        Map<ComoConocioProyecto, Long> derivFreq = formularios.stream()
            .filter(f -> f.getComoConocioProyecto() != null)
            .collect(Collectors.groupingBy(FormularioInteres::getComoConocioProyecto, Collectors.counting()));
        long totalDeriv = derivFreq.values().stream().mapToLong(Long::longValue).sum();
        List<DistribucionDTO> derivacion = derivFreq.entrySet().stream()
            .sorted(Map.Entry.<ComoConocioProyecto, Long>comparingByValue().reversed())
            .map(e -> new DistribucionDTO(e.getKey().name(), e.getValue(),
                totalDeriv > 0 ? round(e.getValue() / (double) totalDeriv * 100) : 0))
            .toList();

        List<DistribucionDTO> etaria = computeDistribucionEtaria(filtrados);

        return new DemograficoDTO(sexo, derivacion, etaria);
    }

    private List<DistribucionDTO> computeDistribucionEtaria(List<Paciente> filtrados) {
        LocalDate hoy = LocalDate.now(clock);
        int[] grupos = {2, 3, 4, 5};
        Map<Integer, String> labels = Map.of(2, "18-35m", 3, "36-47m", 4, "48-59m", 5, "60-120m");
        Map<Integer, Long> freq = new LinkedHashMap<>();
        for (int g : grupos) freq.put(g, 0L);

        for (Paciente p : filtrados) {
            if (p.getFechaNacimientoNino() == null) continue;
            long meses = ChronoUnit.MONTHS.between(p.getFechaNacimientoNino(), hoy);
            for (int g : grupos) {
                int[] r = EDAD_GRUPOS_MESES.get(g);
                if (meses >= r[0] && meses <= r[1]) {
                    freq.merge(g, 1L, Long::sum);
                    break;
                }
            }
        }

        long total = freq.values().stream().mapToLong(Long::longValue).sum();
        return Arrays.stream(grupos)
            .mapToObj(g -> {
                long n = freq.get(g);
                return new DistribucionDTO(labels.get(g), n, total > 0 ? round(n / (double) total * 100) : 0);
            })
            .toList();
    }

    private MchatAnaliticaDTO computeMchat(List<Paciente> filtrados) {
        List<MchatFamilia> todos = filtrados.stream()
            .map(Paciente::getMchatFamilia)
            .filter(Objects::nonNull)
            .toList();
        long totalConMchat = todos.size();

        double[] scores = todos.stream().mapToDouble(m -> m.getScoreTotal()).toArray();
        double mediaScore = scores.length > 0 ? Arrays.stream(scores).average().orElse(0) : 0;
        double sdScore    = calcSD(scores, mediaScore);

        Map<Integer, Long> scoreFreq = todos.stream()
            .collect(Collectors.groupingBy(MchatFamilia::getScoreTotal, Collectors.counting()));
        List<DistribucionDTO> distribucionScores = IntStream.rangeClosed(0, 20)
            .mapToObj(i -> {
                long count = scoreFreq.getOrDefault(i, 0L);
                return new DistribucionDTO(
                    String.valueOf(i), count,
                    totalConMchat > 0 ? round(count / (double) totalConMchat * 100) : 0);
            })
            .toList();

        Map<MchatResultadoFinal, Long> resultadoFreq = todos.stream()
            .filter(m -> m.getResultadoFinal() != null)
            .collect(Collectors.groupingBy(MchatFamilia::getResultadoFinal, Collectors.counting()));
        long totalResultado = resultadoFreq.values().stream().mapToLong(Long::longValue).sum();
        List<DistribucionDTO> resultadoFinal = resultadoFreq.entrySet().stream()
            .map(e -> new DistribucionDTO(e.getKey().name(), e.getValue(),
                totalResultado > 0 ? round(e.getValue() / (double) totalResultado * 100) : 0))
            .toList();

        long riesgoMedio = todos.stream().filter(m -> m.getScoreTotal() >= 3 && m.getScoreTotal() <= 7).count();

        List<MchatSeguimiento> seguimientos = filtrados.stream()
            .map(Paciente::getMchatSeguimiento)
            .filter(Objects::nonNull)
            .toList();
        long totalConSeguimiento = seguimientos.size();
        long riesgoMedioPositiva = seguimientos.stream()
            .filter(s -> MchatScoringUtil.calcularScore(itemsArray(s)) >= 2).count();
        long riesgoMedioNegativa = totalConSeguimiento - riesgoMedioPositiva;

        int[] fallasTamizaje = new int[20];
        for (MchatFamilia m : todos) {
            boolean[] resp = familiaArray(m);
            for (int i = 0; i < 20; i++) {
                int n = i + 1;
                boolean invertida = n == 2 || n == 5 || n == 12;
                if (invertida ? resp[i] : !resp[i]) fallasTamizaje[i]++;
            }
        }
        List<DistribucionDTO> itemsFalladosTamizaje = buildItemsDistribucion(fallasTamizaje, totalConMchat);

        int[] fallasSeguimiento = new int[20];
        for (MchatSeguimiento s : seguimientos) {
            boolean[] resp = itemsArray(s);
            for (int i = 0; i < 20; i++) {
                int n = i + 1;
                boolean invertida = n == 2 || n == 5 || n == 12;
                if (invertida ? resp[i] : !resp[i]) fallasSeguimiento[i]++;
            }
        }
        List<DistribucionDTO> itemsFalladosSeguimiento = buildItemsDistribucion(fallasSeguimiento, totalConSeguimiento);

        return new MchatAnaliticaDTO(
            distribucionScores, resultadoFinal,
            round(mediaScore), round(sdScore),
            totalConMchat, riesgoMedio, totalConSeguimiento,
            riesgoMedioPositiva, riesgoMedioNegativa,
            itemsFalladosTamizaje, totalConSeguimiento, itemsFalladosSeguimiento
        );
    }

    private CarsAnaliticaDTO computeCars(List<Paciente> filtrados) {
        List<EvaluacionCars> conScore = filtrados.stream()
            .map(Paciente::getEvaluacionCars)
            .filter(e -> e != null && e.getRawScore() != null)
            .toList();
        long totalConCars = conScore.size();

        long minimoNoTea  = conScore.stream().filter(e -> e.getRawScore().compareTo(new BigDecimal("30")) < 0).count();
        long leveModerado = conScore.stream().filter(e -> {
            int cmp30 = e.getRawScore().compareTo(new BigDecimal("30"));
            int cmp37 = e.getRawScore().compareTo(CarsResultado.CUTOFF_SEVERO);
            return cmp30 >= 0 && cmp37 < 0;
        }).count();
        long severo = conScore.stream().filter(e -> e.getRawScore().compareTo(CarsResultado.CUTOFF_SEVERO) >= 0).count();

        double media = conScore.stream().mapToDouble(e -> e.getRawScore().doubleValue()).average().orElse(0);
        double sd    = calcSD(conScore.stream().mapToDouble(e -> e.getRawScore().doubleValue()).toArray(), media);

        Map<String, Long> bins = new LinkedHashMap<>();
        for (int i = 0; i < CARS_BIN_EDGES.length - 1; i++) {
            bins.put(binLabel(CARS_BIN_EDGES[i], CARS_BIN_EDGES[i + 1]), 0L);
        }
        for (EvaluacionCars e : conScore) {
            double val = e.getRawScore().doubleValue();
            for (int i = 0; i < CARS_BIN_EDGES.length - 1; i++) {
                double from = CARS_BIN_EDGES[i];
                double to   = CARS_BIN_EDGES[i + 1];
                boolean lastBin = (i == CARS_BIN_EDGES.length - 2);
                if (val >= from && (lastBin ? val <= to : val < to)) {
                    bins.merge(binLabel(from, to), 1L, (a, b) -> a + b);
                    break;
                }
            }
        }
        List<DistribucionDTO> distribucionRawScore = bins.entrySet().stream()
            .map(e -> new DistribucionDTO(e.getKey(), e.getValue(),
                totalConCars > 0 ? round(e.getValue() / (double) totalConCars * 100) : 0))
            .toList();

        return new CarsAnaliticaDTO(distribucionRawScore, minimoNoTea, leveModerado, severo,
            round(media), round(sd), totalConCars);
    }

    private VinelandAnaliticaDTO computeVineland(List<Paciente> filtrados) {
        List<EvaluacionVineland> todos = filtrados.stream()
            .map(Paciente::getEvaluacionVineland)
            .filter(Objects::nonNull)
            .toList();
        long total = todos.size();
        if (total == 0) {
            return new VinelandAnaliticaDTO(0, 0, 0, 0, 0, 0, null, null, null, 0);
        }

        double mediaCom  = todos.stream().filter(v -> v.getComunicacion()   != null).mapToInt(EvaluacionVineland::getComunicacion).average().orElse(0);
        double mediaAuto = todos.stream().filter(v -> v.getAutovalimiento() != null).mapToInt(EvaluacionVineland::getAutovalimiento).average().orElse(0);
        double mediaSoc  = todos.stream().filter(v -> v.getSocial()         != null).mapToInt(EvaluacionVineland::getSocial).average().orElse(0);
        double mediaMot  = todos.stream().filter(v -> v.getMotor()          != null).mapToInt(EvaluacionVineland::getMotor).average().orElse(0);
        double mediaCoc  = todos.stream().filter(v -> v.getCocienteFinal()  != null).mapToInt(EvaluacionVineland::getCocienteFinal).average().orElse(0);

        double[] cocValues = todos.stream().filter(v -> v.getCocienteFinal() != null)
            .mapToDouble(EvaluacionVineland::getCocienteFinal).toArray();
        double sdCoc = calcSD(cocValues, mediaCoc);

        List<EvaluacionVineland> conConducta = todos.stream().filter(v -> v.getConductaDesadaptativa() != null).toList();
        Double mediaConducta = conConducta.isEmpty() ? null
            : round(conConducta.stream().mapToInt(EvaluacionVineland::getConductaDesadaptativa).average().orElse(0));

        List<EvaluacionVineland> conIntern = todos.stream().filter(v -> v.getInternalizante() != null).toList();
        Double mediaIntern = conIntern.isEmpty() ? null
            : round(conIntern.stream().mapToInt(EvaluacionVineland::getInternalizante).average().orElse(0));

        List<EvaluacionVineland> conExtern = todos.stream().filter(v -> v.getExternalizante() != null).toList();
        Double mediaExtern = conExtern.isEmpty() ? null
            : round(conExtern.stream().mapToInt(EvaluacionVineland::getExternalizante).average().orElse(0));

        return new VinelandAnaliticaDTO(
            round(mediaCom), round(mediaAuto), round(mediaSoc), round(mediaMot),
            round(mediaCoc), round(sdCoc),
            mediaConducta, mediaIntern, mediaExtern, total
        );
    }

    private AnticuerposDTO computeAnticuerpos(List<Paciente> filtrados) {
        if (filtrados.isEmpty()) {
            return new AnticuerposDTO(0, 0, 0, 0, 0, buildDistribucionRangosVacia());
        }

        List<Long> ids = filtrados.stream().map(Paciente::getId).toList();
        List<Suero> sueros = sueroRepository.findAllByPacienteIdInAndActivoTrue(ids);
        long totalConSuero = sueros.size();
        long totalSinSuero = filtrados.size() - totalConSuero;

        double[] btus = sueros.stream()
            .filter(s -> s.getValorAnticuerpos() != null)
            .mapToDouble(s -> s.getValorAnticuerpos().doubleValue())
            .toArray();

        double media   = btus.length > 0 ? Arrays.stream(btus).average().orElse(0) : 0;
        double sd      = calcSD(btus, media);
        double mediana = calcMediana(btus);

        Map<Integer, Long> rangoFreq = sueros.stream()
            .filter(s -> s.getRango() != null)
            .collect(Collectors.groupingBy(Suero::getRango, Collectors.counting()));

        List<DistribucionDTO> distribucionRangos = IntStream.rangeClosed(0, 3)
            .mapToObj(r -> {
                long n = rangoFreq.getOrDefault(r, 0L);
                return new DistribucionDTO("Rango " + r, n,
                    totalConSuero > 0 ? round(n / (double) totalConSuero * 100) : 0);
            })
            .toList();

        return new AnticuerposDTO(totalConSuero, totalSinSuero,
            round(media), round(sd), round(mediana), distribucionRangos);
    }

    private ComparacionGruposDTO computeComparacionGrupos(List<Paciente> todos) {
        List<Paciente> problemaPacientes = todos.stream()
            .filter(p -> p.getTipoPaciente() == TipoPaciente.PROBLEMA).toList();
        List<Paciente> controlPacientes  = todos.stream()
            .filter(p -> p.getTipoPaciente() == TipoPaciente.CONTROL).toList();

        List<Long> ids = todos.stream().map(Paciente::getId).toList();
        Map<Long, Double> btuMap = ids.isEmpty() ? Collections.emptyMap()
            : sueroRepository.findAllByPacienteIdInAndActivoTrue(ids).stream()
                .filter(s -> s.getValorAnticuerpos() != null)
                .collect(Collectors.toMap(s -> s.getPaciente().getId(), s -> s.getValorAnticuerpos().doubleValue()));

        double[] btuProblema = problemaPacientes.stream()
            .map(p -> btuMap.get(p.getId())).filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue).toArray();
        double[] btuControl  = controlPacientes.stream()
            .map(p -> btuMap.get(p.getId())).filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue).toArray();

        EstadisticasGrupoDTO statsProblema = buildEstadisticas(problemaPacientes.size(), btuProblema);
        EstadisticasGrupoDTO statsControl  = buildEstadisticas(controlPacientes.size(),  btuControl);

        Double pValue = calcWelchPValue(btuProblema, btuControl);
        Double cohenD = calcCohenD(btuProblema, btuControl);

        return new ComparacionGruposDTO(statsProblema, statsControl, pValue, cohenD);
    }

    private EstadisticasGrupoDTO buildEstadisticas(int nTotal, double[] btus) {
        long nConSuero = btus.length;
        double pct     = nTotal > 0 ? round(nConSuero / (double) nTotal * 100) : 0;
        double media   = btus.length > 0 ? Arrays.stream(btus).average().orElse(0) : 0;
        double sd      = calcSD(btus, media);
        double mediana = calcMediana(btus);
        return new EstadisticasGrupoDTO(nTotal, nConSuero, pct, round(media), round(sd), round(mediana));
    }

    // ══ Helpers privados ══

    private Double extraerValorEje(Paciente p, String eje, Map<Long, Double> btuMap) {
        return switch (eje) {
            case "MCHAT_SCORE"       -> p.getMchatFamilia()       != null ? (double) p.getMchatFamilia().getScoreTotal() : null;
            case "CARS_RAW"          -> p.getEvaluacionCars()     != null && p.getEvaluacionCars().getRawScore() != null
                                        ? p.getEvaluacionCars().getRawScore().doubleValue() : null;
            case "VINELAND_COCIENTE" -> p.getEvaluacionVineland() != null && p.getEvaluacionVineland().getCocienteFinal() != null
                                        ? (double) p.getEvaluacionVineland().getCocienteFinal() : null;
            case "BTU_VALUE"         -> btuMap.get(p.getId());
            default -> null;
        };
    }

    private List<DistribucionDTO> buildDistribucionRangosVacia() {
        return IntStream.rangeClosed(0, 3)
            .mapToObj(r -> new DistribucionDTO("Rango " + r, 0, 0))
            .toList();
    }

    private Double calcPearson(double[] xs, double[] ys) {
        int n = xs.length;
        if (n < 2) return null;
        double mx = Arrays.stream(xs).average().orElse(0);
        double my = Arrays.stream(ys).average().orElse(0);
        double num = 0, dx2 = 0, dy2 = 0;
        for (int i = 0; i < n; i++) {
            double dx = xs[i] - mx, dy = ys[i] - my;
            num += dx * dy;
            dx2 += dx * dx;
            dy2 += dy * dy;
        }
        double denom = Math.sqrt(dx2 * dy2);
        return denom == 0 ? null : round(num / denom);
    }

    private double calcMediana(double[] values) {
        if (values.length == 0) return 0;
        double[] sorted = Arrays.copyOf(values, values.length);
        Arrays.sort(sorted);
        int mid = sorted.length / 2;
        return sorted.length % 2 == 0 ? (sorted[mid - 1] + sorted[mid]) / 2.0 : sorted[mid];
    }

    private String binLabel(double from, double to) {
        String fromStr = (from == Math.floor(from)) ? String.valueOf((int) from) : String.valueOf(from);
        String toStr   = (to   == Math.floor(to))   ? String.valueOf((int) to)   : String.valueOf(to);
        return fromStr + "-" + toStr;
    }

    private boolean[] familiaArray(MchatFamilia m) {
        return new boolean[]{
            m.isP1(), m.isP2(), m.isP3(), m.isP4(), m.isP5(),
            m.isP6(), m.isP7(), m.isP8(), m.isP9(), m.isP10(),
            m.isP11(), m.isP12(), m.isP13(), m.isP14(), m.isP15(),
            m.isP16(), m.isP17(), m.isP18(), m.isP19(), m.isP20()
        };
    }

    private boolean[] itemsArray(MchatSeguimiento s) {
        return new boolean[]{
            s.isItem1(), s.isItem2(), s.isItem3(), s.isItem4(), s.isItem5(),
            s.isItem6(), s.isItem7(), s.isItem8(), s.isItem9(), s.isItem10(),
            s.isItem11(), s.isItem12(), s.isItem13(), s.isItem14(), s.isItem15(),
            s.isItem16(), s.isItem17(), s.isItem18(), s.isItem19(), s.isItem20()
        };
    }

    private List<DistribucionDTO> buildItemsDistribucion(int[] fallas, long total) {
        List<DistribucionDTO> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            items.add(new DistribucionDTO(
                "Ítem " + (i + 1),
                fallas[i],
                total > 0 ? round(fallas[i] / (double) total * 100) : 0
            ));
        }
        items.sort(Comparator.comparingLong(DistribucionDTO::n).reversed());
        return items;
    }

    private double calcSD(double[] values, double media) {
        if (values.length <= 1) return 0;
        double suma = 0;
        for (double v : values) suma += Math.pow(v - media, 2);
        return Math.sqrt(suma / (values.length - 1));
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private Double calcPValue(Double r, int n) {
        if (r == null || n < 3) return null;
        double t = r * Math.sqrt(n - 2) / Math.sqrt(1 - r * r);
        TDistribution dist = new TDistribution(n - 2);
        double p = 2.0 * (1.0 - dist.cumulativeProbability(Math.abs(t)));
        return Math.round(p * 10000.0) / 10000.0;
    }

    // Welch's t-test (no asume igual varianza entre grupos)
    private Double calcWelchPValue(double[] g1, double[] g2) {
        if (g1.length < 2 || g2.length < 2) return null;
        double m1 = Arrays.stream(g1).average().orElse(0);
        double m2 = Arrays.stream(g2).average().orElse(0);
        double v1 = calcVarianza(g1, m1);
        double v2 = calcVarianza(g2, m2);
        double n1 = g1.length, n2 = g2.length;
        double se = Math.sqrt(v1 / n1 + v2 / n2);
        if (se == 0) return null;
        double t  = (m1 - m2) / se;
        double df = Math.pow(v1 / n1 + v2 / n2, 2)
                  / (Math.pow(v1 / n1, 2) / (n1 - 1) + Math.pow(v2 / n2, 2) / (n2 - 1));
        TDistribution dist = new TDistribution(df);
        double p = 2.0 * (1.0 - dist.cumulativeProbability(Math.abs(t)));
        return Math.round(p * 10000.0) / 10000.0;
    }

    // Cohen's d con SD poolado
    private Double calcCohenD(double[] g1, double[] g2) {
        if (g1.length < 2 || g2.length < 2) return null;
        double m1 = Arrays.stream(g1).average().orElse(0);
        double m2 = Arrays.stream(g2).average().orElse(0);
        double v1 = calcVarianza(g1, m1);
        double v2 = calcVarianza(g2, m2);
        double n1 = g1.length, n2 = g2.length;
        double sdPooled = Math.sqrt(((n1 - 1) * v1 + (n2 - 1) * v2) / (n1 + n2 - 2));
        if (sdPooled == 0) return null;
        return round((m1 - m2) / sdPooled);
    }

    private double calcVarianza(double[] values, double media) {
        if (values.length <= 1) return 0;
        double suma = 0;
        for (double v : values) suma += Math.pow(v - media, 2);
        return suma / (values.length - 1);  // varianza muestral (n-1) para Welch
    }
}
