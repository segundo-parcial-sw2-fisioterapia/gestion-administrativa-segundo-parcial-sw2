package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.tarifas;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.tarifas.dto.ActualizarTarifaInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TarifasService {

    private final TarifasRepository tarifasRepository;

    /**
     * Lista todas las tarifas configuradas en el sistema.
     * Si la base de datos está vacía, inicializa las 9 combinaciones automáticamente.
     *
     * @return Lista de tarifas.
     */
    @Transactional
    public List<Tarifas> listarTarifas() {
        List<Tarifas> tarifas = tarifasRepository.findAll();
        if (tarifas.isEmpty()) {
            for (CategoriaSemaforoTarifa semaforo : CategoriaSemaforoTarifa.values()) {
                for (NivelIntensidad nivel : NivelIntensidad.values()) {
                    Tarifas nuevaTarifa = Tarifas.builder()
                            .categoriaSemaforo(semaforo)
                            .nivel(nivel)
                            .precioMensual(java.math.BigDecimal.ZERO)
                            .build();
                    tarifasRepository.save(nuevaTarifa);
                }
            }
            tarifas = tarifasRepository.findAll();
        }
        return tarifas;
    }

    /**
     * Obtiene una tarifa por su ID.
     *
     * @param id ID de la tarifa.
     * @return La tarifa encontrada, o null si no existe.
     */
    public Tarifas verTarifa(String id) {
        return tarifasRepository.findById(Long.parseLong(id)).orElse(null);
    }

    /**
     * Actualiza el precio mensual y el registro de quién actualizó la tarifa.
     *
     * @param id    ID de la tarifa a actualizar.
     * @param input Datos de actualización (precioMensual y actualizadoPor).
     * @return La tarifa actualizada.
     */
    @Transactional
    public Tarifas actualizarTarifa(String id, ActualizarTarifaInput input) {
        Tarifas tarifa = tarifasRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada: " + id));
        if (input.getPrecioMensual() != null) tarifa.setPrecioMensual(input.getPrecioMensual());
        if (input.getActualizadoPor() != null) tarifa.setActualizadoPor(input.getActualizadoPor());
        return tarifasRepository.save(tarifa);
    }
}
