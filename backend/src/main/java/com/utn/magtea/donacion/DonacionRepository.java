package com.utn.magtea.donacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DonacionRepository extends JpaRepository<Donacion, Long> {

    Optional<Donacion> findByMpPreferenceId(String mpPreferenceId);
}
