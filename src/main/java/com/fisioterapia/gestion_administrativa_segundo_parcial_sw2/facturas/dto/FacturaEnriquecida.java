package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.PersonaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.Empleados;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.Facturas;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta enriquecida de factura.
 * Todos los campos deben coincidir exactamente con el tipo GraphQL FacturaEnriquecida del schema.
 * Las fechas se retornan como String para ser serializadas sin conflictos por Spring GraphQL.
 */
@Data
@NoArgsConstructor
public class FacturaEnriquecida {

    private Long id;
    private Long pacienteId;
    private String numeroFactura;
    /** Serializado como String (ISO-8601) para coincidir con el schema: fechaEmision: String! */
    private String fechaEmision;
    private BigDecimal montoTotal;
    /** Serializado como String para coincidir con el schema: estado: String! */
    private String estado;
    private Long mensualidadId;
    private String concepto;
    private Long empleadoId;
    /** Serializado como String para coincidir con el schema: metodoPago: String */
    private String metodoPago;
    private String urlDocumento;
    private String hashBlockchain;
    private String txBlockchain;
    private String fechaRegistroBlockchain;
    /** Serializado como String (ISO-8601) para coincidir con el schema: fechaCreacion: String */
    private String fechaCreacion;
    private PersonaInfo paciente;
    /** DTO seguro del empleado — no la entidad JPA directamente. */
    private EmpleadoDto empleado;

    public FacturaEnriquecida(Facturas f) {
        this.id = f.getId();
        this.pacienteId = f.getPacienteId();
        this.numeroFactura = f.getNumeroFactura();
        this.fechaEmision = f.getFechaEmision() != null ? f.getFechaEmision().toString() : null;
        this.montoTotal = f.getMontoTotal();
        this.estado = f.getEstado() != null ? f.getEstado().name() : null;
        this.mensualidadId = f.getMensualidadId();
        this.concepto = f.getConcepto();
        this.empleadoId = f.getEmpleadoId();
        this.metodoPago = f.getMetodoPago() != null ? f.getMetodoPago().name() : null;
        this.urlDocumento = f.getUrlDocumento();
        this.hashBlockchain = f.getHashBlockchain();
        this.txBlockchain = f.getTxBlockchain();
        this.fechaRegistroBlockchain = f.getFechaRegistroBlockchain() != null ? f.getFechaRegistroBlockchain().toString() : null;
        this.fechaCreacion = f.getFechaCreacion() != null ? f.getFechaCreacion().toString() : null;
    }

    /** DTO interno seguro para serializar datos del empleado hacia GraphQL. */
    @Data
    @NoArgsConstructor
    public static class EmpleadoDto {
        private Long id;
        private String cargo;
        private String especialidad;
        private PersonaInfo persona;

        public static EmpleadoDto desde(Empleados emp) {
            if (emp == null) return null;
            EmpleadoDto dto = new EmpleadoDto();
            dto.setId(emp.getId());
            dto.setCargo(emp.getCargo());
            dto.setEspecialidad(emp.getEspecialidad());
            dto.setPersona(emp.getPersona());
            return dto;
        }
    }
}
