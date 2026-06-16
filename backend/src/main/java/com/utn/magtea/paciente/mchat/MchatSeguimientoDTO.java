package com.utn.magtea.paciente.mchat;

// true = Pasa, false = Falla
public record MchatSeguimientoDTO(
        boolean item1,  boolean item2,  boolean item3,  boolean item4,  boolean item5,
        boolean item6,  boolean item7,  boolean item8,  boolean item9,  boolean item10,
        boolean item11, boolean item12, boolean item13, boolean item14, boolean item15,
        boolean item16, boolean item17, boolean item18, boolean item19, boolean item20
) {
    public boolean[] toBooleanArray() {
        return new boolean[]{item1, item2, item3, item4, item5, item6, item7, item8, item9, item10,
                             item11, item12, item13, item14, item15, item16, item17, item18, item19, item20};
    }
}
