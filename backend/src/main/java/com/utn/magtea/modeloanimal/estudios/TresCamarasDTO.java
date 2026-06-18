package com.utn.magtea.modeloanimal.estudios;

import com.utn.magtea.modeloanimal.SocializacionResultado;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TresCamarasDTO(
        @NotNull(message = "El tiempo ratón novedad (m1) es obligatorio")
        @Positive(message = "El tiempo debe ser mayor a 0") Double m1TiempoRatonNovedad,
        @NotNull(message = "El tiempo objeto novedoso (m1) es obligatorio")
        @Positive(message = "El tiempo debe ser mayor a 0") Double m1TiempoObjetoNovedoso,
        @NotNull(message = "El tiempo ratón desconocido (m2) es obligatorio")
        @Positive(message = "El tiempo debe ser mayor a 0") Double m2TiempoRatonDesconocido,
        @NotNull(message = "El tiempo ratón familiar (m2) es obligatorio")
        @Positive(message = "El tiempo debe ser mayor a 0") Double m2TiempoRatonFamiliar,
        SocializacionResultado sociabilizacion1,
        SocializacionResultado sociabilizacion2
) {}
