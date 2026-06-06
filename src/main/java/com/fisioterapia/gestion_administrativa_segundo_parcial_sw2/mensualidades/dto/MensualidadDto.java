package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.Mensualidades;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta segura de mensualidad para serialización GraphQL.
 * Todos los campos de fecha/enum se convierten a String para evitar
 * errores de coerción con tipos Java (LocalDate, LocalDateTime, Enum)
 * que Spring GraphQL no mapea automáticamente a String.
 */
@Data
@NoArgsConstructor
public class MensualidadDto {

    private Long id;
    private Long planTratamientoId;
    private Long pacienteId;
    /** LocalDate convertida a String ISO-8601 (ej: "2026-06-01"). */
    private String periodo;
    private BigDecimal monto;
    /** EstadoMensualidad convertido a String (ej: "pendiente", "pagada", "vencida"). */
    private String estado;
    /** LocalDateTime convertida a String ISO-8601. */
    private String fechaCreacion;

    public static MensualidadDto desde(Mensualidades m) {
        if (m == null) return null;
        MensualidadDto dto = new MensualidadDto();
        dto.setId(m.getId());
        dto.setPlanTratamientoId(m.getPlanTratamientoId());
        dto.setPacienteId(m.getPacienteId());
        dto.setPeriodo(m.getPeriodo() != null ? m.getPeriodo().toString() : null);
        dto.setMonto(m.getMonto());
        dto.setEstado(m.getEstado() != null ? m.getEstado().name() : null);
        dto.setFechaCreacion(m.getFechaCreacion() != null ? m.getFechaCreacion().toString() : null);
        return dto;
    }
}
