package com.utn.magtea.modeloanimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ModeloAnimalRepository extends JpaRepository<ModeloAnimal, Long>, JpaSpecificationExecutor<ModeloAnimal> {}
