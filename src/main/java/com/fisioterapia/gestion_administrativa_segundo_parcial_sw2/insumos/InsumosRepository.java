package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InsumosRepository extends JpaRepository<Insumos, Long> {

    /** Retorna insumos cuyo stock actual es menor o igual al stock mínimo configurado. */
    @Query("SELECT i FROM Insumos i WHERE i.stockActual <= i.stockMinimo AND i.activo = true")
    List<Insumos> findInsumosConStockBajo();
}
