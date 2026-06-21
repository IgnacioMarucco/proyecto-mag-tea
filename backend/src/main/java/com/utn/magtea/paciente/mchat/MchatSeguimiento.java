package com.utn.magtea.paciente.mchat;

import com.utn.magtea.common.Auditable;
import com.utn.magtea.paciente.Paciente;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Audited
@Entity
@Table(name = "paciente_mchat_seguimiento")
@Getter
@Setter
@NoArgsConstructor
public class MchatSeguimiento extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    // Convención: true = Pasa, false = Falla — EXCEPTO ítems 2, 5 y 12 (invertidos): true = Falla, false = Pasa
    @Column(nullable = false) private boolean item1;
    @Column(nullable = false) private boolean item2;
    @Column(nullable = false) private boolean item3;
    @Column(nullable = false) private boolean item4;
    @Column(nullable = false) private boolean item5;
    @Column(nullable = false) private boolean item6;
    @Column(nullable = false) private boolean item7;
    @Column(nullable = false) private boolean item8;
    @Column(nullable = false) private boolean item9;
    @Column(nullable = false) private boolean item10;
    @Column(nullable = false) private boolean item11;
    @Column(nullable = false) private boolean item12;
    @Column(nullable = false) private boolean item13;
    @Column(nullable = false) private boolean item14;
    @Column(nullable = false) private boolean item15;
    @Column(nullable = false) private boolean item16;
    @Column(nullable = false) private boolean item17;
    @Column(nullable = false) private boolean item18;
    @Column(nullable = false) private boolean item19;
    @Column(nullable = false) private boolean item20;

    @Column(nullable = false)
    private int fallas;
}
