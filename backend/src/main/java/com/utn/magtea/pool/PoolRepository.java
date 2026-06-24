package com.utn.magtea.pool;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PoolRepository extends JpaRepository<Pool, Long>, JpaSpecificationExecutor<Pool> {

    List<Pool> findByCajaIdAndActivoTrue(Long cajaId);

    boolean existsByCodigo(String codigo);

    Optional<Pool> findByCodigoAndActivoTrue(String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pool p WHERE p.id = :id")
    Optional<Pool> findByIdForUpdate(@Param("id") Long id);
}
