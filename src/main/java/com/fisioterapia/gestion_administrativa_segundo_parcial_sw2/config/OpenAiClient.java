package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * Cliente ligero hacia la API de OpenAI (Chat Completions) construido sobre RestClient.
 * No agrega dependencias: usa el RestClient de spring-web y Jackson ya presentes.
 *
 * Se usa en modo JSON ({@code response_format: json_object}) para que el modelo traduzca
 * un prompt en lenguaje natural a la especificación estructurada de un reporte dinámico.
 */
@Slf4j
@Component
public class OpenAiClient {

    private final RestClient restClient;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;

    public OpenAiClient(
            ObjectMapper mapper,
            @Value("${openai.api.key:}") String apiKey,
            @Value("${openai.api.url:https://api.openai.com/v1}") String baseUrl,
            @Value("${openai.model:gpt-4o-mini}") String model) {
        this.mapper = mapper;
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** Indica si hay una API key configurada (permite degradar con un mensaje claro). */
    public boolean estaConfigurado() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Envía un mensaje de sistema y uno de usuario y devuelve el contenido de la
     * respuesta del asistente como texto JSON (forzado por response_format).
     *
     * @param system Instrucciones y catálogo permitido.
     * @param user   Prompt del usuario en lenguaje natural.
     * @return Cadena JSON con la especificación del reporte.
     */
    public String completarJson(String system, String user) {
        if (!estaConfigurado())
            throw new IllegalStateException("OPENAI_API_KEY no configurada en el entorno.");

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)
                )
        );

        try {
            String respuesta = restClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = mapper.readTree(respuesta);
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Error consultando OpenAI: {}", e.getMessage());
            throw new RuntimeException("No se pudo generar el reporte con IA: " + e.getMessage(), e);
        }
    }
}
