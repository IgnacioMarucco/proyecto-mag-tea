package com.utn.magtea.paciente;

import com.utn.magtea.common.Auditable;
import com.utn.magtea.formulariointeres.ComoConocioProyecto;
import com.utn.magtea.paciente.cars.PacienteEvaluacionCars;
import com.utn.magtea.paciente.criterios.PacienteCriterios;
import com.utn.magtea.paciente.mchat.MchatFamilia;
import com.utn.magtea.paciente.mchatseguimiento.PacienteMchatSeguimiento;
import com.utn.magtea.paciente.vineland.PacienteEvaluacionVineland;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Audited
@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
public class Paciente extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Trazabilidad al formulario de origen
    private Long formularioInteresId;

    @Column(nullable = false, unique = true)
    private String codigoNumerico;

    // Datos copiados del FormularioInteres
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

    // Datos del alta
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sexo sexo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPaciente tipoPaciente;

    // Primera visita
    private LocalDateTime fechaPrimeraVisita;

    @Column(nullable = false)
    private boolean consentimientoFirmado = false;

    @Column(columnDefinition = "TEXT")
    private String notas;

    // Token para el formulario M-CHAT enviado por mail
    @NotAudited
    private String mchatToken;
    @NotAudited
    private LocalDateTime mchatTokenExpiry;

    // Segunda visita
    private LocalDate fechaExtraccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PacienteEstado estadoClinico = PacienteEstado.ADMITIDO;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToOne(mappedBy = "paciente", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private MchatFamilia mchatFamilia;

    @OneToOne(mappedBy = "paciente", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private PacienteCriterios criterios;

    @OneToOne(mappedBy = "paciente", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private PacienteMchatSeguimiento mchatSeguimiento;

    @OneToOne(mappedBy = "paciente", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private PacienteEvaluacionCars evaluacionCars;

    @OneToOne(mappedBy = "paciente", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private PacienteEvaluacionVineland evaluacionVineland;

}
