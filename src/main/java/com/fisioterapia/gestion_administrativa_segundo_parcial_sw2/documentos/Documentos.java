package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.documentos;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Documentos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoDocumento tipo;

    @Column(length = 500)
    private String urlS3;

    /** ID del usuario en clinica que subió el documento. */
    @Column(nullable = false)
    private Long subidoPor;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime fechaSubida;

    @Column(nullable = false)
    @Builder.Default
    private Boolean firmado = false;

    @Column(length = 255)
    private String hashBlockchain;

    @Column(length = 255)
    private String txBlockchain;

    /** ID del paciente, empleado o factura al que pertenece. */
    private Long referenciaEntidad;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private TipoEntidadDocumento tipoEntidad;
}
