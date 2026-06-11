package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.ReporteFinanciero;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico.CatalogoReportes;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico.ReporteDinamico;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico.ReporteDinamicoInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico.ReporteIA;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolver GraphQL de reportes del subsistema de gestión empresarial (BI administrativo).
 * El frontend Angular consume estas queries a través del gateway federado.
 */
@Controller
@RequiredArgsConstructor
public class ReportesResolver {

    private final ReportesService reportesService;
    private final ReporteDinamicoService reporteDinamicoService;
    private final ReporteIaService reporteIaService;
    private final ReporteExportacionService reporteExportacionService;

    /**
     * Reporte financiero anual con ingresos por mes, desglose por método de pago,
     * ticket promedio y estado de cobranza de mensualidades.
     */
    @QueryMapping
    public ReporteFinanciero reporteFinanciero(@Argument Integer anio) {
        return reportesService.reporteFinanciero(anio);
    }

    /**
     * Catálogo de fuentes, dimensiones y métricas disponibles para construir reportes
     * dinámicos. Alimenta tanto el constructor manual del frontend como la capa de IA.
     */
    @QueryMapping
    public CatalogoReportes catalogoReportes() {
        return reporteDinamicoService.catalogo();
    }

    /**
     * Ejecuta un reporte dinámico parametrizable: fuente + métrica + dimensión + filtros.
     * Un único resolver cubre N reportes sobre datos reales.
     */
    @QueryMapping
    public ReporteDinamico reporteDinamico(@Argument ReporteDinamicoInput input) {
        return reporteDinamicoService.ejecutar(input);
    }

    /**
     * Genera un reporte a partir de un prompt en lenguaje natural usando OpenAI para
     * interpretar la solicitud y el motor determinista para ejecutarla sobre datos reales.
     */
    @QueryMapping
    public ReporteIA reportePorPrompt(@Argument String prompt) {
        return reporteIaService.generarDesdePrompt(prompt);
    }

    @org.springframework.graphql.data.method.annotation.MutationMapping
    public String exportarReportePdf(@Argument ReporteDinamicoInput input) {
        return reporteExportacionService.exportarPdf(input);
    }

    @org.springframework.graphql.data.method.annotation.MutationMapping
    public String exportarReporteExcel(@Argument ReporteDinamicoInput input) {
        return reporteExportacionService.exportarExcel(input);
    }
}
