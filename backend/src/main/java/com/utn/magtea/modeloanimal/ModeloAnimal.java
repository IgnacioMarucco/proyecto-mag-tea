package com.utn.magtea.modeloanimal;

import com.utn.magtea.camada.Camada;
import com.utn.magtea.common.Auditable;
import com.utn.magtea.modeloanimal.estudios.TresCamaras;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesUltrasonicas;
import com.utn.magtea.pool.Pool;
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
@Table(name = "modelos_animales")
@Getter
@Setter
@NoArgsConstructor
public class ModeloAnimal extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String identificador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camada_id", nullable = false)
    private Camada camada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SexoRaton sexo;

    private LocalDate fechaDia1Inoculacion;

    private Integer numCelulasGanglionares;

    private Integer numCelulasPurkinje;

    @OneToOne(mappedBy = "modeloAnimal", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private VocalizacionesUltrasonicas vocalizaciones;

    @OneToOne(mappedBy = "modeloAnimal", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private TresCamaras tresCamaras;

    @OneToMany(mappedBy = "modeloAnimal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModeloAnimalPoolAporte> aportes = new ArrayList<>();

    @Column(nullable = false)
    private boolean activo = true;
}
