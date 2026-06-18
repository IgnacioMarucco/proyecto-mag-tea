package com.utn.magtea.camada;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CamadaRepository extends JpaRepository<Camada, Long>, JpaSpecificationExecutor<Camada> {

    boolean existsByNombreAndActivoTrue(String nombre);

    boolean existsByNombreAndActivoTrueAndIdNot(String nombre, Long id);
}
