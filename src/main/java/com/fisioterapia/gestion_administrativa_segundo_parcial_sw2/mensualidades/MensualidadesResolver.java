package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto.CrearMensualidadInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto.PagoRegistrado;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto.RegistrarPagoMensualidadInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MensualidadesResolver {

    private final MensualidadesService mensualidadesService;

    @QueryMapping
    public MensualidadesPaginadas listarMensualidades(@Argument PaginaInput pagina) {
        return mensualidadesService.listarMensualidades(pagina);
    }

    @QueryMapping
    public MensualidadesPaginadasEnriquecidas listarMensualidadesEnriquecidas(
            @Argument String pacienteId,
            @Argument EstadoMensualidad estado,
            @Argument PaginaInput pagina) {
        return mensualidadesService.listarMensualidadesEnriquecidas(pacienteId, estado, pagina);
    }

    @QueryMapping
    public Mensualidades verMensualidad(@Argument String id) {
        return mensualidadesService.verMensualidad(id);
    }

    @QueryMapping
    public List<Mensualidades> listarMensualidadesPorPaciente(@Argument String pacienteId) {
        return mensualidadesService.listarMensualidadesPorPaciente(pacienteId);
    }

    @QueryMapping
    public List<Mensualidades> listarMensualidadesPorPlan(@Argument String planId) {
        return mensualidadesService.listarMensualidadesPorPlan(planId);
    }

    @QueryMapping
    public List<Mensualidades> listarMensualidadesPendientes() {
        return mensualidadesService.listarMensualidadesPendientes();
    }

    @MutationMapping
    public Mensualidades crearMensualidades(@Argument CrearMensualidadInput input) {
        return mensualidadesService.crearMensualidades(input);
    }

    @MutationMapping
    public PagoRegistrado registrarPagoMensualidad(@Argument RegistrarPagoMensualidadInput input) {
        return mensualidadesService.registrarPagoMensualidad(input);
    }
}
