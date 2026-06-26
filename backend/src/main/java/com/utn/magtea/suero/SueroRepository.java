package com.utn.magtea.suero;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SueroRepository extends JpaRepository<Suero, Long>, JpaSpecificationExecutor<Suero> {

    boolean existsByPacienteIdAndActivoTrue(Long pacienteId);

    List<Suero> findByCajaIdAndActivoTrue(Long cajaId);

    java.util.Optional<Suero> findByPacienteCodigoNumericoAndActivoTrue(String codigoNumerico);

    List<Suero> findAllByPacienteIdInAndActivoTrue(List<Long> pacienteIds);

    List<Suero> findTop3ByActivoTrueOrderByCreatedAtDesc();

    @Query("SELECT s FROM Suero s JOIN FETCH s.paciente WHERE s.activo = true")
    List<Suero> findAllForExport();
}
