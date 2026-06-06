package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.MetodoPago;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Facturas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Referencia al paciente en clinica (sin FK directa — autonomía de microservicio). */
    @Column(nullable = false)
    private Long pacienteId;

    @Column(nullable = false, length = 50)
    private String numeroFactura;

    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoFactura estado = EstadoFactura.emitida;

    /** Referencia a la mensualidad que origina esta factura (nullable). */
    private Long mensualidadId;

    /** Descripción generada automáticamente del concepto facturado. */
    @Column(length = 255)
    private String concepto;

    /** Recepcionista que registró el cobro (nullable). */
    private Long empleadoId;

    /** Método de pago utilizado (nullable). */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MetodoPago metodoPago;

    @Column(length = 500)
    private String urlDocumento;

    @Column(length = 255)
    private String hashBlockchain;

    @Column(length = 255)
    private String txBlockchain;

    private LocalDateTime fechaRegistroBlockchain;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;
}
