package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.movimientos_insumos;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_insumos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientosInsumos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long insumoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private Integer cantidad;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime fecha;

    /** ID del personal en clinica.usuarios. */
    private Long registradoPor;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
