package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.tarifas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class TarifasInitializer implements CommandLineRunner {

    private final TarifasRepository repository;

    public TarifasInitializer(TarifasRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (CategoriaSemaforoTarifa semaforo : CategoriaSemaforoTarifa.values()) {
            for (NivelIntensidad nivel : NivelIntensidad.values()) {
                if (repository.findByCategoriaSemaforoAndNivel(semaforo, nivel).isEmpty()) {
                    Tarifas tarifa = Tarifas.builder()
                            .categoriaSemaforo(semaforo)
                            .nivel(nivel)
                            .precioMensual(BigDecimal.ZERO)
                            .build();
                    repository.save(tarifa);
                }
            }
        }
    }
}
