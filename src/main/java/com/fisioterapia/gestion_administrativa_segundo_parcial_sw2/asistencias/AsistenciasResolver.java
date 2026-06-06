package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.asistencias;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.asistencias.dto.CrearAsistenciaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.asistencias.dto.EditarAsistenciaInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AsistenciasResolver {

    private final AsistenciasService asistenciasService;

    @QueryMapping
    public AsistenciasPaginadas listarAsistencias(@Argument PaginaInput pagina) {
        return asistenciasService.listarAsistencias(pagina);
    }

    @QueryMapping
    public Asistencias verAsistencia(@Argument String id) {
        return asistenciasService.verAsistencia(id);
    }

    @QueryMapping
    public List<Asistencias> listarAsistenciasPorEmpleado(@Argument String empleadoId) {
        return asistenciasService.listarAsistenciasPorEmpleado(empleadoId);
    }

    @MutationMapping
    public Asistencias crearAsistencias(@Argument CrearAsistenciaInput input) {
        return asistenciasService.crearAsistencias(input);
    }

    @MutationMapping
    public Asistencias editarAsistencia(@Argument String id, @Argument EditarAsistenciaInput input) {
        return asistenciasService.editarAsistencia(id, input);
    }

    @MutationMapping
    public Boolean eliminarAsistencia(@Argument String id) {
        return asistenciasService.eliminarAsistencia(id);
    }
}
