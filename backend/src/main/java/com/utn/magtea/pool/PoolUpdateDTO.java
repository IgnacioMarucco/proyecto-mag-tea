package com.utn.magtea.pool;

import com.utn.magtea.tubo.TuboInputDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record PoolUpdateDTO(
        @NotNull(message = "La caja es obligatoria") Long cajaId,
        @NotNull(message = "La fecha de creación es obligatoria") LocalDate fechaCreacion,
        @NotNull @NotEmpty(message = "Debe incluir al menos un tubo del pool")
        @Valid List<TuboInputDTO> tubos
) {}
