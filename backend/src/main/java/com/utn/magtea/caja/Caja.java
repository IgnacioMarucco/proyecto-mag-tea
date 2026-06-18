package com.utn.magtea.caja;

import com.utn.magtea.common.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Table(name = "cajas")
@Getter
@Setter
@NoArgsConstructor
public class Caja extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String freezer;

    @Column(nullable = false)
    private Integer cajon;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private boolean activo = true;
}
