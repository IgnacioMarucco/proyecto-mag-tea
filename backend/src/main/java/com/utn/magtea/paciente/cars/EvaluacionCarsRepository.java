package com.utn.magtea.paciente.cars;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EvaluacionCarsRepository extends JpaRepository<EvaluacionCars, Long> {

    @Query("SELECT e FROM EvaluacionCars e JOIN e.paciente p " +
           "WHERE p.activo = true AND p.tipoPaciente = 'PROBLEMA'")
    List<EvaluacionCars> findAllProblema();

    @Query("SELECT " +
           "SUM(CASE WHEN e.rawScore < 30 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN e.rawScore >= 30 AND e.rawScore <= 36.5 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN e.rawScore > 36.5 THEN 1 ELSE 0 END) " +
           "FROM EvaluacionCars e JOIN e.paciente p " +
           "WHERE p.activo = true AND p.tipoPaciente = 'PROBLEMA' AND e.rawScore IS NOT NULL")
    Object[] categoriasCars();
}
