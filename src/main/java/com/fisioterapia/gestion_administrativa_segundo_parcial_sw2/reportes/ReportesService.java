package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.EstadoFactura;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.Facturas;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.FacturasRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.EstadoMensualidad;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.Mensualidades;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.MensualidadesRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.IngresoMensual;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.IngresoPorCategoria;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.ReporteFinanciero;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.ResumenMensualidades;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de reportes financieros del subsistema administrativo.
 * Agrega datos reales de facturas y mensualidades para el módulo BI del frontend.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportesService {

    private static final String[] MESES = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private final FacturasRepository facturasRepository;
    private final MensualidadesRepository mensualidadesRepository;

    /**
     * Genera el reporte financiero consolidado de un año: ingresos facturados por mes,
     * desglose por método de pago, ticket promedio y estado de cobranza de mensualidades.
     * Solo considera facturas emitidas (excluye anuladas).
     *
     * @param anio Año a reportar (ej. 2026). Si es null usa el año actual.
     */
    public ReporteFinanciero reporteFinanciero(Integer anio) {
        int year = anio != null ? anio : Year.now().getValue();

        LocalDateTime desde = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime hasta = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
        List<Facturas> facturas = facturasRepository.findByFechaEmisionBetween(desde, hasta).stream()
                .filter(f -> f.getEstado() == EstadoFactura.emitida)
                .toList();

        // Ingresos por mes (1-12, todos los meses presentes para una serie continua).
        double[] ingresosMes = new double[12];
        int[] facturasMes = new int[12];
        double totalIngresos = 0;
        Map<String, double[]> porMetodo = new LinkedHashMap<>(); // [total, cantidad]

        for (Facturas f : facturas) {
            int idx = f.getFechaEmision().getMonthValue() - 1;
            double monto = montoDouble(f.getMontoTotal());
            ingresosMes[idx] += monto;
            facturasMes[idx]++;
            totalIngresos += monto;

            String metodo = f.getMetodoPago() != null ? f.getMetodoPago().name() : "no_registrado";
            double[] acum = porMetodo.computeIfAbsent(metodo, k -> new double[2]);
            acum[0] += monto;
            acum[1] += 1;
        }

        List<IngresoMensual> ingresosPorMes = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            ingresosPorMes.add(new IngresoMensual(i + 1, MESES[i], ingresosMes[i], facturasMes[i]));
        }

        List<IngresoPorCategoria> ingresosPorMetodoPago = new ArrayList<>();
        porMetodo.forEach((metodo, acum) ->
                ingresosPorMetodoPago.add(new IngresoPorCategoria(metodo, acum[0], (int) acum[1])));

        int totalFacturas = facturas.size();
        double ticketPromedio = totalFacturas > 0 ? totalIngresos / totalFacturas : 0;

        ResumenMensualidades mensualidades = resumenMensualidades(year);

        return new ReporteFinanciero(
                year,
                redondear(totalIngresos),
                totalFacturas,
                redondear(ticketPromedio),
                ingresosPorMes,
                ingresosPorMetodoPago,
                mensualidades
        );
    }

    /** Calcula el estado de cobranza de las mensualidades cuyo período cae en el año dado. */
    private ResumenMensualidades resumenMensualidades(int year) {
        int pagadas = 0, pendientes = 0, vencidas = 0;
        double montoPagado = 0, montoPendiente = 0;

        for (Mensualidades m : mensualidadesRepository.findAll()) {
            if (m.getPeriodo() == null || m.getPeriodo().getYear() != year) continue;
            double monto = montoDouble(m.getMonto());
            if (m.getEstado() == EstadoMensualidad.pagada) {
                pagadas++;
                montoPagado += monto;
            } else if (m.getEstado() == EstadoMensualidad.vencida) {
                vencidas++;
                montoPendiente += monto;
            } else {
                pendientes++;
                montoPendiente += monto;
            }
        }

        return new ResumenMensualidades(
                pagadas, pendientes, vencidas,
                redondear(montoPagado), redondear(montoPendiente)
        );
    }

    private double montoDouble(BigDecimal v) {
        return v != null ? v.doubleValue() : 0d;
    }

    private double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
