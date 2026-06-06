package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.turnos;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "turnos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Turnos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long empleadoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private DiaSemana diaSemana;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
