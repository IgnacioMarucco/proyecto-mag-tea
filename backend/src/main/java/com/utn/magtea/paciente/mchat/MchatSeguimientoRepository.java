package com.utn.magtea.paciente.mchat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MchatSeguimientoRepository extends JpaRepository<MchatSeguimiento, Long> {

    @Query("SELECT m FROM MchatSeguimiento m JOIN m.paciente p " +
           "WHERE p.activo = true AND p.tipoPaciente = 'PROBLEMA'")
    List<MchatSeguimiento> findAllProblema();

    long countByPaciente_Activo(boolean activo);
}
