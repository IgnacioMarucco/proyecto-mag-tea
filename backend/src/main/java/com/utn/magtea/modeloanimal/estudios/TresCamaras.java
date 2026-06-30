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
@Table(name = "tres_camaras")
@Getter
@Setter
@NoArgsConstructor
public class TresCamaras extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_animal_id", nullable = false, unique = true)
    private ModeloAnimal modeloAnimal;

    @Column(nullable = false)
    private Double m1TiempoRatonNovedad;

    @Column(nullable = false)
    private Double m1TiempoObjetoNovedoso;

    @Column(nullable = true)
    private Double m2TiempoRatonDesconocido;

    @Column(nullable = true)
    private Double m2TiempoRatonFamiliar;
}
