package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensualidadesRepository extends JpaRepository<Mensualidades, Long> {

    List<Mensualidades> findByPacienteId(Long pacienteId);
    Page<Mensualidades> findByPacienteId(Long pacienteId, Pageable pageable);

    List<Mensualidades> findByPlanTratamientoId(Long planTratamientoId);

    List<Mensualidades> findByEstado(EstadoMensualidad estado);
    Page<Mensualidades> findByEstado(EstadoMensualidad estado, Pageable pageable);

    List<Mensualidades> findByPacienteIdAndEstado(Long pacienteId, EstadoMensualidad estado);
    Page<Mensualidades> findByPacienteIdAndEstado(Long pacienteId, EstadoMensualidad estado, Pageable pageable);
}
