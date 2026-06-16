package com.utn.magtea.paciente.mchat;

// true = Sí, false = No
public record MchatSubmitDTO(
        boolean p1,  boolean p2,  boolean p3,  boolean p4,  boolean p5,
        boolean p6,  boolean p7,  boolean p8,  boolean p9,  boolean p10,
        boolean p11, boolean p12, boolean p13, boolean p14, boolean p15,
        boolean p16, boolean p17, boolean p18, boolean p19, boolean p20
) {
    public boolean[] toBooleanArray() {
        return new boolean[]{p1, p2, p3, p4, p5, p6, p7, p8, p9, p10,
                             p11, p12, p13, p14, p15, p16, p17, p18, p19, p20};
    }
}
