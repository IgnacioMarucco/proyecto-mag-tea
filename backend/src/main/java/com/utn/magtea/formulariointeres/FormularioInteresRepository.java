package com.utn.magtea.formulariointeres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FormularioInteresRepository extends JpaRepository<FormularioInteres, Long>,
        JpaSpecificationExecutor<FormularioInteres> {

    long countByEstadoAndActivoTrue(EstadoFormulario estado);
}
