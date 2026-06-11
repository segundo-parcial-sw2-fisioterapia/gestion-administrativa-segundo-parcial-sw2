package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FacturasRepository extends JpaRepository<Facturas, Long> {
    List<Facturas> findByPacienteId(Long pacienteId);
    List<Facturas> findByMensualidadId(Long mensualidadId);
    Page<Facturas> findByFechaEmisionBetween(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);
    List<Facturas> findByFechaEmisionBetween(LocalDateTime desde, LocalDateTime hasta);
}
