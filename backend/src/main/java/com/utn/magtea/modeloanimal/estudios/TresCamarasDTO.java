package com.utn.magtea.modeloanimal.estudios;

import com.utn.magtea.modeloanimal.SocializacionResultado;
import jakarta.validation.constraints.Positive;

public record TresCamarasDTO(
        @Positive(message = "El tiempo debe ser mayor a 0") Double m1TiempoRatonNovedad,
        @Positive(message = "El tiempo debe ser mayor a 0") Double m1TiempoObjetoNovedoso,
        @Positive(message = "El tiempo debe ser mayor a 0") Double m2TiempoRatonDesconocido,
        @Positive(message = "El tiempo debe ser mayor a 0") Double m2TiempoRatonFamiliar,
        SocializacionResultado sociabilizacion1,
        SocializacionResultado sociabilizacion2
) {}
