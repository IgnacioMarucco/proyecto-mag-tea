package com.utn.magtea.paciente.mchat;

import com.utn.magtea.paciente.mchatseguimiento.MchatResultadoFinal;

import java.time.LocalDateTime;

public record MchatFamiliaResponseDTO(
        Long id,
        boolean p1,  boolean p2,  boolean p3,  boolean p4,  boolean p5,
        boolean p6,  boolean p7,  boolean p8,  boolean p9,  boolean p10,
        boolean p11, boolean p12, boolean p13, boolean p14, boolean p15,
        boolean p16, boolean p17, boolean p18, boolean p19, boolean p20,
        int scoreTotal,
        MchatResultadoFinal resultadoFinal,
        LocalDateTime createdAt
) {}
