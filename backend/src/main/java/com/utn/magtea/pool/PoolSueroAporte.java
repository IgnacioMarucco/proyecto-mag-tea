package com.utn.magtea.pool;

import com.utn.magtea.common.Auditable;
import com.utn.magtea.tubo.Tubo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Audited
@Entity
@Table(name = "pool_suero_aportes")
@Getter
@Setter
@NoArgsConstructor
public class PoolSueroAporte extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tubo_id", nullable = false)
    private Tubo tubo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidadAportada;
}
