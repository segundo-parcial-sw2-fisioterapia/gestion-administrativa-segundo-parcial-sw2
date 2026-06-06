package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.turnos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TurnosRepository extends JpaRepository<Turnos, Long> {
    List<Turnos> findByEmpleadoId(Long empleadoId);
    List<Turnos> findByEmpleadoIdAndActivoTrue(Long empleadoId);
}
