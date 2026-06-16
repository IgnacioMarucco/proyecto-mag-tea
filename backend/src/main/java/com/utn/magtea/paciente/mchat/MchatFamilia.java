package com.utn.magtea.paciente.mchat;

import com.utn.magtea.common.Auditable;
import com.utn.magtea.paciente.Paciente;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Table(name = "mchat_familia")
@Getter
@Setter
@NoArgsConstructor
public class MchatFamilia extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

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

    @Enumerated(EnumType.STRING)
    private MchatResultadoFinal resultadoFinal;
}
