package com.utn.magtea.paciente;

import com.utn.magtea.common.Auditable;
import com.utn.magtea.formulariointeres.ComoConocioProyecto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    // Datos del alta
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sexo sexo;

    // Primera visita
    private LocalDateTime fechaPrimeraVisita;

    @Column(nullable = false)
    private boolean consentimientoFirmado = false;

    @Column(columnDefinition = "TEXT")
    private String notas;

    // Flag que indica si los criterios fueron registrados al menos una vez
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean criteriosRegistrados = false;

    // Criterios de inclusión
    @Column(nullable = false)
    private boolean criterioTEADSMV = false;

    @Column(nullable = false)
    private boolean criterioTGDDSMIV = false;

    @Column(nullable = false)
    private boolean criterioEdad = false;

    // Criterios de exclusión
    @Column(nullable = false)
    private boolean epilepsia = false;

    @Column(nullable = false)
    private boolean paralisisCerebral = false;

    @Column(nullable = false)
    private boolean infeccionesCongenitas = false;

    @Column(nullable = false)
    private boolean lesionesEstructuralesSNC = false;

    @Column(nullable = false)
    private boolean facomatosis = false;

    @Column(nullable = false)
    private boolean patologiasNeurometabolicas = false;

    @Column(nullable = false)
    private boolean lesionesOcupantesEspacioSNC = false;

    @Column(nullable = false)
    private boolean patologiaPsiquiatrica = false;

    @Column(nullable = false)
    private boolean otrosSindromesGeneticos = false;

    @Column(nullable = false)
    private boolean pubertadPrecoz = false;

    // Escala M-CHAT-R (Sprint 3)
    private Integer mchatScoreTotal;
    private Integer mchatSeguimientoFallas;

    @Enumerated(EnumType.STRING)
    private MchatResultadoFinal mchatResultadoFinal;

    // Escala CARS-2 (Sprint 3)
    private Double carsRawScore;
    private Double carsTScore;

    // Escala Vineland (Sprint 3)
    private Integer vinelandComunicacion;
    private Integer vinelandAutovalimiento;
    private Integer vinelandSocial;
    private Integer vinelandMotor;
    private Integer vinelandCocienteFinal;
    private Integer vinelandConductaDesadaptativa;
    private Integer vinelandInternalizante;
    private Integer vinelandExternalizante;

    // Segunda visita (Sprint 3)
    private LocalDate fechaExtraccion;

    // Respuestas individuales del seguimiento M-CHAT-R/F (null si no aplica)
    private Boolean seguimientoItem1;
    private Boolean seguimientoItem2;
    private Boolean seguimientoItem3;
    private Boolean seguimientoItem4;
    private Boolean seguimientoItem5;
    private Boolean seguimientoItem6;
    private Boolean seguimientoItem7;
    private Boolean seguimientoItem8;
    private Boolean seguimientoItem9;
    private Boolean seguimientoItem10;
    private Boolean seguimientoItem11;
    private Boolean seguimientoItem12;
    private Boolean seguimientoItem13;
    private Boolean seguimientoItem14;
    private Boolean seguimientoItem15;
    private Boolean seguimientoItem16;
    private Boolean seguimientoItem17;
    private Boolean seguimientoItem18;
    private Boolean seguimientoItem19;
    private Boolean seguimientoItem20;

    // Token para el formulario M-CHAT enviado por mail
    private String mchatToken;
    private LocalDateTime mchatTokenExpiry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PacienteEstado estadoClinico = PacienteEstado.ADMITIDO;

    @Column(nullable = false)
    private boolean activo = true;

    public void refreshEstadoClinico() {
        if (fechaExtraccion != null) {
            this.estadoClinico = PacienteEstado.EXTRACCION_PENDIENTE;
            return;
        }
        if (mchatScoreTotal != null) {
            this.estadoClinico = PacienteEstado.MCHAT_RESPONDIDO;
            return;
        }
        this.estadoClinico = PacienteEstado.ADMITIDO;
        // EXTRACCION_REALIZADA se setea cuando se registra un Suero (sprint 4)
    }
}
