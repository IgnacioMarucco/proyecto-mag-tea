package com.utn.magtea.suero;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.common.Auditable;
import com.utn.magtea.paciente.Paciente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

@Audited
@Entity
@Table(name = "sueros")
@Getter
@Setter
@NoArgsConstructor
public class Suero extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    @Column(nullable = false)
    private String tubos;

    @Column(nullable = false)
    private LocalDate fechaExtraccion;

    @Column(nullable = false)
    private double cantidadTotal;

    @Column(nullable = false)
    private double cantidadUsada = 0.0;

    @Column(nullable = false)
    private double valorAnticuerpos;

    @Column(nullable = false)
    private int rango;

    @Column(nullable = false)
    private boolean activo = true;
}
