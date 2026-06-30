package com.utn.magtea.paciente.cars;

import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.paciente.Paciente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests de CarsService. No tiene dependencias inyectadas → no necesita MockitoExtension.
 */
class CarsServiceTest {

    private final CarsService service = new CarsService();

    // ── validarItems ─────────────────────────────────────────────────────────────

    @Test
    void deberia_validarItems_cuandoTodosLosValoresSonPermitidos() {
        var dto = buildDtoConValor(new BigDecimal("1.0"));
        assertThatNoException().isThrownBy(() -> service.validarItems(dto));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0"})
    void deberia_aceptarValorPermitido_cuandoEsDecimalValido(String valor) {
        var dto = buildDtoConValor(new BigDecimal(valor));
        assertThatNoException().isThrownBy(() -> service.validarItems(dto));
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoItem1Null() {
        // item1 = null, resto = 1.0
        var dto = new CarsDTO(
                null,
                bd("1.0"), bd("1.0"), bd("1.0"), bd("1.0"),
                bd("1.0"), bd("1.0"), bd("1.0"), bd("1.0"),
                bd("1.0"), bd("1.0"), bd("1.0"), bd("1.0"),
                bd("1.0"), bd("1.0"),
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null);

        assertThatThrownBy(() -> service.validarItems(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ítem 1");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoValorFueraDeRango() {
        // 0.5 no está en la lista permitida
        var dto = buildDtoConValor(new BigDecimal("0.5"));

        assertThatThrownBy(() -> service.validarItems(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ítem 1")
                .hasMessageContaining("0.5")
                .hasMessageContaining("1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0");
    }

    @Test
    void deberia_lanzarBusinessRuleException_cuandoItem15InValido() {
        var dto = new CarsDTO(
                bd("1.0"), bd("1.0"), bd("1.0"), bd("1.0"),
                bd("1.0"), bd("1.0"), bd("1.0"), bd("1.0"),
                bd("1.0"), bd("1.0"), bd("1.0"), bd("1.0"),
                bd("1.0"), bd("1.0"),
                bd("5.0"),  // item15 inválido
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null);

        assertThatThrownBy(() -> service.validarItems(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ítem 15");
    }

    // ── aplicar ───────────────────────────────────────────────────────────────────

    @Test
    void deberia_crearEvaluacionCars_cuandoPacienteSinCars() {
        var paciente = new Paciente();
        paciente.setEvaluacionCars(null);

        var dto = buildDtoConValor(bd("2.0"));

        service.aplicar(paciente, dto);

        var cars = paciente.getEvaluacionCars();
        assertThat(cars).isNotNull();
        assertThat(cars.getItem1()).isEqualByComparingTo("2.0");
        assertThat(cars.getItem15()).isEqualByComparingTo("2.0");
        // rawScore: 15 ítems × 2.0 = 30.0
        assertThat(cars.getRawScore()).isEqualByComparingTo("30.0");
        assertThat(cars.getPaciente()).isSameAs(paciente);
    }

    @Test
    void deberia_actualizarEvaluacionCars_cuandoPacienteYaTieneCars() {
        var paciente = new Paciente();
        var carsExistente = new EvaluacionCars();
        carsExistente.setId(5L);
        paciente.setEvaluacionCars(carsExistente);

        var dto = buildDtoConValor(bd("3.5"));

        service.aplicar(paciente, dto);

        var cars = paciente.getEvaluacionCars();
        assertThat(cars.getId()).isEqualTo(5L); // misma instancia reutilizada
        assertThat(cars.getItem1()).isEqualByComparingTo("3.5");
        // rawScore: 15 ítems × 3.5 = 52.5
        assertThat(cars.getRawScore()).isEqualByComparingTo("52.5");
    }

    @Test
    void deberia_guardarObservaciones_cuandoEstanPresentes() {
        var paciente = new Paciente();
        var dto = new CarsDTO(
                bd("1.0"), bd("1.0"), bd("1.0"), bd("1.0"),
                bd("1.0"), bd("1.0"), bd("1.0"), bd("1.0"),
                bd("1.0"), bd("1.0"), bd("1.0"), bd("1.0"),
                bd("1.0"), bd("1.0"), bd("1.0"),
                "obs1", "obs2", null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null);

        service.aplicar(paciente, dto);

        assertThat(paciente.getEvaluacionCars().getObs1()).isEqualTo("obs1");
        assertThat(paciente.getEvaluacionCars().getObs2()).isEqualTo("obs2");
    }

    @Test
    void deberia_guardarTscoreYPercentil_cuandoPresentes() {
        var paciente = new Paciente();
        var dto = new CarsDTO(
                bd("2.0"), bd("2.0"), bd("2.0"), bd("2.0"),
                bd("2.0"), bd("2.0"), bd("2.0"), bd("2.0"),
                bd("2.0"), bd("2.0"), bd("2.0"), bd("2.0"),
                bd("2.0"), bd("2.0"), bd("2.0"),
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                bd("72.0"), 92);

        service.aplicar(paciente, dto);

        assertThat(paciente.getEvaluacionCars().getTScore()).isEqualByComparingTo("72.0");
        assertThat(paciente.getEvaluacionCars().getPercentil()).isEqualTo(92);
    }

    // ── CarsResultado.from ────────────────────────────────────────────────────────

    @Test
    void deberia_retornarMinimoNoTEA_cuandoRawScoreMenorA30() {
        assertThat(CarsResultado.from(new BigDecimal("29.9"))).isEqualTo(CarsResultado.MINIMO_NO_TEA);
        assertThat(CarsResultado.from(new BigDecimal("15.0"))).isEqualTo(CarsResultado.MINIMO_NO_TEA);
    }

    @Test
    void deberia_retornarLeveModerado_cuandoRawScoreEntre30y36_9() {
        assertThat(CarsResultado.from(new BigDecimal("30.0"))).isEqualTo(CarsResultado.LEVE_MODERADO);
        assertThat(CarsResultado.from(new BigDecimal("36.9"))).isEqualTo(CarsResultado.LEVE_MODERADO);
    }

    @Test
    void deberia_retornarSevero_cuandoRawScoreMayorOIgualA37() {
        assertThat(CarsResultado.from(new BigDecimal("37.0"))).isEqualTo(CarsResultado.SEVERO);
        assertThat(CarsResultado.from(new BigDecimal("60.0"))).isEqualTo(CarsResultado.SEVERO);
    }

    @Test
    void deberia_retornarNull_cuandoRawScoreEsNull() {
        assertThat(CarsResultado.from(null)).isNull();
    }

    // ── helpers ───────────────────────────────────────────────────────────────────

    private BigDecimal bd(String v) { return new BigDecimal(v); }

    /** Crea un DTO con todos los ítems al mismo valor y sin obs/tScore/percentil. */
    private CarsDTO buildDtoConValor(BigDecimal v) {
        return new CarsDTO(
                v, v, v, v, v, v, v, v, v, v, v, v, v, v, v,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null);
    }
}
