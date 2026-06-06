package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.movimientos_insumos;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.movimientos_insumos.dto.CrearMovimientoInsumoInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MovimientosInsumosResolver {

    private final MovimientosInsumosService movimientosInsumosService;

    @QueryMapping
    public MovimientosInsumosPaginados listarMovimientosInsumos(@Argument PaginaInput pagina) {
        return movimientosInsumosService.listarMovimientosInsumos(pagina);
    }

    @QueryMapping
    public MovimientosInsumos verMovimientoInsumo(@Argument String id) {
        return movimientosInsumosService.verMovimientoInsumo(id);
    }

    @QueryMapping
    public List<MovimientosInsumos> listarMovimientosPorInsumo(@Argument String insumoId) {
        return movimientosInsumosService.listarMovimientosPorInsumo(insumoId);
    }

    @MutationMapping
    public MovimientosInsumos crearMovimientosInsumos(@Argument CrearMovimientoInsumoInput input) {
        return movimientosInsumosService.crearMovimientosInsumos(input);
    }

    @MutationMapping
    public Boolean eliminarMovimientoInsumo(@Argument String id) {
        return movimientosInsumosService.eliminarMovimientoInsumo(id);
    }
}
