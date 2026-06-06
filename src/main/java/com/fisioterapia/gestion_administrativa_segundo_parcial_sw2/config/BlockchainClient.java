package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

/**
 * Cliente HTTP interno hacia MS-6 blockchain-firmas.
 * Si el servicio no está disponible, devuelve Optional.empty() sin lanzar excepción
 * para que el flujo principal continúe sin bloquear la operación.
 */
@Slf4j
@Component
public class BlockchainClient {

    @Value("${blockchain.service.url}")
    private String blockchainUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Envía el contenido a blockchain-firmas para obtener hash SHA-256 y txHash Sepolia.
     *
     * @param contenido Texto serializado del documento o factura.
     * @param tipo      "CLINICO" o "ADMINISTRATIVO".
     * @return La respuesta con hash, txHash y timestamp, o empty si falla.
     */
    public Optional<BlockchainRespuesta> firmar(String contenido, String tipo) {
        try {
            Map<String, String> body = Map.of("contenido", contenido, "tipo", tipo);
            BlockchainRespuesta respuesta = restTemplate.postForObject(
                    blockchainUrl + "/firmar",
                    body,
                    BlockchainRespuesta.class
            );
            return Optional.ofNullable(respuesta);
        } catch (Exception e) {
            log.warn("blockchain-firmas no disponible: {}. La operacion continua sin firma.", e.getMessage());
            return Optional.empty();
        }
    }
}
