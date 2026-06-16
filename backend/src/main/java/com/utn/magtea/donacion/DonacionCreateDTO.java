package com.utn.magtea.donacion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;

public record DonacionCreateDTO(
        @NotNull(message = "El monto es obligatorio")
        @Min(value = 1, message = "El monto debe ser mayor a cero")
        Long monto,
        String donante,
        @Email(message = "El correo no es válido") String correo
) {}
