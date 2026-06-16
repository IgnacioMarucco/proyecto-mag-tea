package com.utn.magtea.donacion;

import com.utn.magtea.common.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "donaciones")
@Getter
@Setter
@NoArgsConstructor
public class Donacion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long monto;

    private String donante;

    private String correo;

    private String mpPreferenceId;

    private String mpPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDonacion estado = EstadoDonacion.PENDIENTE;
}
