package com.utn.magtea.reporte;

import com.utn.magtea.formulariointeres.ComoConocioProyecto;
import com.utn.magtea.formulariointeres.EstadoFormulario;
import com.utn.magtea.formulariointeres.FormularioInteresRepository;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteRepository;
import com.utn.magtea.paciente.Sexo;
import com.utn.magtea.paciente.TipoPaciente;
import com.utn.magtea.paciente.cars.EvaluacionCars;
import com.utn.magtea.paciente.cars.EvaluacionCarsRepository;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatFamiliaRepository;
import com.utn.magtea.paciente.mchat.MchatResultadoFinal;
import com.utn.magtea.paciente.mchat.MchatScoringUtil;
import com.utn.magtea.paciente.mchat.MchatSeguimiento;
import com.utn.magtea.paciente.mchat.MchatSeguimientoRepository;
import com.utn.magtea.paciente.vineland.EvaluacionVineland;
import com.utn.magtea.paciente.vineland.EvaluacionVinelandRepository;
import com.utn.magtea.reporte.dto.CarsAnaliticaDTO;
import com.utn.magtea.reporte.dto.CorrelacionPuntoDTO;
import com.utn.magtea.reporte.dto.DemograficoDTO;
import com.utn.magtea.reporte.dto.DistribucionDTO;
import com.utn.magtea.reporte.dto.EmbudoDTO;
import com.utn.magtea.reporte.dto.EtapaDTO;
import com.utn.magtea.reporte.dto.MchatAnaliticaDTO;
import com.utn.magtea.reporte.dto.ResumenGeneralDTO;
import com.utn.magtea.reporte.dto.VinelandAnaliticaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final PacienteRepository pacienteRepository;
    private final FormularioInteresRepository formularioRepository;
    private final MchatFamiliaRepository mchatFamiliaRepository;
    private final MchatSeguimientoRepository mchatSeguimientoRepository;
    private final EvaluacionCarsRepository evaluacionCarsRepository;
    private final EvaluacionVinelandRepository evaluacionVinelandRepository;

    @Transactional(readOnly = true)
    public ResumenGeneralDTO getResumen() {
        long totalFormularios      = formularioRepository.countByActivoTrue();
        long contactados           = formularioRepository.countByEstadoAndActivoTrue(EstadoFormulario.CONTACTADO);
        long admitidos             = formularioRepository.countByEstadoAndActivoTrue(EstadoFormulario.ADMITIDO);
        long pacientesTotal        = pacienteRepository.countByActivoTrue();
        long pacientesProblema     = pacienteRepository.countByTipoPacienteAndActivoTrue(TipoPaciente.PROBLEMA);
        long pacientesControl      = pacienteRepository.countByTipoPacienteAndActivoTrue(TipoPaciente.CONTROL);
        long mchatCompletados      = pacienteRepository.countMchatCompletados();
        long extraccionesRealizadas = pacienteRepository.countExtraccionRealizada();

        return new ResumenGeneralDTO(
            totalFormularios, contactados, admitidos,
            pacientesTotal, pacientesProblema, pacientesControl,
            mchatCompletados, extraccionesRealizadas
        );
    }

    @Transactional(readOnly = true)
    public EmbudoDTO getEmbudo() {
        long formularios   = formularioRepository.countByActivoTrue();
        long contactados   = formularioRepository.countByEstadoAndActivoTrue(EstadoFormulario.CONTACTADO)
                           + formularioRepository.countByEstadoAndActivoTrue(EstadoFormulario.ADMITIDO);
        long admitidos     = pacienteRepository.countByTipoPacienteAndActivoTrue(TipoPaciente.PROBLEMA);
        long mchat         = pacienteRepository.countMchatCompletados();
        long extPendiente  = pacienteRepository.countExtraccionPendiente();
        long extRealizada  = pacienteRepository.countExtraccionRealizada();

        double base = formularios > 0 ? formularios : 1;

        List<EtapaDTO> etapas = List.of(
            new EtapaDTO("Formularios recibidos", formularios, 100.0),
            new EtapaDTO("Contactados",           contactados,  round(contactados  / base * 100)),
            new EtapaDTO("Admitidos",             admitidos,    round(admitidos    / base * 100)),
            new EtapaDTO("M-CHAT completado",     mchat,        round(mchat        / base * 100)),
            new EtapaDTO("Extracción pendiente",  extPendiente, round(extPendiente / base * 100)),
            new EtapaDTO("Extracción realizada",  extRealizada, round(extRealizada / base * 100))
        );
        return new EmbudoDTO(etapas);
    }

    @Transactional(readOnly = true)
    public DemograficoDTO getDemografico() {
        List<Object[]> sexoRows = pacienteRepository.countBySexo();
        long totalSexo = sexoRows.stream().mapToLong(r -> (Long) r[1]).sum();
        List<DistribucionDTO> sexo = sexoRows.stream()
            .map(r -> new DistribucionDTO(
                ((Sexo) r[0]).name(),
                (Long) r[1],
                round((Long) r[1] / (double) totalSexo * 100)))
            .toList();

        List<Object[]> derivRows = formularioRepository.distribucionComoConocio();
        long totalDeriv = derivRows.stream().mapToLong(r -> (Long) r[1]).sum();
        List<DistribucionDTO> derivacion = derivRows.stream()
            .map(r -> new DistribucionDTO(
                ((ComoConocioProyecto) r[0]).name(),
                (Long) r[1],
                round((Long) r[1] / (double) totalDeriv * 100)))
            .toList();

        return new DemograficoDTO(sexo, derivacion);
    }

    @Transactional(readOnly = true)
    public MchatAnaliticaDTO getMchat() {
        // Distribución de scores
        List<Object[]> scoreRows = mchatFamiliaRepository.distribucionScores();
        List<MchatFamilia> todos = mchatFamiliaRepository.findAllProblema();
        long totalConMchat = todos.size();

        long totalScores = scoreRows.stream().mapToLong(r -> (Long) r[1]).sum();
        List<DistribucionDTO> distribucionScores = scoreRows.stream()
            .map(r -> new DistribucionDTO(
                String.valueOf(r[0]),
                (Long) r[1],
                round((Long) r[1] / (double) totalScores * 100)))
            .toList();

        // Resultado final
        List<Object[]> resultadoRows = mchatFamiliaRepository.distribucionResultadoFinal();
        long totalResultado = resultadoRows.stream().mapToLong(r -> (Long) r[1]).sum();
        List<DistribucionDTO> resultadoFinal = resultadoRows.stream()
            .map(r -> new DistribucionDTO(
                ((MchatResultadoFinal) r[0]).name(),
                (Long) r[1],
                round((Long) r[1] / (double) totalResultado * 100)))
            .toList();

        // Seguimiento (riesgo medio = score 3-7)
        long riesgoMedio = todos.stream().filter(m -> m.getScoreTotal() >= 3 && m.getScoreTotal() <= 7).count();
        List<MchatSeguimiento> seguimientos = mchatSeguimientoRepository.findAllProblema();
        long totalConSeguimiento = seguimientos.size();
        long riesgoMedioConSeguimiento = totalConSeguimiento;
        long riesgoMedioPositiva = seguimientos.stream()
            .filter(s -> MchatScoringUtil.calcularScore(itemsArray(s)) >= 2).count();
        long riesgoMedioNegativa = totalConSeguimiento - riesgoMedioPositiva;

        // Ítems fallados — tamizaje (MchatFamilia)
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

        // Ítems fallados — seguimiento (MchatSeguimiento)
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
            totalConMchat, riesgoMedio, riesgoMedioConSeguimiento,
            riesgoMedioPositiva, riesgoMedioNegativa,
            itemsFalladosTamizaje, totalConSeguimiento, itemsFalladosSeguimiento
        );
    }

    @Transactional(readOnly = true)
    public CarsAnaliticaDTO getCars() {
        List<EvaluacionCars> todos = evaluacionCarsRepository.findAllProblema();
        List<EvaluacionCars> conScore = todos.stream()
            .filter(e -> e.getRawScore() != null).toList();
        long totalConCars = conScore.size();

        // Categorías
        long minimoNoTea = conScore.stream()
            .filter(e -> e.getRawScore().compareTo(new BigDecimal("30")) < 0).count();
        long leveModerado = conScore.stream()
            .filter(e -> {
                int cmp30  = e.getRawScore().compareTo(new BigDecimal("30"));
                int cmp365 = e.getRawScore().compareTo(new BigDecimal("36.5"));
                return cmp30 >= 0 && cmp365 <= 0;
            }).count();
        long severo = conScore.stream()
            .filter(e -> e.getRawScore().compareTo(new BigDecimal("36.5")) > 0).count();

        // Media y SD
        double media = conScore.stream()
            .mapToDouble(e -> e.getRawScore().doubleValue()).average().orElse(0);
        double sd = calcSD(conScore.stream()
            .mapToDouble(e -> e.getRawScore().doubleValue()).toArray(), media);

        // Distribución por bins de 2.5 (rango 15-60)
        Map<String, Long> bins = new java.util.LinkedHashMap<>();
        for (double start = 15.0; start < 60.0; start += 2.5) {
            String label = String.format("%.1f-%.1f", start, start + 2.5);
            bins.put(label, 0L);
        }
        for (EvaluacionCars e : conScore) {
            double val = e.getRawScore().doubleValue();
            for (double start = 15.0; start < 60.0; start += 2.5) {
                if (val >= start && val < start + 2.5) {
                    String label = String.format("%.1f-%.1f", start, start + 2.5);
                    bins.merge(label, 1L, Long::sum);
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

    @Transactional(readOnly = true)
    public VinelandAnaliticaDTO getVineland() {
        List<EvaluacionVineland> todos = evaluacionVinelandRepository.findAllProblema();
        long total = todos.size();
        if (total == 0) {
            return new VinelandAnaliticaDTO(0, 0, 0, 0, 0, 0, null, null, null, 0);
        }

        double mediaCom   = todos.stream().filter(v -> v.getComunicacion()   != null).mapToInt(EvaluacionVineland::getComunicacion).average().orElse(0);
        double mediaAuto  = todos.stream().filter(v -> v.getAutovalimiento() != null).mapToInt(EvaluacionVineland::getAutovalimiento).average().orElse(0);
        double mediaSoc   = todos.stream().filter(v -> v.getSocial()         != null).mapToInt(EvaluacionVineland::getSocial).average().orElse(0);
        double mediaMot   = todos.stream().filter(v -> v.getMotor()          != null).mapToInt(EvaluacionVineland::getMotor).average().orElse(0);
        double mediaCoc   = todos.stream().filter(v -> v.getCocienteFinal()  != null).mapToInt(EvaluacionVineland::getCocienteFinal).average().orElse(0);

        double[] cocValues = todos.stream().filter(v -> v.getCocienteFinal() != null)
            .mapToDouble(EvaluacionVineland::getCocienteFinal).toArray();
        double sdCoc = calcSD(cocValues, mediaCoc);

        List<EvaluacionVineland> conConducta = todos.stream()
            .filter(v -> v.getConductaDesadaptativa() != null).toList();
        Double mediaConducta = conConducta.isEmpty() ? null
            : round(conConducta.stream().mapToInt(EvaluacionVineland::getConductaDesadaptativa).average().orElse(0));

        List<EvaluacionVineland> conIntern = todos.stream()
            .filter(v -> v.getInternalizante() != null).toList();
        Double mediaIntern = conIntern.isEmpty() ? null
            : round(conIntern.stream().mapToInt(EvaluacionVineland::getInternalizante).average().orElse(0));

        List<EvaluacionVineland> conExtern = todos.stream()
            .filter(v -> v.getExternalizante() != null).toList();
        Double mediaExtern = conExtern.isEmpty() ? null
            : round(conExtern.stream().mapToInt(EvaluacionVineland::getExternalizante).average().orElse(0));

        return new VinelandAnaliticaDTO(
            round(mediaCom), round(mediaAuto), round(mediaSoc), round(mediaMot),
            round(mediaCoc), round(sdCoc),
            mediaConducta, mediaIntern, mediaExtern,
            total
        );
    }

    @Transactional(readOnly = true)
    public List<CorrelacionPuntoDTO> getCorrelaciones(String ejeX, String ejeY) {
        List<Paciente> pacientes = pacienteRepository.findProblemaConEdadCalculable();
        List<CorrelacionPuntoDTO> puntos = new ArrayList<>();

        for (Paciente p : pacientes) {
            Double x = extraerValorEje(p, ejeX);
            Double y = extraerValorEje(p, ejeY);
            if (x != null && y != null) {
                puntos.add(new CorrelacionPuntoDTO(x, y, p.getCodigoNumerico()));
            }
        }
        return puntos;
    }

    // --- helpers privados ---

    private Double extraerValorEje(Paciente p, String eje) {
        return switch (eje) {
            case "MCHAT_SCORE"       -> p.getMchatFamilia()      != null ? (double) p.getMchatFamilia().getScoreTotal() : null;
            case "CARS_RAW"          -> p.getEvaluacionCars()    != null && p.getEvaluacionCars().getRawScore() != null
                                        ? p.getEvaluacionCars().getRawScore().doubleValue() : null;
            case "VINELAND_COCIENTE" -> p.getEvaluacionVineland() != null && p.getEvaluacionVineland().getCocienteFinal() != null
                                        ? (double) p.getEvaluacionVineland().getCocienteFinal() : null;
            default -> null;
        };
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
