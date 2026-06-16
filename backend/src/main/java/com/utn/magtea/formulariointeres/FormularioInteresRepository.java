package com.utn.magtea.formulariointeres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FormularioInteresRepository extends JpaRepository<FormularioInteres, Long>,
        JpaSpecificationExecutor<FormularioInteres> {

    long countByActivoTrue();

    long countByEstadoAndActivoTrue(EstadoFormulario estado);

    @Query("SELECT f.comoConocioProyecto, COUNT(f) FROM FormularioInteres f " +
           "WHERE f.activo = true AND f.comoConocioProyecto IS NOT NULL " +
           "GROUP BY f.comoConocioProyecto ORDER BY COUNT(f) DESC")
    List<Object[]> distribucionComoConocio();
}
