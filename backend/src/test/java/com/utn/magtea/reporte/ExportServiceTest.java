package com.utn.magtea.reporte;

import com.utn.magtea.camada.Camada;
import com.utn.magtea.modeloanimal.ModeloAnimal;
import com.utn.magtea.modeloanimal.ModeloAnimalRepository;
import com.utn.magtea.modeloanimal.SexoRaton;
import com.utn.magtea.modeloanimal.estudios.TresCamaras;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesUltrasonicas;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.paciente.PacienteRepository;
import com.utn.magtea.paciente.Sexo;
import com.utn.magtea.paciente.TipoPaciente;
import com.utn.magtea.paciente.cars.EvaluacionCars;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchat.MchatResultadoFinal;
import com.utn.magtea.paciente.mchat.MchatSeguimiento;
import com.utn.magtea.paciente.vineland.EvaluacionVineland;
import com.utn.magtea.pool.Pool;
import com.utn.magtea.pool.PoolRepository;
import com.utn.magtea.pool.PoolSueroAporte;
import com.utn.magtea.suero.Suero;
import com.utn.magtea.suero.SueroRepository;
import com.utn.magtea.suero.SueroUso;
import com.utn.magtea.tubo.Tubo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock
    private ModeloAnimalRepository modeloAnimalRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PoolRepository poolRepository;

    @Mock
    private SueroRepository sueroRepository;

    private ExportService service;

    @BeforeEach
    void setUp() {
        service = new ExportService(
                modeloAnimalRepository,
                pacienteRepository,
                poolRepository,
                sueroRepository
        );
    }

    @Test
    void deberia_exportarRatonesCsv_conDatosCompletosYVacios() {
        // Modelo 1: Completo
        ModeloAnimal m1 = new ModeloAnimal();
        m1.setId(1L);
        m1.setIdentificador("M-1");
        m1.setSexo(SexoRaton.MACHO);
        
        Camada c1 = new Camada();
        c1.setId(10L);
        c1.setNombre("Camada Alpha");
        c1.setFechaNacimiento(LocalDate.of(2026, 1, 15));
        m1.setCamada(c1);

        Pool p1 = new Pool();
        p1.setId(20L);
        p1.setCodigo("POOL-A");
        p1.setRango(2);
        p1.setUso(SueroUso.PROBLEMA);
        p1.setActivo(true);
        m1.setPool(p1);

        VocalizacionesUltrasonicas v1 = new VocalizacionesUltrasonicas();
        v1.setMuestra1Khz(45.5);
        v1.setMuestra2Khz(50.2);
        m1.setVocalizaciones(v1);

        TresCamaras tc1 = new TresCamaras();
        tc1.setM1TiempoRatonNovedad(120.0);
        tc1.setM1TiempoObjetoNovedoso(80.0);
        tc1.setM2TiempoRatonDesconocido(110.0);
        tc1.setM2TiempoRatonFamiliar(90.0);
        m1.setTresCamaras(tc1);

        m1.setNumCelulasGanglionares(1500);
        m1.setNumCelulasPurkinje(350);

        // Modelo 2: Datos mínimos / Nulos
        ModeloAnimal m2 = new ModeloAnimal();
        m2.setId(2L);
        m2.setIdentificador("M-2");
        m2.setSexo(SexoRaton.HEMBRA);

        Camada c2 = new Camada();
        c2.setId(11L);
        c2.setNombre("Camada Beta");
        c2.setFechaNacimiento(LocalDate.of(2026, 2, 20));
        m2.setCamada(c2);

        Pool p2 = new Pool();
        p2.setId(21L);
        p2.setCodigo("POOL-B");
        p2.setRango(1);
        p2.setUso(SueroUso.CONTROL);
        p2.setActivo(false);
        m2.setPool(p2);

        m2.setVocalizaciones(null);
        m2.setTresCamaras(null);

        when(modeloAnimalRepository.findAllForExport()).thenReturn(List.of(m1, m2));

        byte[] result = service.exportarRatones();
        String csvContent = new String(result, java.nio.charset.StandardCharsets.UTF_8);

        assertThat(csvContent).contains("identificador;sexo;camada_nombre;camada_fecha_nacimiento");
        assertThat(csvContent).contains("M-1;MACHO;Camada Alpha;2026-01-15;POOL-A;2;PROBLEMA;Sí;45.5;50.2;120.0;80.0;110.0;90.0;1500;350");
        assertThat(csvContent).contains("M-2;HEMBRA;Camada Beta;2026-02-20;POOL-B;1;CONTROL;No (baja);;;;;;;;");
    }

    @Test
    void deberia_exportarPacientesCsv_conDatosCompletosYVacios() {
        // Paciente 1: Con datos de evaluación completos y suero
        Paciente pac1 = new Paciente();
        pac1.setId(100L);
        pac1.setCodigoNumerico("P-100");
        pac1.setTipoPaciente(TipoPaciente.PROBLEMA);
        pac1.setSexo(Sexo.MASCULINO);

        MchatFamilia mf1 = new MchatFamilia();
        mf1.setScoreTotal(8);
        mf1.setResultadoFinal(MchatResultadoFinal.POSITIVA);
        mf1.setP1(true); mf1.setP2(false); mf1.setP3(true); mf1.setP4(false);
        mf1.setP5(true); mf1.setP6(false); mf1.setP7(true); mf1.setP8(false);
        mf1.setP9(true); mf1.setP10(false); mf1.setP11(true); mf1.setP12(false);
        mf1.setP13(true); mf1.setP14(false); mf1.setP15(true); mf1.setP16(false);
        mf1.setP17(true); mf1.setP18(false); mf1.setP19(true); mf1.setP20(false);
        pac1.setMchatFamilia(mf1);

        MchatSeguimiento ms1 = new MchatSeguimiento();
        ms1.setFallas(3);
        ms1.setItem1(true); ms1.setItem2(false); ms1.setItem3(true); ms1.setItem4(false);
        ms1.setItem5(true); ms1.setItem6(false); ms1.setItem7(true); ms1.setItem8(false);
        ms1.setItem9(true); ms1.setItem10(false); ms1.setItem11(true); ms1.setItem12(false);
        ms1.setItem13(true); ms1.setItem14(false); ms1.setItem15(true); ms1.setItem16(false);
        ms1.setItem17(true); ms1.setItem18(false); ms1.setItem19(true); ms1.setItem20(false);
        pac1.setMchatSeguimiento(ms1);

        EvaluacionCars cars1 = new EvaluacionCars();
        cars1.setRawScore(BigDecimal.valueOf(35.5));
        cars1.setTScore(BigDecimal.valueOf(52.0));
        cars1.setPercentil(88);
        pac1.setEvaluacionCars(cars1);

        EvaluacionVineland vin1 = new EvaluacionVineland();
        vin1.setComunicacion(85);
        vin1.setAutovalimiento(90);
        vin1.setSocial(78);
        vin1.setMotor(88);
        vin1.setCocienteFinal(85);
        vin1.setConductaDesadaptativa(16);
        vin1.setInternalizante(10);
        vin1.setExternalizante(11);
        pac1.setEvaluacionVineland(vin1);

        // Paciente 2: Evaluaciones nulas y sin suero
        Paciente pac2 = new Paciente();
        pac2.setId(101L);
        pac2.setCodigoNumerico("P-101");
        pac2.setTipoPaciente(TipoPaciente.CONTROL);
        pac2.setSexo(Sexo.FEMENINO);

        // Suero de Paciente 1
        Suero s1 = new Suero();
        s1.setId(200L);
        s1.setPaciente(pac1);
        s1.setValorAnticuerpos(BigDecimal.valueOf(145.2));
        s1.setRango(3);
        s1.setUso(SueroUso.PROBLEMA);
        s1.setActivo(true);

        when(pacienteRepository.findAllForExport()).thenReturn(List.of(pac1, pac2));
        when(sueroRepository.findAllForExport()).thenReturn(List.of(s1));

        byte[] result = service.exportarPacientes();
        String csvContent = new String(result, java.nio.charset.StandardCharsets.UTF_8);

        assertThat(csvContent).contains("codigo_numerico;tipo_paciente;sexo;suero_valor_anticuerpos");
        assertThat(csvContent).contains("P-100;PROBLEMA;MASCULINO;145.2;3;PROBLEMA;8;POSITIVA;1;0;1;0;1;0;1;0;1;0;1;0;1;0;1;0;1;0;1;0;3;1;0;1;0;1;0;1;0;1;0;1;0;1;0;1;0;1;0;1;0;35.5;52.0;88;85;90;78;88;85;16;10;11");
        // Verificar fila de paciente 2 (sin suero, mchat ni evaluaciones)
        assertThat(csvContent).contains("P-101;CONTROL;FEMENINO;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");
    }

    @Test
    void deberia_exportarPoolComposicionCsv_evitandoDuplicadosYExcluyendoNulos() {
        Pool pool1 = new Pool();
        pool1.setId(300L);
        pool1.setCodigo("POOL-1");
        pool1.setRango(2);
        pool1.setUso(SueroUso.PROBLEMA);

        Paciente pac1 = new Paciente();
        pac1.setId(500L);
        pac1.setCodigoNumerico("PAC-500");

        Suero suero1 = new Suero();
        suero1.setId(600L);
        suero1.setPaciente(pac1);
        suero1.setValorAnticuerpos(BigDecimal.valueOf(115.0));
        suero1.setRango(2);
        suero1.setActivo(true);

        Tubo tubo1 = new Tubo();
        tubo1.setId(700L);
        tubo1.setSuero(suero1);

        PoolSueroAporte aporte1 = new PoolSueroAporte();
        aporte1.setId(800L);
        aporte1.setTubo(tubo1);

        // Aporte 2: Duplicado (mismo pool y paciente) -> debe ser ignorado
        PoolSueroAporte aporteDuplicado = new PoolSueroAporte();
        aporteDuplicado.setId(801L);
        aporteDuplicado.setTubo(tubo1);

        // Aporte 3: Tubo sin suero -> debe ser ignorado
        Tubo tuboSinSuero = new Tubo();
        tuboSinSuero.setId(701L);
        tuboSinSuero.setSuero(null);

        PoolSueroAporte aporteSinSuero = new PoolSueroAporte();
        aporteSinSuero.setId(802L);
        aporteSinSuero.setTubo(tuboSinSuero);

        List<PoolSueroAporte> aportesList = new ArrayList<>();
        aportesList.add(aporte1);
        aportesList.add(aporteDuplicado);
        aportesList.add(aporteSinSuero);
        pool1.setAportes(aportesList);

        when(poolRepository.findAllForExport()).thenReturn(List.of(pool1));

        byte[] result = service.exportarPoolComposicion();
        String csvContent = new String(result, java.nio.charset.StandardCharsets.UTF_8);

        assertThat(csvContent).contains("pool_codigo;pool_rango;pool_uso;paciente_codigo");
        assertThat(csvContent).contains("POOL-1;2;PROBLEMA;PAC-500;115.0;2;Sí");
        
        // El contenido solo debe tener una línea de datos (dos saltos de línea en total: BOM + cabecera, +1 fila datos, +1 final)
        String[] lines = csvContent.split("\n");
        // Línea 0: BOM + cabecera
        // Línea 1: POOL-1;2;PROBLEMA;PAC-500;115.0;2;Sí
        assertThat(lines).hasSize(2);
    }

    @Test
    void deberia_exportarRatonesXlsxCorrectamente() {
        ModeloAnimal m = new ModeloAnimal();
        m.setId(1L);
        m.setIdentificador("M-1");
        m.setSexo(SexoRaton.MACHO);
        
        Camada c = new Camada();
        c.setNombre("Camada Alpha");
        c.setFechaNacimiento(LocalDate.of(2026, 1, 15));
        m.setCamada(c);

        Pool p = new Pool();
        p.setCodigo("POOL-A");
        p.setRango(2);
        p.setUso(SueroUso.PROBLEMA);
        p.setActivo(true);
        m.setPool(p);

        when(modeloAnimalRepository.findAllForExport()).thenReturn(List.of(m));

        byte[] result = service.exportarRatonesXlsx();
        assertThat(result).isNotEmpty();
    }

    @Test
    void deberia_exportarPacientesXlsxCorrectamente() {
        Paciente pac = new Paciente();
        pac.setId(100L);
        pac.setCodigoNumerico("P-100");
        pac.setTipoPaciente(TipoPaciente.PROBLEMA);
        pac.setSexo(Sexo.MASCULINO);

        Suero s = new Suero();
        s.setPaciente(pac);
        s.setValorAnticuerpos(BigDecimal.valueOf(145.2));
        s.setRango(3);
        s.setUso(SueroUso.PROBLEMA);
        s.setActivo(true);

        when(pacienteRepository.findAllForExport()).thenReturn(List.of(pac));
        when(sueroRepository.findAllForExport()).thenReturn(List.of(s));

        byte[] result = service.exportarPacientesXlsx();
        assertThat(result).isNotEmpty();
    }

    @Test
    void deberia_exportarPoolComposicionXlsxCorrectamente() {
        Pool pool1 = new Pool();
        pool1.setId(300L);
        pool1.setCodigo("POOL-1");
        pool1.setRango(2);
        pool1.setUso(SueroUso.PROBLEMA);

        Paciente pac1 = new Paciente();
        pac1.setId(500L);
        pac1.setCodigoNumerico("PAC-500");

        Suero suero1 = new Suero();
        suero1.setId(600L);
        suero1.setPaciente(pac1);
        suero1.setValorAnticuerpos(BigDecimal.valueOf(115.0));
        suero1.setRango(2);
        suero1.setActivo(true);

        Tubo tubo1 = new Tubo();
        tubo1.setSuero(suero1);

        PoolSueroAporte aporte1 = new PoolSueroAporte();
        aporte1.setTubo(tubo1);

        pool1.setAportes(List.of(aporte1));

        when(poolRepository.findAllForExport()).thenReturn(List.of(pool1));

        byte[] result = service.exportarPoolComposicionXlsx();
        assertThat(result).isNotEmpty();
    }

    @Test
    void deberia_exportarCompletoXlsxCorrectamente() {
        // Mock ModeloAnimal
        ModeloAnimal m = new ModeloAnimal();
        m.setId(1L);
        m.setIdentificador("M-1");
        m.setSexo(SexoRaton.MACHO);
        
        Camada c = new Camada();
        c.setNombre("Camada Alpha");
        c.setFechaNacimiento(LocalDate.of(2026, 1, 15));
        m.setCamada(c);

        Pool p = new Pool();
        p.setCodigo("POOL-A");
        p.setRango(2);
        p.setUso(SueroUso.PROBLEMA);
        p.setActivo(true);
        m.setPool(p);

        // Mock Paciente & Suero
        Paciente pac = new Paciente();
        pac.setId(100L);
        pac.setCodigoNumerico("P-100");
        pac.setTipoPaciente(TipoPaciente.PROBLEMA);
        pac.setSexo(Sexo.MASCULINO);

        Suero s = new Suero();
        s.setPaciente(pac);
        s.setValorAnticuerpos(BigDecimal.valueOf(145.2));
        s.setRango(3);
        s.setUso(SueroUso.PROBLEMA);
        s.setActivo(true);

        // Mock Pool
        Tubo tubo = new Tubo();
        tubo.setSuero(s);

        PoolSueroAporte aporte = new PoolSueroAporte();
        aporte.setTubo(tubo);

        p.setAportes(List.of(aporte));

        when(modeloAnimalRepository.findAllForExport()).thenReturn(List.of(m));
        when(pacienteRepository.findAllForExport()).thenReturn(List.of(pac));
        when(sueroRepository.findAllForExport()).thenReturn(List.of(s));
        when(poolRepository.findAllForExport()).thenReturn(List.of(p));

        byte[] result = service.exportarCompletoXlsx();
        assertThat(result).isNotEmpty();
    }

    @Test
    void deberia_exportarVacio_cuandoNoHayDatos() {
        when(modeloAnimalRepository.findAllForExport()).thenReturn(Collections.emptyList());
        when(pacienteRepository.findAllForExport()).thenReturn(Collections.emptyList());
        when(sueroRepository.findAllForExport()).thenReturn(Collections.emptyList());
        when(poolRepository.findAllForExport()).thenReturn(Collections.emptyList());

        byte[] ratonesCsv = service.exportarRatones();
        byte[] pacientesCsv = service.exportarPacientes();
        byte[] poolsCsv = service.exportarPoolComposicion();

        assertThat(new String(ratonesCsv, java.nio.charset.StandardCharsets.UTF_8)).endsWith("celulas_purkinje\n");
        assertThat(new String(pacientesCsv, java.nio.charset.StandardCharsets.UTF_8)).endsWith("vineland_externalizante\n");
        assertThat(new String(poolsCsv, java.nio.charset.StandardCharsets.UTF_8)).endsWith("suero_activo\n");

        byte[] completoXlsx = service.exportarCompletoXlsx();
        assertThat(completoXlsx).isNotEmpty();
    }
}
