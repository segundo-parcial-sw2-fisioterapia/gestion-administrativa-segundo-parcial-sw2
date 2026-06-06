package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.asistencias;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.asistencias.dto.CrearAsistenciaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.asistencias.dto.EditarAsistenciaInput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AsistenciasService {

    private final AsistenciasRepository asistenciasRepository;

    /** Lista asistencias paginadas, ordenadas por fecha descendente. */
    public AsistenciasPaginadas listarAsistencias(PaginaInput pagina) {
        int p = pagina != null ? Math.max(0, pagina.getPagina()) : 0;
        int t = pagina != null ? Math.min(100, Math.max(1, pagina.getTamano())) : 20;
        Page<Asistencias> page = asistenciasRepository.findAll(
                PageRequest.of(p, t, Sort.by("fecha").descending()));
        return new AsistenciasPaginadas(page.getContent(), PaginaInfo.de(page));
    }

    /** @return El registro de asistencia con el ID dado, o null si no existe. */
    public Asistencias verAsistencia(String id) {
        return asistenciasRepository.findById(Long.parseLong(id)).orElse(null);
    }

    /** Lista todos los registros de asistencia de un empleado. */
    public List<Asistencias> listarAsistenciasPorEmpleado(String empleadoId) {
        return asistenciasRepository.findByEmpleadoId(Long.parseLong(empleadoId));
    }

    /**
     * Registra la asistencia diaria de un empleado.
     *
     * @param input Datos del registro de asistencia.
     * @return El registro creado.
     */
    @Transactional
    public Asistencias crearAsistencias(CrearAsistenciaInput input) {
        return asistenciasRepository.save(Asistencias.builder()
                .empleadoId(Long.parseLong(input.getEmpleadoId()))
                .fecha(LocalDate.parse(input.getFecha()))
                .horaEntrada(LocalTime.parse(input.getHoraEntrada()))
                .horaSalida(input.getHoraSalida() != null ? LocalTime.parse(input.getHoraSalida()) : null)
                .estado(input.getEstado())
                .build());
    }

    /**
     * Actualiza un registro de asistencia (ej: registrar hora de salida).
     *
     * @param id    ID del registro.
     * @param input Campos a modificar.
     * @return El registro actualizado.
     */
    @Transactional
    public Asistencias editarAsistencia(String id, EditarAsistenciaInput input) {
        Asistencias a = asistenciasRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada: " + id));
        if (input.getHoraEntrada() != null) a.setHoraEntrada(LocalTime.parse(input.getHoraEntrada()));
        if (input.getHoraSalida() != null) a.setHoraSalida(LocalTime.parse(input.getHoraSalida()));
        if (input.getEstado() != null) a.setEstado(input.getEstado());
        return asistenciasRepository.save(a);
    }

    /**
     * Elimina un registro de asistencia por su ID.
     *
     * @param id ID del registro.
     * @return true si fue eliminado.
     */
    @Transactional
    public Boolean eliminarAsistencia(String id) {
        asistenciasRepository.deleteById(Long.parseLong(id));
        return true;
    }
}
