package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.tarifas;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.tarifas.dto.ActualizarTarifaInput;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class TarifasResolver {

    private final TarifasService tarifasService;

    @QueryMapping
    public List<Tarifas> listarTarifas() {
        return tarifasService.listarTarifas();
    }

    @QueryMapping
    public Tarifas verTarifa(@Argument String id) {
        return tarifasService.verTarifa(id);
    }

    @MutationMapping
    public Tarifas actualizarTarifa(@Argument String id, @Argument ActualizarTarifaInput input) {
        return tarifasService.actualizarTarifa(id, input);
    }
}
