package com.utn.magtea.suero;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.common.Auditable;
import com.utn.magtea.paciente.Paciente;
import com.utn.magtea.tubo.Tubo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Audited
@Entity
@Table(name = "sueros")
@Getter
@Setter
@NoArgsConstructor
public class Suero extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    @OneToMany(mappedBy = "suero", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tubo> tubos = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate fechaExtraccion;

    @Column(precision = 12, scale = 4)
    private BigDecimal valorAnticuerpos;

    private Integer rango;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SueroUso uso;

    @Column(nullable = false)
    private boolean activo = true;
}
