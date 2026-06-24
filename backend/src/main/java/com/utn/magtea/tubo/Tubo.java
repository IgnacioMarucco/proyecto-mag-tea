package com.utn.magtea.tubo;

import com.utn.magtea.caja.Caja;
import com.utn.magtea.common.Auditable;
import com.utn.magtea.pool.Pool;
import com.utn.magtea.suero.Suero;
import jakarta.persistence.*;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Table(
    name = "tubos",
    uniqueConstraints = @UniqueConstraint(name = "uc_tubo_caja_posicion", columnNames = {"caja_id", "posicion"})
)
@Getter
@Setter
@NoArgsConstructor
public class Tubo extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTubo tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suero_id")
    private Suero suero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id")
    private Pool pool;

    @Column(nullable = true)
    private String posicion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadInicial;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadUsada = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private MotivoVaciado motivoVaciado;

    @Column(nullable = true, length = 500)
    private String notasVaciado;

    public BigDecimal getCantidadRestante() {
        return cantidadInicial.subtract(cantidadUsada);
    }
}
