package com.utn.magtea.modeloanimal.estudios;

import com.utn.magtea.common.Auditable;
import com.utn.magtea.modeloanimal.ModeloAnimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Table(name = "vocalizaciones_ultrasonicas")
@Getter
@Setter
@NoArgsConstructor
public class VocalizacionesUltrasonicas extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_animal_id", nullable = false)
    private ModeloAnimal modeloAnimal;

    @Column(nullable = false)
    private Double muestra1Khz;

    @Column(nullable = false)
    private Double muestra2Khz;
}
