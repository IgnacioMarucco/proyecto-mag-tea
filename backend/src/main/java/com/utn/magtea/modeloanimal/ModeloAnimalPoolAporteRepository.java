package com.utn.magtea.modeloanimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ModeloAnimalPoolAporteRepository extends JpaRepository<ModeloAnimalPoolAporte, Long> {

    Optional<ModeloAnimalPoolAporte> findByModeloAnimal_IdAndDia(Long modeloAnimalId, Integer dia);

    @Transactional
    void deleteByModeloAnimal_IdAndDia(Long modeloAnimalId, Integer dia);
}
