package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.asistencias;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsistenciasRepository extends JpaRepository<Asistencias, Long> {
    List<Asistencias> findByEmpleadoId(Long empleadoId);
}
