package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.tarifas;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TarifasRepository extends JpaRepository<Tarifas, Long> {

    Optional<Tarifas> findByCategoriaSemaforoAndNivel(CategoriaSemaforoTarifa semaforo, NivelIntensidad nivel);

    List<Tarifas> findAll();
}
