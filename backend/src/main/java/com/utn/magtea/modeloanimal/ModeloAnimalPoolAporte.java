package com.utn.magtea.modeloanimal;

import com.utn.magtea.common.Auditable;
import com.utn.magtea.tubo.Tubo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Audited
@Entity
@Table(name = "modelo_animal_pool_aportes")
@Getter
@Setter
@NoArgsConstructor
public class ModeloAnimalPoolAporte extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_animal_id", nullable = false)
    private ModeloAnimal modeloAnimal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tubo_id", nullable = false)
    private Tubo tubo;

    @Column(precision = 10, scale = 2)
    private BigDecimal cantidadConsumida;

    private Integer dia;
}
