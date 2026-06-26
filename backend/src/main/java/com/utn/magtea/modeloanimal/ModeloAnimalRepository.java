package com.utn.magtea.modeloanimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ModeloAnimalRepository extends JpaRepository<ModeloAnimal, Long>, JpaSpecificationExecutor<ModeloAnimal> {
    long countByPool_Id(Long poolId);
    long countByPool_IdAndActivoTrue(Long poolId);
    boolean existsByCamada_IdAndActivoTrue(Long camadaId);
    java.util.Optional<ModeloAnimal> findByIdentificadorAndActivoTrue(String identificador);

    @Query("""
        SELECT DISTINCT m FROM ModeloAnimal m
        JOIN FETCH m.camada c
        JOIN FETCH m.pool p
        LEFT JOIN FETCH m.aportes
        WHERE m.activo = true
          AND m.fechaDia1Inoculacion IS NOT NULL
          AND m.fechaDia1Inoculacion >= :desde
          AND m.fechaDia1Inoculacion <= :hasta
    """)
    List<ModeloAnimal> findForAgendaInoculacion(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("""
        SELECT m FROM ModeloAnimal m
        JOIN FETCH m.camada c
        LEFT JOIN FETCH m.vocalizaciones
        LEFT JOIN FETCH m.tresCamaras
        WHERE m.activo = true
          AND c.fechaNacimiento IS NOT NULL
          AND (
            (m.vocalizaciones IS NULL AND c.fechaNacimiento <= :umbralVocalizaciones)
            OR
            (m.tresCamaras IS NULL AND c.fechaNacimiento <= :umbralTresCamaras)
          )
    """)
    List<ModeloAnimal> findForAlertasConductuales(
            @Param("umbralVocalizaciones") LocalDate umbralVocalizaciones,
            @Param("umbralTresCamaras") LocalDate umbralTresCamaras);

    @Query("""
        SELECT m FROM ModeloAnimal m
        JOIN FETCH m.camada c
        LEFT JOIN FETCH m.vocalizaciones
        LEFT JOIN FETCH m.tresCamaras
        WHERE m.activo = true
          AND c.fechaNacimiento IS NOT NULL
          AND c.fechaNacimiento BETWEEN :fnDesde AND :fnHasta
    """)
    List<ModeloAnimal> findByCamadaFechaNacimientoBetween(
            @Param("fnDesde") LocalDate fnDesde,
            @Param("fnHasta") LocalDate fnHasta);

    List<ModeloAnimal> findTop3ByActivoTrueOrderByCreatedAtDesc();

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

    @Query("""
        SELECT DISTINCT m FROM ModeloAnimal m
        JOIN FETCH m.pool p
        JOIN FETCH m.camada c
        LEFT JOIN FETCH m.vocalizaciones
        LEFT JOIN FETCH m.tresCamaras
        WHERE m.activo = true
    """)
    List<ModeloAnimal> findAllForExport();
}
