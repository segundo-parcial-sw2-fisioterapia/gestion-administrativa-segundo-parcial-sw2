package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagosRepository extends JpaRepository<Pagos, Long> {
    List<Pagos> findByFacturaId(Long facturaId);
}
