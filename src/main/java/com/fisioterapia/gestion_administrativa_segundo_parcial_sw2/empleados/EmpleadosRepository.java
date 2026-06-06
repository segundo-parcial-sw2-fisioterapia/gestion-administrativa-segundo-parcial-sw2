package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmpleadosRepository extends JpaRepository<Empleados, Long> {

    Optional<Empleados> findByPersonaId(Long personaId);

    /**
     * Busca empleados cuyo cargo o especialidad contengan el término (case-insensitive).
     * Usado por el selector FK del frontend Angular.
     */
    @Query("SELECT e FROM Empleados e WHERE " +
           "LOWER(e.cargo) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(e.especialidad) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Empleados> buscarPorCargoOEspecialidad(@Param("termino") String termino);

    List<Empleados> findByEstadoLaboral(EstadoLaboral estadoLaboral);
}
