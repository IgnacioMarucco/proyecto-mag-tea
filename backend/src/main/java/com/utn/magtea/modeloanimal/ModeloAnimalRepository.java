package com.utn.magtea.modeloanimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ModeloAnimalRepository extends JpaRepository<ModeloAnimal, Long>, JpaSpecificationExecutor<ModeloAnimal> {
    long countByPool_Id(Long poolId);
    long countByPool_IdAndActivoTrue(Long poolId);
    boolean existsByCamada_IdAndActivoTrue(Long camadaId);
    java.util.Optional<ModeloAnimal> findByIdentificadorAndActivoTrue(String identificador);
}
