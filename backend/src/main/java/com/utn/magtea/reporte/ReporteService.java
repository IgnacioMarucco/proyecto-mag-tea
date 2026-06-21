package com.utn.magtea.reporte;

import com.utn.magtea.formulariointeres.ComoConocioProyecto;
import com.utn.magtea.formulariointeres.EstadoFormulario;
import com.utn.magtea.formulariointeres.FormularioInteres;
import com.utn.magtea.formulariointeres.FormularioInteresRepository;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteEstado;
import com.utn.magtea.paciente.PacienteRepository;
import com.utn.magtea.paciente.Sexo;
import com.utn.magtea.paciente.TipoPaciente;
import com.utn.magtea.paciente.cars.EvaluacionCars;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatResultadoFinal;
import com.utn.magtea.paciente.mchat.MchatScoringUtil;
import com.utn.magtea.paciente.mchat.MchatSeguimiento;
import com.utn.magtea.paciente.vineland.EvaluacionVineland;
import com.utn.magtea.reporte.dto.CarsAnaliticaDTO;
import com.utn.magtea.reporte.dto.CorrelacionPuntoDTO;
import com.utn.magtea.reporte.dto.DashboardAnaliticaDTO;
import com.utn.magtea.reporte.dto.DemograficoDTO;
import com.utn.magtea.reporte.dto.DistribucionDTO;
import com.utn.magtea.reporte.dto.EmbudoDTO;
import com.utn.magtea.reporte.dto.EtapaDTO;
import com.utn.magtea.reporte.dto.MchatAnaliticaDTO;
import com.utn.magtea.reporte.dto.ResumenGeneralDTO;
import com.utn.magtea.reporte.dto.VinelandAnaliticaDTO;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final PacienteRepository pacienteRepository;
    private final FormularioInteresRepository formularioRepository;

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

    // ══ Endpoint consolidado ══

    @Transactional(readOnly = true)
    public DashboardAnaliticaDTO getDashboard(String tipoPaciente, List<Integer> edades) {
        List<Paciente> filtrados = filtrarPacientes(tipoPaciente, edades);
        List<FormularioInteres> formulariosFiltrados = filtrarFormularios(edades);

        return new DashboardAnaliticaDTO(
                computeResumen(filtrados, formulariosFiltrados),
                computeEmbudo(filtrados),
                computeDemografico(filtrados, formulariosFiltrados),
                computeMchat(filtrados),
                computeCars(filtrados),
                computeVineland(filtrados)
        );
    }

    // ══ Correlaciones (endpoint separado — el par se elige dinámicamente en el frontend) ══

    @Transactional(readOnly = true)
    public List<CorrelacionPuntoDTO> getCorrelaciones(String ejeX, String ejeY,
            String tipoPaciente, List<Integer> edades) {
        List<Paciente> filtrados = filtrarPacientes(tipoPaciente, edades).stream()
            .filter(p -> p.getFechaNacimientoNino() != null)
            .toList();

        List<CorrelacionPuntoDTO> puntos = new ArrayList<>();
        for (Paciente p : filtrados) {
            Double x = extraerValorEje(p, ejeX);
            Double y = extraerValorEje(p, ejeY);
            if (x != null && y != null) {
                puntos.add(new CorrelacionPuntoDTO(x, y, p.getCodigoNumerico()));
            }
        }
        return puntos;
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
            LocalDate hoy = LocalDate.now();
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
            LocalDate hoy = LocalDate.now();
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
        long mchat        = filtrados.stream().filter(p -> PacienteEstado.MCHAT_COMPLETADOS.contains(p.getEstadoClinico())).count();
        long extPendiente = filtrados.stream().filter(p -> PacienteEstado.CON_EXTRACCION.contains(p.getEstadoClinico())).count();
        long extRealizada = filtrados.stream().filter(p -> p.getEstadoClinico() == PacienteEstado.EXTRACCION_REALIZADA).count();

        double base = admitidos > 0 ? admitidos : 1;
        List<EtapaDTO> etapas = List.of(
            new EtapaDTO("Admitidos",            admitidos,    100.0),
            new EtapaDTO("M-CHAT completado",    mchat,        round(mchat        / base * 100)),
            new EtapaDTO("Extracción pendiente", extPendiente, round(extPendiente / base * 100)),
            new EtapaDTO("Extracción realizada", extRealizada, round(extRealizada / base * 100))
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

        return new DemograficoDTO(sexo, derivacion);
    }

    private MchatAnaliticaDTO computeMchat(List<Paciente> filtrados) {
        List<MchatFamilia> todos = filtrados.stream()
            .map(Paciente::getMchatFamilia)
            .filter(Objects::nonNull)
            .toList();
        long totalConMchat = todos.size();

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
            int cmp30  = e.getRawScore().compareTo(new BigDecimal("30"));
            int cmp365 = e.getRawScore().compareTo(new BigDecimal("36.5"));
            return cmp30 >= 0 && cmp365 <= 0;
        }).count();
        long severo = conScore.stream().filter(e -> e.getRawScore().compareTo(new BigDecimal("36.5")) > 0).count();

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

    // ══ Helpers privados ══

    private Double extraerValorEje(Paciente p, String eje) {
        return switch (eje) {
            case "MCHAT_SCORE"       -> p.getMchatFamilia()       != null ? (double) p.getMchatFamilia().getScoreTotal() : null;
            case "CARS_RAW"          -> p.getEvaluacionCars()     != null && p.getEvaluacionCars().getRawScore() != null
                                        ? p.getEvaluacionCars().getRawScore().doubleValue() : null;
            case "VINELAND_COCIENTE" -> p.getEvaluacionVineland() != null && p.getEvaluacionVineland().getCocienteFinal() != null
                                        ? (double) p.getEvaluacionVineland().getCocienteFinal() : null;
            default -> null;
        };
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
        return Math.sqrt(suma / values.length);
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
