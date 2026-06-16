package com.utn.magtea.paciente.mchat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MchatFamiliaRepository extends JpaRepository<MchatFamilia, Long> {
    Optional<MchatFamilia> findByPaciente_Id(Long pacienteId);
    boolean existsByPaciente_Id(Long pacienteId);

    @Query("SELECT m FROM MchatFamilia m JOIN m.paciente p " +
           "WHERE p.activo = true AND p.tipoPaciente = 'PROBLEMA'")
    List<MchatFamilia> findAllProblema();

    @Query("SELECT m.scoreTotal, COUNT(m) FROM MchatFamilia m JOIN m.paciente p " +
           "WHERE p.activo = true AND p.tipoPaciente = 'PROBLEMA' " +
           "GROUP BY m.scoreTotal ORDER BY m.scoreTotal")
    List<Object[]> distribucionScores();

    @Query("SELECT m.resultadoFinal, COUNT(m) FROM MchatFamilia m JOIN m.paciente p " +
           "WHERE p.activo = true AND p.tipoPaciente = 'PROBLEMA' AND m.resultadoFinal IS NOT NULL " +
           "GROUP BY m.resultadoFinal")
    List<Object[]> distribucionResultadoFinal();
}
