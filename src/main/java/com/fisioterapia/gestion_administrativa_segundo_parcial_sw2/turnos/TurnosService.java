package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.turnos;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.turnos.dto.CrearTurnoInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.turnos.dto.EditarTurnoInput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TurnosService {

    private final TurnosRepository turnosRepository;

    /** Lista turnos paginados, ordenados por empleado y día de semana. */
    public TurnosPaginados listarTurnos(PaginaInput pagina) {
        int p = pagina != null ? Math.max(0, pagina.getPagina()) : 0;
        int t = pagina != null ? Math.min(100, Math.max(1, pagina.getTamano())) : 20;
        Page<Turnos> page = turnosRepository.findAll(
                PageRequest.of(p, t, Sort.by("empleadoId", "diaSemana")));
        return new TurnosPaginados(page.getContent(), PaginaInfo.de(page));
    }

    /** @return El turno con el ID dado, o null si no existe. */
    public Turnos verTurno(String id) {
        return turnosRepository.findById(Long.parseLong(id)).orElse(null);
    }

    /** Lista los turnos de un empleado específico. */
    public List<Turnos> listarTurnosPorEmpleado(String empleadoId) {
        return turnosRepository.findByEmpleadoId(Long.parseLong(empleadoId));
    }

    /**
     * Registra un nuevo turno semanal para un empleado.
     *
     * @param input Datos del turno.
     * @return El turno creado.
     */
    @Transactional
    public Turnos crearTurnos(CrearTurnoInput input) {
        return turnosRepository.save(Turnos.builder()
                .empleadoId(Long.parseLong(input.getEmpleadoId()))
                .diaSemana(input.getDiaSemana())
                .horaInicio(LocalTime.parse(input.getHoraInicio()))
                .horaFin(LocalTime.parse(input.getHoraFin()))
                .build());
    }

    /**
     * Actualiza los datos de un turno existente.
     *
     * @param id    ID del turno.
     * @param input Campos a modificar.
     * @return El turno actualizado.
     */
    @Transactional
    public Turnos editarTurno(String id, EditarTurnoInput input) {
        Turnos turno = turnosRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Turno no encontrado: " + id));
        if (input.getDiaSemana() != null) turno.setDiaSemana(input.getDiaSemana());
        if (input.getHoraInicio() != null) turno.setHoraInicio(LocalTime.parse(input.getHoraInicio()));
        if (input.getHoraFin() != null) turno.setHoraFin(LocalTime.parse(input.getHoraFin()));
        if (input.getActivo() != null) turno.setActivo(input.getActivo());
        return turnosRepository.save(turno);
    }

    /**
     * Elimina un turno por su ID.
     *
     * @param id ID del turno.
     * @return true si fue eliminado.
     */
    @Transactional
    public Boolean eliminarTurno(String id) {
        turnosRepository.deleteById(Long.parseLong(id));
        return true;
    }
}
