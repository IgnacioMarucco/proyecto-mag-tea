package com.utn.magtea.modeloanimal;

import com.utn.magtea.common.Auditable;
import com.utn.magtea.storage.Documento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "imagenes_microscopia")
@Getter
@Setter
@NoArgsConstructor
public class ImagenMicroscopia extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_animal_id", nullable = false)
    private ModeloAnimal modeloAnimal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoImagenMicroscopia tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @Column(name = "url_externa")
    private String urlExterna;

    private String descripcion;
}
