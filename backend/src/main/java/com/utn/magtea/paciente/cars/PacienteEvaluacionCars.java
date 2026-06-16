package com.utn.magtea.paciente.cars;

import com.utn.magtea.common.Auditable;
import com.utn.magtea.paciente.Paciente;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Audited
@Entity
@Table(name = "paciente_evaluacion_cars")
@Getter
@Setter
@NoArgsConstructor
public class PacienteEvaluacionCars extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    // Ítems CARS-2: valores válidos 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0
    @Column(precision = 2, scale = 1) private BigDecimal item1;
    @Column(precision = 2, scale = 1) private BigDecimal item2;
    @Column(precision = 2, scale = 1) private BigDecimal item3;
    @Column(precision = 2, scale = 1) private BigDecimal item4;
    @Column(precision = 2, scale = 1) private BigDecimal item5;
    @Column(precision = 2, scale = 1) private BigDecimal item6;
    @Column(precision = 2, scale = 1) private BigDecimal item7;
    @Column(precision = 2, scale = 1) private BigDecimal item8;
    @Column(precision = 2, scale = 1) private BigDecimal item9;
    @Column(precision = 2, scale = 1) private BigDecimal item10;
    @Column(precision = 2, scale = 1) private BigDecimal item11;
    @Column(precision = 2, scale = 1) private BigDecimal item12;
    @Column(precision = 2, scale = 1) private BigDecimal item13;
    @Column(precision = 2, scale = 1) private BigDecimal item14;
    @Column(precision = 2, scale = 1) private BigDecimal item15;

    @Column(columnDefinition = "TEXT") private String obs1;
    @Column(columnDefinition = "TEXT") private String obs2;
    @Column(columnDefinition = "TEXT") private String obs3;
    @Column(columnDefinition = "TEXT") private String obs4;
    @Column(columnDefinition = "TEXT") private String obs5;
    @Column(columnDefinition = "TEXT") private String obs6;
    @Column(columnDefinition = "TEXT") private String obs7;
    @Column(columnDefinition = "TEXT") private String obs8;
    @Column(columnDefinition = "TEXT") private String obs9;
    @Column(columnDefinition = "TEXT") private String obs10;
    @Column(columnDefinition = "TEXT") private String obs11;
    @Column(columnDefinition = "TEXT") private String obs12;
    @Column(columnDefinition = "TEXT") private String obs13;
    @Column(columnDefinition = "TEXT") private String obs14;
    @Column(columnDefinition = "TEXT") private String obs15;

    // Suma de los 15 ítems — snapshot clínico persistido
    @Column(precision = 4, scale = 1)
    private BigDecimal rawScore;

    // Valores obtenidos de tabla de normas (ingresados manualmente)
    @Column(precision = 5, scale = 2)
    private BigDecimal tScore;

    private Integer percentil;
}
