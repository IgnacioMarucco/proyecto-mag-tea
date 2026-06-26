package com.utn.magtea.reporte;

import com.utn.magtea.modeloanimal.ModeloAnimal;
import com.utn.magtea.modeloanimal.ModeloAnimalRepository;
import com.utn.magtea.modeloanimal.estudios.TresCamaras;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesUltrasonicas;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteRepository;
import com.utn.magtea.paciente.cars.EvaluacionCars;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatSeguimiento;
import com.utn.magtea.paciente.vineland.EvaluacionVineland;
import com.utn.magtea.pool.Pool;
import com.utn.magtea.pool.PoolRepository;
import com.utn.magtea.pool.PoolSueroAporte;
import com.utn.magtea.suero.Suero;
import com.utn.magtea.suero.SueroRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportService {

    private static final String SEP = ";";
    private static final String BOM = "﻿";
    private static final String NL  = "\n";

    private final ModeloAnimalRepository modeloAnimalRepository;
    private final PacienteRepository     pacienteRepository;
    private final PoolRepository         poolRepository;
    private final SueroRepository        sueroRepository;

    @Transactional(readOnly = true)
    public byte[] exportarRatones() {
        List<ModeloAnimal> animales = modeloAnimalRepository.findAllForExport();
        StringBuilder sb = new StringBuilder(BOM);
        sb.append(headerRatones()).append(NL);
        for (ModeloAnimal m : animales) {
            sb.append(rowRaton(m)).append(NL);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarPacientes() {
        List<Paciente> pacientes = pacienteRepository.findAllForExport();
        Map<Long, Suero> sueroByPacienteId = sueroRepository.findAllForExport()
                .stream()
                .collect(Collectors.toMap(s -> s.getPaciente().getId(), s -> s, (a, b) -> a));
        StringBuilder sb = new StringBuilder(BOM);
        sb.append(headerPacientes()).append(NL);
        for (Paciente p : pacientes) {
            sb.append(rowPaciente(p, sueroByPacienteId.get(p.getId()))).append(NL);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportarPoolComposicion() {
        List<Pool> pools = poolRepository.findAllForExport();
        StringBuilder sb = new StringBuilder(BOM);
        sb.append(headerPoolComposicion()).append(NL);
        Set<String> seen = new HashSet<>();
        for (Pool pool : pools) {
            for (PoolSueroAporte aporte : pool.getAportes()) {
                Suero suero = aporte.getTubo().getSuero();
                if (suero == null) continue;
                Paciente paciente = suero.getPaciente();
                String key = pool.getCodigo() + "|" + paciente.getCodigoNumerico();
                if (seen.add(key)) {
                    sb.append(rowPoolComposicion(pool, suero, paciente)).append(NL);
                }
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ── Headers ──────────────────────────────────────────────────────────────

    private String headerRatones() {
        return String.join(SEP,
                "identificador", "sexo", "camada_nombre", "camada_fecha_nacimiento",
                "pool_codigo", "pool_rango", "pool_uso",
                "vocalizacion_muestra1_khz", "vocalizacion_muestra2_khz",
                "tres_camaras_m1_raton_novedad", "tres_camaras_m1_objeto_novedoso",
                "tres_camaras_m2_raton_desconocido", "tres_camaras_m2_raton_familiar",
                "celulas_ganglionares", "celulas_purkinje");
    }

    private String headerPacientes() {
        List<String> cols = new ArrayList<>(Arrays.asList(
                "codigo_numerico", "tipo_paciente", "sexo",
                "suero_valor_anticuerpos", "suero_rango", "suero_uso",
                "mchat_score_total", "mchat_resultado_final"));
        for (int i = 1; i <= 20; i++) cols.add("mchat_familia_p" + i);
        cols.add("mchat_seg_fallas");
        for (int i = 1; i <= 20; i++) cols.add("mchat_seg_item" + i);
        cols.addAll(Arrays.asList(
                "cars_raw_score", "cars_t_score", "cars_percentil",
                "vineland_comunicacion", "vineland_autovalimiento", "vineland_social",
                "vineland_motor", "vineland_cociente_final",
                "vineland_conducta_desadaptativa", "vineland_internalizante", "vineland_externalizante"));
        return String.join(SEP, cols);
    }

    private String headerPoolComposicion() {
        return String.join(SEP,
                "pool_codigo", "pool_rango", "pool_uso",
                "paciente_codigo", "suero_valor_anticuerpos", "suero_rango");
    }

    // ── Rows ─────────────────────────────────────────────────────────────────

    private String rowRaton(ModeloAnimal m) {
        VocalizacionesUltrasonicas v  = m.getVocalizaciones();
        TresCamaras               tc = m.getTresCamaras();
        return String.join(SEP,
                csv(m.getIdentificador()),
                csv(m.getSexo()),
                csv(m.getCamada().getNombre()),
                csv(m.getCamada().getFechaNacimiento()),
                csv(m.getPool().getCodigo()),
                csv(m.getPool().getRango()),
                csv(m.getPool().getUso()),
                v  != null ? csv(v.getMuestra1Khz())              : "",
                v  != null ? csv(v.getMuestra2Khz())              : "",
                tc != null ? csv(tc.getM1TiempoRatonNovedad())    : "",
                tc != null ? csv(tc.getM1TiempoObjetoNovedoso())  : "",
                tc != null ? csv(tc.getM2TiempoRatonDesconocido()): "",
                tc != null ? csv(tc.getM2TiempoRatonFamiliar())   : "",
                csv(m.getNumCelulasGanglionares()),
                csv(m.getNumCelulasPurkinje()));
    }

    private String rowPaciente(Paciente p, Suero suero) {
        MchatFamilia     mf  = p.getMchatFamilia();
        MchatSeguimiento ms  = p.getMchatSeguimiento();
        EvaluacionCars   cars = p.getEvaluacionCars();
        EvaluacionVineland vin = p.getEvaluacionVineland();

        List<String> cols = new ArrayList<>(Arrays.asList(
                csv(p.getCodigoNumerico()),
                csv(p.getTipoPaciente()),
                csv(p.getSexo()),
                suero != null ? csv(suero.getValorAnticuerpos()) : "",
                suero != null ? csv(suero.getRango())            : "",
                suero != null ? csv(suero.getUso())              : "",
                mf    != null ? csv(mf.getScoreTotal())          : "",
                mf    != null ? csv(mf.getResultadoFinal())      : ""));

        if (mf != null) {
            cols.addAll(Arrays.asList(
                    bit(mf.isP1()),  bit(mf.isP2()),  bit(mf.isP3()),  bit(mf.isP4()),
                    bit(mf.isP5()),  bit(mf.isP6()),  bit(mf.isP7()),  bit(mf.isP8()),
                    bit(mf.isP9()),  bit(mf.isP10()), bit(mf.isP11()), bit(mf.isP12()),
                    bit(mf.isP13()), bit(mf.isP14()), bit(mf.isP15()), bit(mf.isP16()),
                    bit(mf.isP17()), bit(mf.isP18()), bit(mf.isP19()), bit(mf.isP20())));
        } else {
            for (int i = 0; i < 20; i++) cols.add("");
        }

        cols.add(ms != null ? csv(ms.getFallas()) : "");
        if (ms != null) {
            cols.addAll(Arrays.asList(
                    bit(ms.isItem1()),  bit(ms.isItem2()),  bit(ms.isItem3()),  bit(ms.isItem4()),
                    bit(ms.isItem5()),  bit(ms.isItem6()),  bit(ms.isItem7()),  bit(ms.isItem8()),
                    bit(ms.isItem9()),  bit(ms.isItem10()), bit(ms.isItem11()), bit(ms.isItem12()),
                    bit(ms.isItem13()), bit(ms.isItem14()), bit(ms.isItem15()), bit(ms.isItem16()),
                    bit(ms.isItem17()), bit(ms.isItem18()), bit(ms.isItem19()), bit(ms.isItem20())));
        } else {
            for (int i = 0; i < 20; i++) cols.add("");
        }

        cols.addAll(Arrays.asList(
                cars != null ? csv(cars.getRawScore())  : "",
                cars != null ? csv(cars.getTScore())    : "",
                cars != null ? csv(cars.getPercentil()) : "",
                vin  != null ? csv(vin.getComunicacion())          : "",
                vin  != null ? csv(vin.getAutovalimiento())        : "",
                vin  != null ? csv(vin.getSocial())                : "",
                vin  != null ? csv(vin.getMotor())                 : "",
                vin  != null ? csv(vin.getCocienteFinal())         : "",
                vin  != null ? csv(vin.getConductaDesadaptativa()) : "",
                vin  != null ? csv(vin.getInternalizante())        : "",
                vin  != null ? csv(vin.getExternalizante())        : ""));

        return String.join(SEP, cols);
    }

    private String rowPoolComposicion(Pool pool, Suero suero, Paciente paciente) {
        return String.join(SEP,
                csv(pool.getCodigo()),
                csv(pool.getRango()),
                csv(pool.getUso()),
                csv(paciente.getCodigoNumerico()),
                csv(suero.getValorAnticuerpos()),
                csv(suero.getRango()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String csv(Object val) {
        if (val == null) return "";
        String s = val.toString();
        if (s.contains(SEP) || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String bit(boolean val) {
        return val ? "1" : "0";
    }

    // ── XLSX ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] exportarRatonesXlsx() {
        List<ModeloAnimal> animales = modeloAnimalRepository.findAllForExport();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Ratones");
            String[] headers = headerRatones().split(SEP);
            writeHeaderRow(sheet, wb, headers);
            int rowIdx = 1;
            for (ModeloAnimal m : animales) {
                String[] vals = rowRaton(m).split(SEP, -1);
                writeDataRow(sheet, rowIdx++, vals);
            }
            autoSize(sheet, headers.length);
            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Error generando XLSX de ratones", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportarPacientesXlsx() {
        List<Paciente> pacientes = pacienteRepository.findAllForExport();
        Map<Long, Suero> sueroByPacienteId = sueroRepository.findAllForExport()
                .stream()
                .collect(Collectors.toMap(s -> s.getPaciente().getId(), s -> s, (a, b) -> a));
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Pacientes");
            String[] headers = headerPacientes().split(SEP);
            writeHeaderRow(sheet, wb, headers);
            int rowIdx = 1;
            for (Paciente p : pacientes) {
                String[] vals = rowPaciente(p, sueroByPacienteId.get(p.getId())).split(SEP, -1);
                writeDataRow(sheet, rowIdx++, vals);
            }
            autoSize(sheet, headers.length);
            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Error generando XLSX de pacientes", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportarPoolComposicionXlsx() {
        List<Pool> pools = poolRepository.findAllForExport();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Pool Composición");
            String[] headers = headerPoolComposicion().split(SEP);
            writeHeaderRow(sheet, wb, headers);
            int rowIdx = 1;
            Set<String> seen = new HashSet<>();
            for (Pool pool : pools) {
                for (PoolSueroAporte aporte : pool.getAportes()) {
                    Suero suero = aporte.getTubo().getSuero();
                    if (suero == null) continue;
                    Paciente paciente = suero.getPaciente();
                    String key = pool.getCodigo() + "|" + paciente.getCodigoNumerico();
                    if (seen.add(key)) {
                        String[] vals = rowPoolComposicion(pool, suero, paciente).split(SEP, -1);
                        writeDataRow(sheet, rowIdx++, vals);
                    }
                }
            }
            autoSize(sheet, headers.length);
            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Error generando XLSX de pool composición", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportarCompletoXlsx() {
        List<ModeloAnimal> animales = modeloAnimalRepository.findAllForExport();
        List<Paciente> pacientes = pacienteRepository.findAllForExport();
        Map<Long, Suero> sueroByPacienteId = sueroRepository.findAllForExport()
                .stream()
                .collect(Collectors.toMap(s -> s.getPaciente().getId(), s -> s, (a, b) -> a));
        List<Pool> pools = poolRepository.findAllForExport();

        try (Workbook wb = new XSSFWorkbook()) {
            // Sheet 1 — Ratones
            Sheet sheetRatones = wb.createSheet("Ratones");
            String[] headersRatones = headerRatones().split(SEP);
            writeHeaderRow(sheetRatones, wb, headersRatones);
            int r = 1;
            for (ModeloAnimal m : animales) {
                writeDataRow(sheetRatones, r++, rowRaton(m).split(SEP, -1));
            }
            autoSize(sheetRatones, headersRatones.length);

            // Sheet 2 — Pacientes
            Sheet sheetPacientes = wb.createSheet("Pacientes");
            String[] headersPacientes = headerPacientes().split(SEP);
            writeHeaderRow(sheetPacientes, wb, headersPacientes);
            r = 1;
            for (Paciente p : pacientes) {
                writeDataRow(sheetPacientes, r++, rowPaciente(p, sueroByPacienteId.get(p.getId())).split(SEP, -1));
            }
            autoSize(sheetPacientes, headersPacientes.length);

            // Sheet 3 — Pool Composición
            Sheet sheetPools = wb.createSheet("Pool Composición");
            String[] headersPools = headerPoolComposicion().split(SEP);
            writeHeaderRow(sheetPools, wb, headersPools);
            r = 1;
            Set<String> seen = new HashSet<>();
            for (Pool pool : pools) {
                for (PoolSueroAporte aporte : pool.getAportes()) {
                    Suero suero = aporte.getTubo().getSuero();
                    if (suero == null) continue;
                    Paciente paciente = suero.getPaciente();
                    String key = pool.getCodigo() + "|" + paciente.getCodigoNumerico();
                    if (seen.add(key)) {
                        writeDataRow(sheetPools, r++, rowPoolComposicion(pool, suero, paciente).split(SEP, -1));
                    }
                }
            }
            autoSize(sheetPools, headersPools.length);

            return toBytes(wb);
        } catch (IOException e) {
            throw new RuntimeException("Error generando XLSX completo", e);
        }
    }

    private void writeHeaderRow(Sheet sheet, Workbook wb, String[] headers) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void writeDataRow(Sheet sheet, int rowIdx, String[] vals) {
        Row row = sheet.createRow(rowIdx);
        for (int i = 0; i < vals.length; i++) {
            row.createCell(i).setCellValue(vals[i]);
        }
    }

    private void autoSize(Sheet sheet, int numCols) {
        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private byte[] toBytes(Workbook wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        return out.toByteArray();
    }
}
