package com.utn.magtea.pool;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PoolRepository extends JpaRepository<Pool, Long>, JpaSpecificationExecutor<Pool> {

    List<Pool> findByCajaIdAndActivoTrue(Long cajaId);
}
