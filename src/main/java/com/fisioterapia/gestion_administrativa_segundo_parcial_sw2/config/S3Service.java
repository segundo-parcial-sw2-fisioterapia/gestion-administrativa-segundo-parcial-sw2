package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Servicio de acceso a Amazon S3 para subir documentos generados por el ERP.
 */
@Slf4j
@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Sube un archivo al bucket S3 y retorna su URL pública.
     *
     * @param key         Ruta dentro del bucket (ej: "facturas/FAC-001.pdf").
     * @param contenido   Bytes del archivo a subir.
     * @param contentType MIME type (ej: "application/pdf").
     * @return URL pública del objeto en S3.
     */
    public String subirArchivo(String key, byte[] contenido, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType)
                        .contentLength((long) contenido.length)
                        .build(),
                RequestBody.fromBytes(contenido));

        String url = "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
        log.info("Archivo subido a S3: {}", url);
        return url;
    }
}
