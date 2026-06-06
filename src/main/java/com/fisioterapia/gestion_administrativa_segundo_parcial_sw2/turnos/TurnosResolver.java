package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.turnos;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.turnos.dto.CrearTurnoInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.turnos.dto.EditarTurnoInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TurnosResolver {

    private final TurnosService turnosService;

    @QueryMapping
    public TurnosPaginados listarTurnos(@Argument PaginaInput pagina) {
        return turnosService.listarTurnos(pagina);
    }

    @QueryMapping
    public Turnos verTurno(@Argument String id) {
        return turnosService.verTurno(id);
    }

    @QueryMapping
    public List<Turnos> listarTurnosPorEmpleado(@Argument String empleadoId) {
        return turnosService.listarTurnosPorEmpleado(empleadoId);
    }

    @MutationMapping
    public Turnos crearTurnos(@Argument CrearTurnoInput input) {
        return turnosService.crearTurnos(input);
    }

    @MutationMapping
    public Turnos editarTurno(@Argument String id, @Argument EditarTurnoInput input) {
        return turnosService.editarTurno(id, input);
    }

    @MutationMapping
    public Boolean eliminarTurno(@Argument String id) {
        return turnosService.eliminarTurno(id);
    }
}
