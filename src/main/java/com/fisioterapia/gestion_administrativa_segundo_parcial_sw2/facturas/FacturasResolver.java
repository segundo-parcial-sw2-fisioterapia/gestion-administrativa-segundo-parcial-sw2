package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.CrearFacturaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.EditarFacturaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.FacturaEnriquecida;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FacturasResolver {

    private final FacturasService facturasService;

    @QueryMapping
    public FacturasPaginadas listarFacturas(@Argument PaginaInput pagina) {
        return facturasService.listarFacturas(pagina);
    }

    /**
     * Lista facturas enriquecidas (con nombre del paciente) filtradas por mes y año.
     * Si mes/anio son null, retorna todas las facturas paginadas.
     */
    @QueryMapping
    public FacturasEnriquecidaPaginadas listarFacturasEnriquecidas(
            @Argument Integer mes,
            @Argument Integer anio,
            @Argument PaginaInput pagina) {
        return facturasService.listarFacturasEnriquecidas(mes, anio, pagina);
    }

    @QueryMapping
    public Facturas verFactura(@Argument String id) {
        return facturasService.verFactura(id);
    }

    @QueryMapping
    public List<Facturas> listarFacturasPorPaciente(@Argument String pacienteId) {
        return facturasService.listarFacturasPorPaciente(pacienteId);
    }

    @QueryMapping
    public List<Facturas> listarFacturasPorMensualidad(@Argument String mensualidadId) {
        return facturasService.listarFacturasPorMensualidad(mensualidadId);
    }

    @QueryMapping
    public FacturaEnriquecida verFacturaEnriquecida(@Argument String id) {
        return facturasService.verFacturaEnriquecida(id);
    }

    @MutationMapping
    public Facturas crearFacturas(@Argument CrearFacturaInput input) {
        return facturasService.crearFacturas(input);
    }

    @MutationMapping
    public Facturas editarFactura(@Argument String id, @Argument EditarFacturaInput input) {
        return facturasService.editarFactura(id, input);
    }

    @MutationMapping
    public Facturas anularFactura(@Argument String id) {
        return facturasService.anularFactura(id);
    }

    @MutationMapping
    public Boolean eliminarFactura(@Argument String id) {
        return facturasService.eliminarFactura(id);
    }

    @MutationMapping
    public Facturas registrarFacturaEnBlockchain(@Argument String id) {
        return facturasService.registrarFacturaEnBlockchain(id);
    }

    /**
     * Genera un PDF de la factura y lo retorna como data URI base64.
     * El frontend usa esto para imprimir o descargar.
     */
    @MutationMapping
    public String generarPdfFactura(@Argument String id) {
        return facturasService.generarPdfFactura(id);
    }
}
