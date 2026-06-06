package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.dto.CrearPagoInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.dto.EditarPagoInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PagosResolver {

    private final PagosService pagosService;

    @QueryMapping
    public PagosPaginados listarPagos(@Argument PaginaInput pagina) {
        return pagosService.listarPagos(pagina);
    }

    @QueryMapping
    public Pagos verPago(@Argument String id) {
        return pagosService.verPago(id);
    }

    @QueryMapping
    public List<Pagos> listarPagosPorFactura(@Argument String facturaId) {
        return pagosService.listarPagosPorFactura(facturaId);
    }

    @MutationMapping
    public Pagos crearPagos(@Argument CrearPagoInput input) {
        return pagosService.crearPagos(input);
    }

    @MutationMapping
    public Pagos editarPago(@Argument String id, @Argument EditarPagoInput input) {
        return pagosService.editarPago(id, input);
    }

    @MutationMapping
    public Boolean eliminarPago(@Argument String id) {
        return pagosService.eliminarPago(id);
    }
}
