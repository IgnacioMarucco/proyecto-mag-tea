package com.utn.magtea.mchat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MchatRespuestasRepository extends JpaRepository<MchatRespuestas, Long> {
    Optional<MchatRespuestas> findByPacienteId(Long pacienteId);
    boolean existsByPacienteId(Long pacienteId);
}
