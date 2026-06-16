package com.utn.magtea.paciente.vineland;

import com.utn.magtea.common.Auditable;
import com.utn.magtea.paciente.Paciente;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Audited
@Entity
@Table(name = "paciente_evaluacion_vineland")
@Getter
@Setter
@NoArgsConstructor
public class PacienteEvaluacionVineland extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    private Integer comunicacion;
    private Integer autovalimiento;
    private Integer social;
    private Integer motor;
    private Integer cocienteFinal;
    private Integer conductaDesadaptativa;
    private Integer internalizante;
    private Integer externalizante;
}
