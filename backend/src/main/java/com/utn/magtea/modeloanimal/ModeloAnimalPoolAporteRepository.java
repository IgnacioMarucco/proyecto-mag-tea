package com.utn.magtea.modeloanimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ModeloAnimalPoolAporteRepository extends JpaRepository<ModeloAnimalPoolAporte, Long> {

    Optional<ModeloAnimalPoolAporte> findByModeloAnimal_IdAndDia(Long modeloAnimalId, Integer dia);

    @Transactional
    void deleteByModeloAnimal_IdAndDia(Long modeloAnimalId, Integer dia);

    @org.springframework.data.jpa.repository.Query("""
        SELECT a FROM ModeloAnimalPoolAporte a
        JOIN FETCH a.modeloAnimal m
        ORDER BY a.createdAt DESC
        LIMIT 3
    """)
    List<ModeloAnimalPoolAporte> findTop3ForActividad();
}
