package com.utn.magtea.profesional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProfesionalRepository extends JpaRepository<Profesional, Long>,
        JpaSpecificationExecutor<Profesional> {
    Optional<Profesional> findByEmail(String email);
    boolean existsByEmail(String email);
}
