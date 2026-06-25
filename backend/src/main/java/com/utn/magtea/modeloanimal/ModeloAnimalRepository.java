package com.utn.magtea.modeloanimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModeloAnimalRepository extends JpaRepository<ModeloAnimal, Long>, JpaSpecificationExecutor<ModeloAnimal> {
    long countByPool_Id(Long poolId);
    long countByPool_IdAndActivoTrue(Long poolId);
    boolean existsByCamada_IdAndActivoTrue(Long camadaId);
    java.util.Optional<ModeloAnimal> findByIdentificadorAndActivoTrue(String identificador);

    @Query("""
        SELECT DISTINCT m FROM ModeloAnimal m
        JOIN FETCH m.pool p
        JOIN FETCH p.caja
        LEFT JOIN FETCH p.aportes a
        LEFT JOIN FETCH a.tubo t
        LEFT JOIN FETCH t.suero s
        LEFT JOIN FETCH s.paciente pac
        LEFT JOIN FETCH pac.mchatFamilia
        LEFT JOIN FETCH pac.mchatSeguimiento
        LEFT JOIN FETCH pac.evaluacionCars
        LEFT JOIN FETCH pac.evaluacionVineland
        JOIN FETCH m.camada
        LEFT JOIN FETCH m.vocalizaciones
        LEFT JOIN FETCH m.tresCamaras
        WHERE m.identificador = :identificador AND m.activo = true
    """)
    java.util.Optional<ModeloAnimal> findByIdentificadorForReporte(@Param("identificador") String identificador);
}
