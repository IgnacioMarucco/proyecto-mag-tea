package com.utn.magtea.pool;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.common.Auditable;
import com.utn.magtea.suero.Suero;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Audited
@Entity
@Table(name = "pools")
@Getter
@Setter
@NoArgsConstructor
public class Pool extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pool_sueros",
            joinColumns = @JoinColumn(name = "pool_id"),
            inverseJoinColumns = @JoinColumn(name = "suero_id")
    )
    private List<Suero> sueros = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    @Column(nullable = false)
    private String tubos;

    @Column(nullable = false)
    private LocalDate fechaCreacion;

    @Column(nullable = false)
    private int rango;

    @Column(nullable = false)
    private double cantidadTotal;

    @Column(nullable = false)
    private double cantidadUsada = 0.0;

    @Column(nullable = false)
    private boolean activo = true;
}
