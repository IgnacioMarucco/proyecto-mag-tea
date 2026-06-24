package com.utn.magtea.pool;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PoolSueroAporteRepository extends JpaRepository<PoolSueroAporte, Long> {

    boolean existsByTuboIdAndPool_ActivoTrue(Long tuboId);
}
