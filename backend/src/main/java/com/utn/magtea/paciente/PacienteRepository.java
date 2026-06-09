package com.utn.magtea.paciente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long>, JpaSpecificationExecutor<Paciente> {
    boolean existsByCodigoNumerico(String codigoNumerico);
    Optional<Paciente> findByMchatToken(String mchatToken);
}
