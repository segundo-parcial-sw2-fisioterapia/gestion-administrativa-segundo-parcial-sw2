package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.documentos;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.BlockchainClient;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.documentos.dto.CrearDocumentoInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.documentos.dto.EditarDocumentoInput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentosService {

    private final DocumentosRepository documentosRepository;
    private final BlockchainClient blockchainClient;

    /** Lista documentos paginados, ordenados por fecha de subida descendente. */
    public DocumentosPaginados listarDocumentos(PaginaInput pagina) {
        int p = pagina != null ? Math.max(0, pagina.getPagina()) : 0;
        int t = pagina != null ? Math.min(100, Math.max(1, pagina.getTamano())) : 20;
        Page<Documentos> page = documentosRepository.findAll(
                PageRequest.of(p, t, Sort.by("fechaSubida").descending()));
        return new DocumentosPaginados(page.getContent(), PaginaInfo.de(page));
    }

    /** @return El documento con el ID dado, o null si no existe. */
    public Documentos verDocumento(String id) {
        return documentosRepository.findById(Long.parseLong(id)).orElse(null);
    }

    /** Lista documentos de una entidad (paciente, empleado o factura). */
    public List<Documentos> listarDocumentosPorEntidad(String referenciaEntidadId) {
        return documentosRepository.findByReferenciaEntidad(Long.parseLong(referenciaEntidadId));
    }

    /**
     * Registra un nuevo documento administrativo.
     *
     * @param input Datos del documento.
     * @return El documento creado.
     */
    @Transactional
    public Documentos crearDocumentos(CrearDocumentoInput input) {
        return documentosRepository.save(Documentos.builder()
                .nombre(input.getNombre())
                .tipo(input.getTipo())
                .urlS3(input.getUrlS3())
                .subidoPor(Long.parseLong(input.getSubidoPor()))
                .referenciaEntidad(input.getReferenciaEntidad() != null
                        ? Long.parseLong(input.getReferenciaEntidad()) : null)
                .tipoEntidad(input.getTipoEntidad())
                .build());
    }

    /**
     * Actualiza los metadatos de un documento existente.
     *
     * @param id    ID del documento.
     * @param input Campos a modificar.
     * @return El documento actualizado.
     */
    @Transactional
    public Documentos editarDocumento(String id, EditarDocumentoInput input) {
        Documentos doc = documentosRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + id));
        if (input.getNombre() != null) doc.setNombre(input.getNombre());
        if (input.getTipo() != null) doc.setTipo(input.getTipo());
        if (input.getUrlS3() != null) doc.setUrlS3(input.getUrlS3());
        if (input.getReferenciaEntidad() != null)
            doc.setReferenciaEntidad(Long.parseLong(input.getReferenciaEntidad()));
        if (input.getTipoEntidad() != null) doc.setTipoEntidad(input.getTipoEntidad());
        return documentosRepository.save(doc);
    }

    /**
     * Elimina un documento por su ID.
     *
     * @param id ID del documento.
     * @return true si fue eliminado.
     */
    @Transactional
    public Boolean eliminarDocumento(String id) {
        documentosRepository.deleteById(Long.parseLong(id));
        return true;
    }

    /**
     * Firma digitalmente un documento registrando su hash en blockchain-firmas (MS-6).
     * Si blockchain no está disponible, la operación continúa sin bloquear.
     *
     * @param id ID del documento a firmar.
     * @return El documento con campos de blockchain actualizados.
     */
    @Transactional
    public Documentos firmarDocumentoConBlockchain(String id) {
        Documentos doc = documentosRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Documento no encontrado: " + id));
        String contenido = "DOCUMENTO:" + doc.getNombre() + "|TIPO:" + doc.getTipo().name();
        blockchainClient.firmar(contenido, "ADMINISTRATIVO").ifPresent(respuesta -> {
            doc.setFirmado(true);
            doc.setHashBlockchain(respuesta.getHash());
            doc.setTxBlockchain(respuesta.getTxHash());
        });
        return documentosRepository.save(doc);
    }
}
