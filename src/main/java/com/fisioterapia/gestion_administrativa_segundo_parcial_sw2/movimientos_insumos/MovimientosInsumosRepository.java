package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.movimientos_insumos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientosInsumosRepository extends JpaRepository<MovimientosInsumos, Long> {
    List<MovimientosInsumos> findByInsumoIdOrderByFechaDesc(Long insumoId);
}
