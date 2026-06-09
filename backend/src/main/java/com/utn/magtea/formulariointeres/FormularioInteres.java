package com.utn.magtea.formulariointeres;

import com.utn.magtea.common.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "formularios_interes")
@Getter
@Setter
@NoArgsConstructor
public class FormularioInteres extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fechaContacto;

    @Column(nullable = false)
    private String apellidoTutor;

    @Column(nullable = false)
    private String nombreTutor;

    @Column(nullable = false)
    private String correoTutor;

    private String telefono;

    @Column(nullable = false)
    private String apellidoNino;

    @Column(nullable = false)
    private String nombreNino;

    private LocalDate fechaNacimientoNino;

    @Enumerated(EnumType.STRING)
    private ComoConocioProyecto comoConocioProyecto;

    private String otroComoConocio;

    private String diasDisponibles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFormulario estado = EstadoFormulario.PENDIENTE;

    @Column(nullable = false)
    private boolean activo = true;
}
