package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.documentos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentosRepository extends JpaRepository<Documentos, Long> {
    List<Documentos> findByReferenciaEntidad(Long referenciaEntidadId);
}
