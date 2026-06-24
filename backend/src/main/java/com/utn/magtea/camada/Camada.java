package com.utn.magtea.camada;

import com.utn.magtea.common.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;

@Audited
@Entity
@Table(name = "camadas")
@Getter
@Setter
@NoArgsConstructor
public class Camada extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private boolean activo = true;
}
