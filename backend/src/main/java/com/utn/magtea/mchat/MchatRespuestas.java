package com.utn.magtea.mchat;

import com.utn.magtea.common.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mchat_respuestas")
@Getter
@Setter
@NoArgsConstructor
public class MchatRespuestas extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long pacienteId;

    // true = Sí, false = No
    private boolean p1;
    private boolean p2;
    private boolean p3;
    private boolean p4;
    private boolean p5;
    private boolean p6;
    private boolean p7;
    private boolean p8;
    private boolean p9;
    private boolean p10;
    private boolean p11;
    private boolean p12;
    private boolean p13;
    private boolean p14;
    private boolean p15;
    private boolean p16;
    private boolean p17;
    private boolean p18;
    private boolean p19;
    private boolean p20;

    @Column(nullable = false)
    private int scoreTotal;
}
