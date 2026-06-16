package com.utn.magtea.paciente.vineland;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EvaluacionVinelandRepository extends JpaRepository<EvaluacionVineland, Long> {

    @Query("SELECT v FROM EvaluacionVineland v JOIN v.paciente p " +
           "WHERE p.activo = true AND p.tipoPaciente = 'PROBLEMA'")
    List<EvaluacionVineland> findAllProblema();
}
