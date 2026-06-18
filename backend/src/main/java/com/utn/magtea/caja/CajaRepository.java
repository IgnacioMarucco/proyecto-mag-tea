package com.utn.magtea.caja;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CajaRepository extends JpaRepository<Caja, Long>, JpaSpecificationExecutor<Caja> {

    boolean existsByFreezerAndCajonAndNumeroAndActivoTrue(String freezer, Integer cajon, Integer numero);

    boolean existsByFreezerAndCajonAndNumeroAndActivoTrueAndIdNot(String freezer, Integer cajon, Integer numero, Long id);
}
