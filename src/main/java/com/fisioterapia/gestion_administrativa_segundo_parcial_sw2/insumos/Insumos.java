package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "insumos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Insumos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaInsumo categoria;

    @Column(nullable = false)
    @Builder.Default
    private Integer stockActual = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer stockMinimo = 0;

    @Column(nullable = false, length = 30)
    private String unidadMedida;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
