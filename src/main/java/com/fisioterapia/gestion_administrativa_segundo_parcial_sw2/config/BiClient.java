package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Cliente HTTP interno hacia el microservicio BI/Automatización (Django :8000).
 * Emite eventos de telemetría (ej. factura_pagada) para que el motor BI calcule
 * KPIs sobre datos reales. Comunicación directa entre microservicios (no pasa por
 * el gateway). Es fail-soft: si BI no está disponible no interrumpe el flujo de pago.
 */
@Slf4j
@Component
public class BiClient {

    @Value("${bi.eventos.url:http://localhost:8000/api/bi-automatizacion/eventos/}")
    private String biEventosUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Registra un evento de factura pagada en el motor BI para alimentar el KPI de ingresos.
     *
     * @param pacienteId       Paciente facturado.
     * @param empleadoId       Empleado que registró el cobro (nullable).
     * @param monto            Monto total de la factura.
     * @param categoriaSemaforo Categoría semáforo del tratamiento (nullable).
     */
    public void registrarFacturaPagada(Long pacienteId, Long empleadoId, BigDecimal monto, String categoriaSemaforo) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("tipo_evento", "factura_pagada");
        evento.put("fecha", LocalDate.now().toString());
        evento.put("paciente_id", pacienteId != null ? pacienteId.toString() : null);
        evento.put("empleado_id", empleadoId != null ? empleadoId.toString() : null);
        evento.put("valor_numerico", monto != null ? monto.doubleValue() : null);
        if (categoriaSemaforo != null) evento.put("categoria_semaforo", categoriaSemaforo);

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, Object>> request = new org.springframework.http.HttpEntity<>(evento, headers);
            
            restTemplate.postForObject(biEventosUrl, request, Map.class);
            log.info("Evento factura_pagada enviado a BI (paciente {}, monto {})", pacienteId, monto);
        } catch (Exception e) {
            log.warn("No se pudo enviar evento factura_pagada a BI: {}. El pago continúa.", e.getMessage());
        }
    }
}
