package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensualidades")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mensualidades {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Referencia al plan de tratamiento en clinica (sin FK directa — autonomía de microservicio). */
    @Column(nullable = false)
    private Long planTratamientoId;

    /** Desnormalización del paciente para permitir queries directas sin join cross-service. */
    @Column(nullable = false)
    private Long pacienteId;

    /** Primer día del mes que cubre esta mensualidad. */
    @Column(nullable = false)
    private LocalDate periodo;

    /** Monto a cobrar por este período mensual. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    /** Estado de pago de la mensualidad. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoMensualidad estado = EstadoMensualidad.pendiente;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;
}
