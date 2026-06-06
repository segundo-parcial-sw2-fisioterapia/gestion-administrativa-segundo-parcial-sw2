package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.tarifas;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "tarifas",
    uniqueConstraints = @UniqueConstraint(columnNames = {"categoria_semaforo", "nivel"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tarifas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Categoría semáforo que determina la urgencia/complejidad del tratamiento. */
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_semaforo", nullable = false, length = 20)
    private CategoriaSemaforoTarifa categoriaSemaforo;

    /** Nivel de intensidad del plan de tratamiento. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NivelIntensidad nivel;

    /** Precio mensual de la tarifa para esta categoría y nivel. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual;

    /** Fecha y hora de la última actualización (gestionada automáticamente). */
    @UpdateTimestamp
    private LocalDateTime actualizadoEn;

    /** ID del empleado que realizó la última actualización (nullable). */
    private Long actualizadoPor;
}
