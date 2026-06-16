package com.utn.magtea.paciente.criterios;

import com.utn.magtea.common.Auditable;
import com.utn.magtea.paciente.Paciente;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Audited
@Entity
@Table(name = "paciente_criterios")
@Getter
@Setter
@NoArgsConstructor
public class PacienteCriterios extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

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
}
