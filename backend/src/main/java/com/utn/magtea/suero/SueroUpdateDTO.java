package com.utn.magtea.suero;

import com.utn.magtea.tubo.TuboInputDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.util.List;

public record SueroUpdateDTO(
        @NotNull(message = "La caja es obligatoria") Long cajaId,
        @NotNull(message = "Los tubos son obligatorios")
        @NotEmpty(message = "Debe incluir al menos un tubo")
        @Valid List<TuboInputDTO> tubos,
        @NotNull(message = "La fecha de extracción es obligatoria") LocalDate fechaExtraccion,
        @PositiveOrZero(message = "El valor de anticuerpos no puede ser negativo") Double valorAnticuerpos
) {}
