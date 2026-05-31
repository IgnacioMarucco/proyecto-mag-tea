package com.utn.magtea.profesional;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfesionalRepository extends JpaRepository<Profesional, Long> {
    Optional<Profesional> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Profesional> findAllByActivoTrue();
}
