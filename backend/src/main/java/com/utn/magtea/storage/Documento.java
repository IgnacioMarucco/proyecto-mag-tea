package com.utn.magtea.storage;

import com.utn.magtea.common.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "documentos")
@Getter
@Setter
@NoArgsConstructor
public class Documento extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bucket;

    @Column(nullable = false)
    private String clave;

    @Column(nullable = false)
    private String nombreOriginal;

    private String mimeType;

    private Long tamanio;
}
