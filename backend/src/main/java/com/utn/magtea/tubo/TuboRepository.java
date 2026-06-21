package com.utn.magtea.tubo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TuboRepository extends JpaRepository<Tubo, Long> {

    List<Tubo> findBySueroId(Long sueroId);

    List<Tubo> findByPoolId(Long poolId);

    List<Tubo> findBySueroActivoTrue();

    List<Tubo> findByCajaIdAndSueroActivoTrue(Long cajaId);

    List<Tubo> findByCajaIdAndPoolActivoTrue(Long cajaId);
}
