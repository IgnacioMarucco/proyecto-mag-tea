package com.utn.magtea.inicio;

import java.util.List;

public record InicioResponseDTO(
        Integer formulariosPendientes,
        List<AgendaEventoDTO> agendaSemana,
        List<InoculacionSemanalItemDTO> inoculacionesSemana,
        List<AlertaConductualItemDTO> alertasConductuales,
        List<ActividadRecienteItemDTO> actividadReciente
) {}
