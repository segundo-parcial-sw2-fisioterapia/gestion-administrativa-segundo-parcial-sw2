package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.documentos;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.documentos.dto.CrearDocumentoInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.documentos.dto.EditarDocumentoInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DocumentosResolver {

    private final DocumentosService documentosService;

    @QueryMapping
    public DocumentosPaginados listarDocumentos(@Argument PaginaInput pagina) {
        return documentosService.listarDocumentos(pagina);
    }

    @QueryMapping
    public Documentos verDocumento(@Argument String id) {
        return documentosService.verDocumento(id);
    }

    @QueryMapping
    public List<Documentos> listarDocumentosPorEntidad(@Argument String referenciaEntidadId) {
        return documentosService.listarDocumentosPorEntidad(referenciaEntidadId);
    }

    @MutationMapping
    public Documentos crearDocumentos(@Argument CrearDocumentoInput input) {
        return documentosService.crearDocumentos(input);
    }

    @MutationMapping
    public Documentos editarDocumento(@Argument String id, @Argument EditarDocumentoInput input) {
        return documentosService.editarDocumento(id, input);
    }

    @MutationMapping
    public Boolean eliminarDocumento(@Argument String id) {
        return documentosService.eliminarDocumento(id);
    }

    @MutationMapping
    public Documentos firmarDocumentoConBlockchain(@Argument String id) {
        return documentosService.firmarDocumentoConBlockchain(id);
    }
}
