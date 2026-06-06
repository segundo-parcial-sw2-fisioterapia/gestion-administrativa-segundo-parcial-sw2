package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.PersonaInfo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "empleados")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Empleados {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Referencia a personas en clinica (sin FK directa — autonomía de microservicio). */
    @Column(nullable = false)
    private Long personaId;

    @Column(nullable = false, length = 100)
    private String cargo;

    @Column(length = 100)
    private String especialidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salarioBase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoContrato tipoContrato;

    @Column(nullable = false)
    private LocalDate fechaIngreso;

    private LocalDate fechaBaja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoLaboral estadoLaboral = EstadoLaboral.activo;

    /**
     * Datos de la persona física obtenidos del microservicio clinica.
     * No se persiste en BD — se enriquece en el servicio tras la consulta.
     */
    @Transient
    private PersonaInfo persona;
}
