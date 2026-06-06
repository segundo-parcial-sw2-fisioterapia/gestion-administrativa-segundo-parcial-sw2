package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cliente HTTP interno hacia el microservicio clinica (NestJS :3000).
 * Obtiene datos de Persona para enriquecer las respuestas de Empleados.
 * Comunicación directa entre microservicios (no pasa por el gateway).
 * Si clinica no está disponible, devuelve Optional.empty() sin interrumpir el flujo.
 */
@Slf4j
@Component
public class ClinicaClient {

    @Value("${clinica.graphql.url:http://localhost:3000/graphql}")
    private String clinicaGraphqlUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Obtiene los datos básicos de una persona desde el microservicio clinica.
     * La query GraphQL usa Int! para IDs según el schema de clinica (NestJS).
     *
     * @param personaId ID de la persona en clinica.
     * @return Datos de la persona o empty si no se encuentra o hay error.
     */
    @SuppressWarnings("unchecked")
    public Optional<PersonaInfo> obtenerPersona(Long personaId) {
        try {
            Map<String, Object> body = Map.of(
                    "query", "query($id: Int!) { verPersona(id: $id) { nombre apellido ci telefono } }",
                    "variables", Map.of("id", personaId.intValue())
            );

            Map<String, Object> response = restTemplate.postForObject(
                    clinicaGraphqlUrl, body, Map.class
            );

            if (response == null) return Optional.empty();

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data == null) return Optional.empty();

            Map<String, Object> persona = (Map<String, Object>) data.get("verPersona");
            if (persona == null) return Optional.empty();

            PersonaInfo info = new PersonaInfo();
            info.setNombre((String) persona.get("nombre"));
            info.setApellido((String) persona.get("apellido"));
            info.setCi((String) persona.get("ci"));
            info.setTelefono((String) persona.get("telefono"));
            return Optional.of(info);

        } catch (Exception e) {
            log.debug("clinica no disponible para persona {}: {}", personaId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Obtiene datos de nombre/CI de múltiples pacientes desde clinica (batch).
     * Usa la query interna pacientesPorIds que retorna el paciente con su persona anidada.
     * Keyed por paciente ID (no personaId).
     *
     * @param pacienteIds Lista de IDs del entity Pacientes en clinica.
     * @return Mapa de pacienteId → PersonaInfo.
     */
    @SuppressWarnings("unchecked")
    public Map<Long, PersonaInfo> obtenerPacientesBatch(List<Long> pacienteIds) {
        if (pacienteIds == null || pacienteIds.isEmpty()) return Map.of();
        try {
            List<Integer> ids = pacienteIds.stream()
                    .filter(java.util.Objects::nonNull)
                    .map(Long::intValue)
                    .distinct()
                    .toList();
            if (ids.isEmpty()) return Map.of();

            Map<String, Object> body = Map.of(
                    "query", "query($ids: [Int!]!) { pacientesPorIds(ids: $ids) { id persona { nombre apellido ci telefono } } }",
                    "variables", Map.of("ids", ids)
            );

            Map<String, Object> response = restTemplate.postForObject(clinicaGraphqlUrl, body, Map.class);
            if (response == null) return Map.of();

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data == null) return Map.of();

            List<Map<String, Object>> lista = (List<Map<String, Object>>) data.get("pacientesPorIds");
            if (lista == null) return Map.of();

            Map<Long, PersonaInfo> resultado = new java.util.HashMap<>();
            for (Map<String, Object> pac : lista) {
                if (pac.get("id") == null) continue;
                Long id = ((Number) pac.get("id")).longValue();
                Map<String, Object> persona = (Map<String, Object>) pac.get("persona");
                if (persona == null) continue;
                PersonaInfo info = new PersonaInfo();
                info.setNombre((String) persona.get("nombre"));
                info.setApellido((String) persona.get("apellido"));
                info.setCi((String) persona.get("ci"));
                info.setTelefono((String) persona.get("telefono"));
                resultado.put(id, info);
            }
            return resultado;
        } catch (Exception e) {
            log.warn("Error al obtener pacientes por lote desde clinica: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * Enriquece una lista de IDs de persona con sus datos desde clinica de forma altamente optimizada.
     * Realiza una ÚNICA llamada HTTP consolidada (Batch) por lote.
     *
     * @param personaIds Lista de IDs de persona.
     * @return Mapa de personaId → PersonaInfo.
     */
    @SuppressWarnings("unchecked")
    public Map<Long, PersonaInfo> obtenerPersonasBatch(List<Long> personaIds) {
        if (personaIds == null || personaIds.isEmpty()) return Map.of();

        try {
            // Mapear los IDs de Long a Integer para coincidir con el tipo [Int!]! de GraphQL de clinica
            List<Integer> idsComoEnteros = personaIds.stream()
                    .filter(java.util.Objects::nonNull)
                    .map(Long::intValue)
                    .distinct()
                    .toList();

            if (idsComoEnteros.isEmpty()) return Map.of();

            Map<String, Object> body = Map.of(
                    "query", "query($ids: [Int!]!) { personasPorIds(ids: $ids) { id nombre apellido ci telefono } }",
                    "variables", Map.of("ids", idsComoEnteros)
            );

            Map<String, Object> response = restTemplate.postForObject(
                    clinicaGraphqlUrl, body, Map.class
            );

            if (response == null) return Map.of();

            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data == null) return Map.of();

            List<Map<String, Object>> personasList = (List<Map<String, Object>>) data.get("personasPorIds");
            if (personasList == null) return Map.of();

            Map<Long, PersonaInfo> resultado = new java.util.HashMap<>();
            for (Map<String, Object> persona : personasList) {
                if (persona.get("id") == null) continue;

                Long id = ((Number) persona.get("id")).longValue();
                PersonaInfo info = new PersonaInfo();
                info.setNombre((String) persona.get("nombre"));
                info.setApellido((String) persona.get("apellido"));
                info.setCi((String) persona.get("ci"));
                info.setTelefono((String) persona.get("telefono"));

                resultado.put(id, info);
            }

            return resultado;

        } catch (Exception e) {
            log.warn("Error al obtener personas por lote desde clinica: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * Consulta las sesiones correspondientes a una mensualidad y las marca
     * como HABILITADA en el microservicio Clínica.
     *
     * @param mensualidadId ID de la mensualidad pagada.
     */
    @SuppressWarnings("unchecked")
    public void habilitarSesionesPorMensualidad(Long mensualidadId) {
        if (mensualidadId == null) return;
        try {
            log.info("Habilitando sesiones clínicas para mensualidad ID: {}", mensualidadId);
            
            // 1. Consultar sesiones de la mensualidad
            Map<String, Object> bodyListar = Map.of(
                    "query", "query($mensualidadId: Int!) { listarSesionesPorMensualidad(mensualidadId: $mensualidadId) { id } }",
                    "variables", Map.of("mensualidadId", mensualidadId.intValue())
            );

            Map<String, Object> responseListar = restTemplate.postForObject(
                    clinicaGraphqlUrl, bodyListar, Map.class
            );

            if (responseListar == null) return;

            Map<String, Object> dataListar = (Map<String, Object>) responseListar.get("data");
            if (dataListar == null) return;

            List<Map<String, Object>> sesiones = (List<Map<String, Object>>) dataListar.get("listarSesionesPorMensualidad");
            if (sesiones == null || sesiones.isEmpty()) {
                log.info("No se encontraron sesiones asociadas a la mensualidad ID: {}", mensualidadId);
                return;
            }

            // 2. Actualizar cada sesión a HABILITADA
            for (Map<String, Object> sesion : sesiones) {
                if (sesion.get("id") == null) continue;
                int sesionId = ((Number) sesion.get("id")).intValue();
                
                Map<String, Object> bodyEditar = Map.of(
                        "query", "mutation($datos: UpdateSesioneInput!) { editarSesion(datos: $datos) { id estado_sesion } }",
                        "variables", Map.of("datos", Map.of(
                                "id", sesionId,
                                "estado_sesion", "HABILITADA"
                        ))
                );

                restTemplate.postForObject(clinicaGraphqlUrl, bodyEditar, Map.class);
                log.info("Sesión ID {} habilitada exitosamente.", sesionId);
            }
        } catch (Exception e) {
            log.warn("Error al habilitar sesiones para mensualidad {}: {}", mensualidadId, e.getMessage());
        }
    }
}
