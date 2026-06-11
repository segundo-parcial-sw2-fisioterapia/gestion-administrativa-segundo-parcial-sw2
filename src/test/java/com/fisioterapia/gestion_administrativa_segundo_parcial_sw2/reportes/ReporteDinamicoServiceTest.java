package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.EmpleadosRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.Facturas;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.FacturasRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos.InsumosRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.MensualidadesRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.movimientos_insumos.MovimientosInsumosRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.MetodoPago;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.PagosRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico.*;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.tarifas.TarifasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Prueba unitaria del motor de reportes dinámicos. Verifica la agregación sobre datos
 * reales (mockeados), el filtro por año y la validación del whitelist de campos.
 */
@ExtendWith(MockitoExtension.class)
class ReporteDinamicoServiceTest {

    @Mock FacturasRepository facturasRepository;
    @Mock MensualidadesRepository mensualidadesRepository;
    @Mock PagosRepository pagosRepository;
    @Mock EmpleadosRepository empleadosRepository;
    @Mock InsumosRepository insumosRepository;
    @Mock MovimientosInsumosRepository movimientosInsumosRepository;
    @Mock TarifasRepository tarifasRepository;

    ReporteDinamicoService service;

    @BeforeEach
    void setUp() {
        service = new ReporteDinamicoService(
                facturasRepository, mensualidadesRepository, pagosRepository,
                empleadosRepository, insumosRepository, movimientosInsumosRepository,
                tarifasRepository);
        service.construirRegistro(); // normalmente lo dispara @PostConstruct

        lenient().when(facturasRepository.findAll()).thenReturn(List.of(
                factura(MetodoPago.efectivo, "100.00", LocalDateTime.of(2026, 1, 10, 9, 0)),
                factura(MetodoPago.efectivo, "50.00", LocalDateTime.of(2026, 3, 5, 9, 0)),
                factura(MetodoPago.tarjeta, "200.00", LocalDateTime.of(2026, 4, 1, 9, 0)),
                factura(MetodoPago.efectivo, "999.00", LocalDateTime.of(2025, 6, 1, 9, 0)) // otro año
        ));
    }

    @Test
    void sumaIngresosPorMetodoDePagoFiltradoPorAnio() {
        ReporteDinamicoInput input = new ReporteDinamicoInput(
                FuenteReporte.FACTURAS, MetricaReporte.SUMA, "montoTotal",
                "metodoPago", 2026, null, null);

        ReporteDinamico r = service.ejecutar(input);

        // 2025 queda fuera; tarjeta(200) > efectivo(150); total 350.
        assertThat(r.getFilas()).hasSize(2);
        assertThat(r.getFilas().get(0).getEtiqueta()).isEqualTo("tarjeta");
        assertThat(r.getFilas().get(0).getValor()).isEqualTo(200.0);
        assertThat(r.getFilas().get(1).getValor()).isEqualTo(150.0);
        assertThat(r.getTotal()).isEqualTo(350.0);
    }

    @Test
    void conteoDeFacturasPorMetodoDePago() {
        ReporteDinamicoInput input = new ReporteDinamicoInput(
                FuenteReporte.FACTURAS, MetricaReporte.CONTEO, null,
                "metodoPago", 2026, null, null);

        ReporteDinamico r = service.ejecutar(input);

        assertThat(r.getTotal()).isEqualTo(3.0); // 3 facturas en 2026
        assertThat(r.getFilas().get(0).getEtiqueta()).isEqualTo("efectivo"); // 2 registros
        assertThat(r.getFilas().get(0).getValor()).isEqualTo(2.0);
    }

    @Test
    void rechazaDimensionFueraDelWhitelist() {
        ReporteDinamicoInput input = new ReporteDinamicoInput(
                FuenteReporte.FACTURAS, MetricaReporte.CONTEO, null,
                "columnaInexistente", null, null, null);

        assertThatThrownBy(() -> service.ejecutar(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no permitida");
    }

    @Test
    void catalogoExponeFuentesYDimensiones() {
        CatalogoReportes catalogo = service.catalogo();

        assertThat(catalogo.getFuentes()).isNotEmpty();
        FuenteMeta facturas = catalogo.getFuentes().stream()
                .filter(f -> f.getFuente() == FuenteReporte.FACTURAS)
                .findFirst().orElseThrow();
        assertThat(facturas.isSoportaAnio()).isTrue();
        assertThat(facturas.getDimensiones()).extracting(CampoMeta::getCampo).contains("metodoPago", "mes");
        assertThat(facturas.getNumericos()).extracting(CampoMeta::getCampo).contains("montoTotal");
    }

    private Facturas factura(MetodoPago metodo, String monto, LocalDateTime fecha) {
        return Facturas.builder()
                .pacienteId(1L)
                .numeroFactura("F-" + monto)
                .fechaEmision(fecha)
                .montoTotal(new BigDecimal(monto))
                .metodoPago(metodo)
                .build();
    }
}
