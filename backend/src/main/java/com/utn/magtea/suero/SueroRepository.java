package com.utn.magtea.suero;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SueroRepository extends JpaRepository<Suero, Long>, JpaSpecificationExecutor<Suero> {

    boolean existsByPacienteIdAndActivoTrue(Long pacienteId);

    List<Suero> findByCajaIdAndActivoTrue(Long cajaId);

    java.util.Optional<Suero> findByPacienteCodigoNumericoAndActivoTrue(String codigoNumerico);
}
