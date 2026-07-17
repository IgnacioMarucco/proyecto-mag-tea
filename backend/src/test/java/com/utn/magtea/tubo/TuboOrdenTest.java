package com.utn.magtea.tubo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TuboOrdenTest {

    private Tubo tubo(String posicion) {
        Tubo t = new Tubo();
        t.setPosicion(posicion);
        t.setCantidadInicial(BigDecimal.ONE);
        return t;
    }

    @Test
    void deberia_ordenarNaturalmente_conNumerosDeDosDigitos() {
        var tubos = new java.util.ArrayList<>(List.of(
                tubo("A10"), tubo("A2"), tubo("B1"), tubo("A1")));

        tubos.sort(TuboOrden.POR_POSICION);

        assertThat(tubos).extracting(Tubo::getPosicion)
                .containsExactly("A1", "A2", "A10", "B1");
    }

    @Test
    void deberia_ubicarTubosSinPosicionAlFinal() {
        var tubos = new java.util.ArrayList<>(List.of(
                tubo(null), tubo("A2"), tubo("A1")));

        tubos.sort(TuboOrden.POR_POSICION);

        assertThat(tubos).extracting(Tubo::getPosicion)
                .containsExactly("A1", "A2", null);
    }
}
