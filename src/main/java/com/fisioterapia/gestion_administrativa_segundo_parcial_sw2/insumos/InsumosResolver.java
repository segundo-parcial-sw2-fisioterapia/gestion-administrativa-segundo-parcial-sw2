package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos.dto.CrearInsumoInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos.dto.EditarInsumoInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class InsumosResolver {

    private final InsumosService insumosService;

    @QueryMapping
    public InsumosPaginados listarInsumos(@Argument PaginaInput pagina) {
        return insumosService.listarInsumos(pagina);
    }

    @QueryMapping
    public Insumos verInsumo(@Argument String id) {
        return insumosService.verInsumo(id);
    }

    @QueryMapping
    public List<Insumos> listarInsumosConStockBajo() {
        return insumosService.listarInsumosConStockBajo();
    }

    @MutationMapping
    public Insumos crearInsumos(@Argument CrearInsumoInput input) {
        return insumosService.crearInsumos(input);
    }

    @MutationMapping
    public Insumos editarInsumo(@Argument String id, @Argument EditarInsumoInput input) {
        return insumosService.editarInsumo(id, input);
    }

    @MutationMapping
    public Boolean eliminarInsumo(@Argument String id) {
        return insumosService.eliminarInsumo(id);
    }
}
