package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.OpenAiClient;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.StringJoiner;

/**
 * Capa de IA del módulo de reportes: traduce un prompt en lenguaje natural a una
 * {@link ReporteDinamicoInput} y delega la ejecución en el motor determinista.
 *
 * El modelo SOLO elige entre el catálogo whitelisteado (fuentes, dimensiones, métricas);
 * el backend valida la respuesta y ejecuta la consulta. La IA nunca genera SQL ni accede
 * a la base de datos, lo que evita inyección y consultas a entidades no permitidas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteIaService {

    private final OpenAiClient openAiClient;
    private final ReporteDinamicoService motor;
    private final ObjectMapper mapper;

    /**
     * Genera un reporte a partir de un prompt en lenguaje natural.
     *
     * @param prompt Petición del usuario, ej. "ingresos por método de pago en 2026".
     * @return La interpretación de la IA, el reporte ejecutado y un resumen narrativo.
     */
    public ReporteIA generarDesdePrompt(String prompt) {
        if (prompt == null || prompt.isBlank())
            throw new IllegalArgumentException("El prompt no puede estar vacío.");

        String system = construirSystemPrompt();
        String json = openAiClient.completarJson(system, prompt.trim());

        ReporteDinamicoInput interpretacion;
        try {
            interpretacion = mapper.readValue(json, ReporteDinamicoInput.class);
        } catch (Exception e) {
            log.error("Respuesta de IA no parseable: {}", json);
            throw new RuntimeException("La IA devolvió una especificación inválida.", e);
        }

        // El motor valida contra el catálogo y lanza si algún campo no está permitido.
        ReporteDinamico resultado = motor.ejecutar(interpretacion);
        String narrativa = construirNarrativa(prompt, interpretacion, resultado);

        return new ReporteIA(prompt, interpretacion, resultado, narrativa);
    }

    /** Construye el prompt de sistema embebiendo el catálogo permitido en formato compacto. */
    private String construirSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Eres un asistente que traduce solicitudes en lenguaje natural en la ")
          .append("especificación de un reporte para un ERP de fisioterapia. ")
          .append("Respondes EXCLUSIVAMENTE con un objeto JSON válido con esta forma:\n")
          .append("{\"fuente\": <FUENTE>, \"metrica\": <CONTEO|SUMA|PROMEDIO>, ")
          .append("\"campoMetrica\": <campo numérico o null>, \"agruparPor\": <dimensión>, ")
          .append("\"anio\": <número o null>, \"filtroCampo\": <dimensión o null>, ")
          .append("\"filtroValor\": <texto o null>}\n\n")
          .append("Reglas:\n")
          .append("- Usa únicamente las fuentes, dimensiones y campos numéricos del catálogo.\n")
          .append("- Si la métrica es CONTEO, campoMetrica debe ser null.\n")
          .append("- Para SUMA o PROMEDIO, campoMetrica es obligatorio.\n")
          .append("- Si el usuario menciona un año, ponlo en \"anio\"; si no, null.\n")
          .append("- Para filtrar por una categoría/estado concreto usa filtroCampo + filtroValor ")
          .append("(el valor debe coincidir con los enumerados); si no aplica, ambos null.\n\n")
          .append("CATÁLOGO DISPONIBLE:\n");

        CatalogoReportes catalogo = motor.catalogo();
        for (FuenteMeta f : catalogo.getFuentes()) {
            sb.append("• Fuente ").append(f.getFuente()).append(" (").append(f.getEtiqueta()).append(")")
              .append(f.isSoportaAnio() ? " [soporta filtro por año]" : "")
              .append("\n    dimensiones (agruparPor / filtroCampo): ")
              .append(listar(f.getDimensiones()))
              .append("\n    numéricos (campoMetrica): ")
              .append(f.getNumericos().isEmpty() ? "ninguno" : listar(f.getNumericos()))
              .append("\n");
        }
        return sb.toString();
    }

    private String listar(List<CampoMeta> campos) {
        StringJoiner j = new StringJoiner(", ");
        for (CampoMeta c : campos) j.add(c.getCampo());
        return j.toString();
    }

    /** Resumen narrativo determinista a partir del resultado ya calculado. */
    private String construirNarrativa(String prompt, ReporteDinamicoInput in, ReporteDinamico r) {
        if (r.getFilas().isEmpty())
            return "No se encontraron datos para «" + prompt + "» con los filtros aplicados.";

        FilaReporte top = r.getFilas().get(0);
        StringBuilder sb = new StringBuilder();
        sb.append(r.getTitulo()).append(". ");
        sb.append("Se analizaron ").append(r.getFilas().size())
          .append(" grupos por ").append(r.getEtiquetaDimension().toLowerCase()).append(". ");

        if (in.getMetrica() == MetricaReporte.CONTEO) {
            sb.append("El grupo con más registros es «").append(top.getEtiqueta())
              .append("» con ").append((long) top.getValor()).append(". ");
            sb.append("Total de registros: ").append((long) r.getTotal()).append(".");
        } else {
            sb.append("El valor más alto corresponde a «").append(top.getEtiqueta())
              .append("» con ").append(formato(top.getValor())).append(". ");
            sb.append(in.getMetrica() == MetricaReporte.SUMA ? "Total acumulado: " : "Promedio general: ")
              .append(formato(r.getTotal())).append(".");
        }
        return sb.toString();
    }

    private String formato(double v) {
        return String.format("%,.2f", v);
    }
}
