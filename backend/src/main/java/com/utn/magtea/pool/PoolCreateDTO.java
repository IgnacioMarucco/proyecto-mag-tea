package com.utn.magtea.pool;

import com.utn.magtea.tubo.TuboInputDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PoolCreateDTO(
        @NotNull(message = "La caja es obligatoria") Long cajaId,
        @NotNull @NotEmpty(message = "Debe incluir al menos un aporte de suero")
        @Valid List<SueroTuboAporteInputDTO> aportes,
        @NotNull @NotEmpty(message = "Debe incluir al menos un tubo del pool")
        @Valid List<TuboInputDTO> tubos
) {}
