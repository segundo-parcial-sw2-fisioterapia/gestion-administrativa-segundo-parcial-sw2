package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.dto.CrearEmpleadoInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.dto.EditarEmpleadoInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EmpleadosResolver {

    private final EmpleadosService empleadosService;

    @QueryMapping
    public EmpleadosPaginados listarEmpleados(@Argument PaginaInput pagina) {
        return empleadosService.listarEmpleados(pagina);
    }

    @QueryMapping
    public Empleados verEmpleado(@Argument String id) {
        return empleadosService.verEmpleado(id);
    }

    @QueryMapping
    public List<Empleados> buscarEmpleados(@Argument String termino) {
        return empleadosService.buscarEmpleados(termino);
    }

    @QueryMapping
    public Empleados verEmpleadoPorPersonaId(@Argument String personaId) {
        return empleadosService.verEmpleadoPorPersonaId(personaId);
    }

    @MutationMapping
    public Empleados crearEmpleados(@Argument CrearEmpleadoInput input) {
        return empleadosService.crearEmpleados(input);
    }

    @MutationMapping
    public Empleados editarEmpleado(@Argument String id, @Argument EditarEmpleadoInput input) {
        return empleadosService.editarEmpleado(id, input);
    }

    @MutationMapping
    public Boolean eliminarEmpleado(@Argument String id) {
        return empleadosService.eliminarEmpleado(id);
    }
}
