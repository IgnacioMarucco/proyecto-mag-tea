package com.utn.magtea.pool;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.common.Auditable;
import com.utn.magtea.modeloanimal.ModeloAnimal;
import com.utn.magtea.suero.SueroUso;
import com.utn.magtea.tubo.Tubo;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    @Column(unique = true)
    private String codigo;

    @Column(nullable = false)
    private LocalDate fechaCreacion;

    @Column(nullable = false)
    private int rango;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SueroUso uso;

    @OneToMany(mappedBy = "pool", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tubo> tubos = new ArrayList<>();

    @OneToMany(mappedBy = "pool", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PoolSueroAporte> aportes = new ArrayList<>();

    @OneToMany(mappedBy = "pool", fetch = FetchType.LAZY)
    private List<ModeloAnimal> modelosAnimales = new ArrayList<>();

    @Column(nullable = false)
    private boolean activo = true;
}
