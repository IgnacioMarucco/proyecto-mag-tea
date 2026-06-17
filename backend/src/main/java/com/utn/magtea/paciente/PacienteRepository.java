package com.utn.magtea.paciente;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long>, JpaSpecificationExecutor<Paciente> {
    boolean existsByCodigoNumerico(String codigoNumerico);
    Optional<Paciente> findByMchatToken(String mchatToken);

    List<Paciente> findByActivoTrue();

    @Override
    @EntityGraph(attributePaths = {
            "mchatFamilia", "mchatSeguimiento", "evaluacionCars", "evaluacionVineland", "criterios"
    })
    List<Paciente> findAll(Specification<Paciente> spec);

    long countByActivoTrue();
    long countByTipoPacienteAndActivoTrue(TipoPaciente tipo);

    @Query("SELECT p.sexo, COUNT(p) FROM Paciente p " +
           "WHERE p.activo = true AND p.tipoPaciente = 'PROBLEMA' GROUP BY p.sexo")
    List<Object[]> countBySexo();

    @Query("SELECT p FROM Paciente p " +
           "WHERE p.activo = true AND p.tipoPaciente = 'PROBLEMA' " +
           "AND p.fechaNacimientoNino IS NOT NULL AND p.fechaPrimeraVisita IS NOT NULL")
    List<Paciente> findProblemaConEdadCalculable();

    @Query("SELECT COUNT(p) FROM Paciente p " +
           "WHERE p.activo = true AND p.tipoPaciente = 'PROBLEMA' " +
           "AND p.estadoClinico IN ('MCHAT_RESPONDIDO', 'EXTRACCION_PENDIENTE', 'EXTRACCION_REALIZADA')")
    long countMchatCompletados();

    @Query("SELECT COUNT(p) FROM Paciente p " +
           "WHERE p.activo = true AND p.tipoPaciente = 'PROBLEMA' " +
           "AND p.estadoClinico IN ('EXTRACCION_PENDIENTE', 'EXTRACCION_REALIZADA')")
    long countExtraccionPendiente();

    @Query("SELECT COUNT(p) FROM Paciente p " +
           "WHERE p.activo = true AND p.tipoPaciente = 'PROBLEMA' " +
           "AND p.estadoClinico = 'EXTRACCION_REALIZADA'")
    long countExtraccionRealizada();
}
