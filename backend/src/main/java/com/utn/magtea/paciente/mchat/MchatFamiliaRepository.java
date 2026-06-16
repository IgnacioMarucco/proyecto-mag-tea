package com.utn.magtea.paciente.mchat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MchatFamiliaRepository extends JpaRepository<MchatFamilia, Long> {
    Optional<MchatFamilia> findByPaciente_Id(Long pacienteId);
    boolean existsByPaciente_Id(Long pacienteId);
}
