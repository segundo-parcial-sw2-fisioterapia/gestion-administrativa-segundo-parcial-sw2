package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.Empleados;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.EmpleadosRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.Facturas;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.FacturasRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos.Insumos;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.insumos.InsumosRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.Mensualidades;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.MensualidadesRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.movimientos_insumos.MovimientosInsumos;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.movimientos_insumos.MovimientosInsumosRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.Pagos;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.PagosRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.reportes.dto.dinamico.*;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.tarifas.Tarifas;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.tarifas.TarifasRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Motor de reportes dinámicos del subsistema de gestión empresarial (BI administrativo).
 *
 * Reemplaza la necesidad de escribir un resolver por reporte: a partir de una
 * {@link ReporteDinamicoInput} (fuente + métrica + dimensión + filtros) construye el
 * resultado agregado sobre datos reales. Cada fuente declara un whitelist de dimensiones
 * y campos numéricos; cualquier campo fuera del catálogo se rechaza. Esto hace seguro
 * que la capa de IA decida la consulta a partir de un prompt: nunca toca SQL ni campos
 * no permitidos.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteDinamicoService {

    private static final String[] MESES = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    };

    private final FacturasRepository facturasRepository;
    private final MensualidadesRepository mensualidadesRepository;
    private final PagosRepository pagosRepository;
    private final EmpleadosRepository empleadosRepository;
    private final InsumosRepository insumosRepository;
    private final MovimientosInsumosRepository movimientosInsumosRepository;
    private final TarifasRepository tarifasRepository;

    /** Descriptor whitelisteado de una fuente: cómo cargarla, agruparla y agregarla. */
    private record Descriptor(
            String etiqueta,
            Supplier<List<?>> datos,
            Function<Object, LocalDate> fechaPrimaria,                 // null si la fuente no tiene fecha
            Map<String, CampoExtractor<String>> dimensiones,          // campo -> etiqueta + extractor String
            Map<String, CampoExtractor<Double>> numericos             // campo -> etiqueta + extractor numérico
    ) {}

    private record CampoExtractor<T>(String etiqueta, Function<Object, T> extractor) {}

    private final Map<FuenteReporte, Descriptor> registro = new EnumMap<>(FuenteReporte.class);

    @PostConstruct
    void construirRegistro() {
        // ── FACTURAS ──────────────────────────────────────────────────────────────
        registro.put(FuenteReporte.FACTURAS, new Descriptor(
                "Facturas",
                () -> List.copyOf(facturasRepository.findAll()),
                o -> fecha(((Facturas) o).getFechaEmision()),
                dim(
                        d("mes", "Mes", o -> mesEtiqueta(((Facturas) o).getFechaEmision())),
                        d("anio", "Año", o -> anio(((Facturas) o).getFechaEmision())),
                        d("metodoPago", "Método de pago", o -> nombre(((Facturas) o).getMetodoPago(), "no_registrado")),
                        d("estado", "Estado", o -> nombre(((Facturas) o).getEstado(), "—"))
                ),
                num(
                        n("montoTotal", "Monto total (Bs)", o -> dbl(((Facturas) o).getMontoTotal()))
                )
        ));

        // ── MENSUALIDADES ─────────────────────────────────────────────────────────
        registro.put(FuenteReporte.MENSUALIDADES, new Descriptor(
                "Mensualidades",
                () -> List.copyOf(mensualidadesRepository.findAll()),
                o -> ((Mensualidades) o).getPeriodo(),
                dim(
                        d("mes", "Mes", o -> mesEtiqueta(((Mensualidades) o).getPeriodo())),
                        d("anio", "Año", o -> anio(((Mensualidades) o).getPeriodo())),
                        d("estado", "Estado", o -> nombre(((Mensualidades) o).getEstado(), "—"))
                ),
                num(
                        n("monto", "Monto (Bs)", o -> dbl(((Mensualidades) o).getMonto()))
                )
        ));

        // ── PAGOS ─────────────────────────────────────────────────────────────────
        registro.put(FuenteReporte.PAGOS, new Descriptor(
                "Pagos",
                () -> List.copyOf(pagosRepository.findAll()),
                o -> fecha(((Pagos) o).getFechaPago()),
                dim(
                        d("mes", "Mes", o -> mesEtiqueta(((Pagos) o).getFechaPago())),
                        d("anio", "Año", o -> anio(((Pagos) o).getFechaPago())),
                        d("metodoPago", "Método de pago", o -> nombre(((Pagos) o).getMetodoPago(), "—"))
                ),
                num(
                        n("monto", "Monto (Bs)", o -> dbl(((Pagos) o).getMonto()))
                )
        ));

        // ── EMPLEADOS ─────────────────────────────────────────────────────────────
        registro.put(FuenteReporte.EMPLEADOS, new Descriptor(
                "Empleados",
                () -> List.copyOf(empleadosRepository.findAll()),
                o -> ((Empleados) o).getFechaIngreso(),
                dim(
                        d("cargo", "Cargo", o -> textoOSino(((Empleados) o).getCargo(), "sin_cargo")),
                        d("especialidad", "Especialidad", o -> textoOSino(((Empleados) o).getEspecialidad(), "sin_especialidad")),
                        d("tipoContrato", "Tipo de contrato", o -> nombre(((Empleados) o).getTipoContrato(), "—")),
                        d("estadoLaboral", "Estado laboral", o -> nombre(((Empleados) o).getEstadoLaboral(), "—")),
                        d("anioIngreso", "Año de ingreso", o -> anio(((Empleados) o).getFechaIngreso()))
                ),
                num(
                        n("salarioBase", "Salario base (Bs)", o -> dbl(((Empleados) o).getSalarioBase()))
                )
        ));

        // ── INSUMOS ───────────────────────────────────────────────────────────────
        registro.put(FuenteReporte.INSUMOS, new Descriptor(
                "Insumos",
                () -> List.copyOf(insumosRepository.findAll()),
                null,
                dim(
                        d("categoria", "Categoría", o -> nombre(((Insumos) o).getCategoria(), "—")),
                        d("unidadMedida", "Unidad de medida", o -> textoOSino(((Insumos) o).getUnidadMedida(), "—")),
                        d("activo", "Activo", o -> Boolean.TRUE.equals(((Insumos) o).getActivo()) ? "activo" : "inactivo")
                ),
                num(
                        n("stockActual", "Stock actual", o -> (double) opt(((Insumos) o).getStockActual())),
                        n("precioUnitario", "Precio unitario (Bs)", o -> dbl(((Insumos) o).getPrecioUnitario())),
                        n("valorInventario", "Valor de inventario (Bs)",
                                o -> opt(((Insumos) o).getStockActual()) * dbl(((Insumos) o).getPrecioUnitario()))
                )
        ));

        // ── MOVIMIENTOS DE INSUMOS ────────────────────────────────────────────────
        registro.put(FuenteReporte.MOVIMIENTOS_INSUMOS, new Descriptor(
                "Movimientos de insumos",
                () -> List.copyOf(movimientosInsumosRepository.findAll()),
                o -> fecha(((MovimientosInsumos) o).getFecha()),
                dim(
                        d("mes", "Mes", o -> mesEtiqueta(((MovimientosInsumos) o).getFecha())),
                        d("anio", "Año", o -> anio(((MovimientosInsumos) o).getFecha())),
                        d("tipo", "Tipo de movimiento", o -> nombre(((MovimientosInsumos) o).getTipo(), "—"))
                ),
                num(
                        n("cantidad", "Cantidad", o -> (double) opt(((MovimientosInsumos) o).getCantidad()))
                )
        ));

        // ── TARIFAS ───────────────────────────────────────────────────────────────
        registro.put(FuenteReporte.TARIFAS, new Descriptor(
                "Tarifas",
                () -> List.copyOf(tarifasRepository.findAll()),
                null,
                dim(
                        d("categoriaSemaforo", "Categoría semáforo", o -> nombre(((Tarifas) o).getCategoriaSemaforo(), "—")),
                        d("nivel", "Nivel", o -> nombre(((Tarifas) o).getNivel(), "—"))
                ),
                num(
                        n("precioMensual", "Precio mensual (Bs)", o -> dbl(((Tarifas) o).getPrecioMensual()))
                )
        ));
    }

    // ─── API pública ──────────────────────────────────────────────────────────────

    /** Devuelve el catálogo de fuentes/dimensiones/métricas para la UI y la capa de IA. */
    public CatalogoReportes catalogo() {
        List<FuenteMeta> fuentes = new ArrayList<>();
        for (Map.Entry<FuenteReporte, Descriptor> e : registro.entrySet()) {
            Descriptor d = e.getValue();
            fuentes.add(new FuenteMeta(
                    e.getKey(),
                    d.etiqueta(),
                    d.fechaPrimaria() != null,
                    d.dimensiones().entrySet().stream()
                            .map(x -> new CampoMeta(x.getKey(), x.getValue().etiqueta())).toList(),
                    d.numericos().entrySet().stream()
                            .map(x -> new CampoMeta(x.getKey(), x.getValue().etiqueta())).toList()
            ));
        }
        fuentes.sort(Comparator.comparing(FuenteMeta::getEtiqueta));
        return new CatalogoReportes(fuentes);
    }

    /**
     * Ejecuta un reporte dinámico validando el input contra el catálogo y agregando
     * los datos reales de la fuente indicada.
     *
     * @param input Especificación de la consulta (fuente, métrica, dimensión, filtros).
     * @throws IllegalArgumentException si algún campo no está permitido para la fuente.
     */
    public ReporteDinamico ejecutar(ReporteDinamicoInput input) {
        if (input == null || input.getFuente() == null)
            throw new IllegalArgumentException("La fuente del reporte es obligatoria.");

        Descriptor desc = registro.get(input.getFuente());
        if (desc == null)
            throw new IllegalArgumentException("Fuente no soportada: " + input.getFuente());

        MetricaReporte metrica = input.getMetrica() != null ? input.getMetrica() : MetricaReporte.CONTEO;

        CampoExtractor<String> dim = desc.dimensiones().get(input.getAgruparPor());
        if (dim == null)
            throw new IllegalArgumentException(
                    "Dimensión '" + input.getAgruparPor() + "' no permitida para " + input.getFuente()
                            + ". Disponibles: " + desc.dimensiones().keySet());

        CampoExtractor<Double> numerico = null;
        if (metrica != MetricaReporte.CONTEO) {
            numerico = desc.numericos().get(input.getCampoMetrica());
            if (numerico == null)
                throw new IllegalArgumentException(
                        "Campo numérico '" + input.getCampoMetrica() + "' no permitido para " + input.getFuente()
                                + ". Disponibles: " + desc.numericos().keySet());
        }

        // Filtro de igualdad opcional sobre una dimensión.
        CampoExtractor<String> filtroDim = null;
        if (input.getFiltroCampo() != null && !input.getFiltroCampo().isBlank()) {
            filtroDim = desc.dimensiones().get(input.getFiltroCampo());
            if (filtroDim == null)
                throw new IllegalArgumentException(
                        "Campo de filtro '" + input.getFiltroCampo() + "' no permitido para " + input.getFuente());
        }

        // Cargar, filtrar y agrupar.
        Map<String, double[]> acumulador = new LinkedHashMap<>(); // etiqueta -> [suma, conteo]
        LocalDate hoy = LocalDate.now();
        for (Object fila : desc.datos().get()) {
            if (desc.fechaPrimaria() != null) {
                LocalDate f = desc.fechaPrimaria().apply(fila);
                if (f == null && (input.getAnio() != null || input.getRangoFecha() != null)) continue;
                if (f != null) {
                    if (input.getAnio() != null && f.getYear() != input.getAnio()) continue;
                    if (input.getRangoFecha() != null) {
                        boolean entra = switch (input.getRangoFecha().toLowerCase()) {
                            case "hoy" -> f.isEqual(hoy);
                            case "ultima_semana" -> !f.isBefore(hoy.minusDays(7)) && !f.isAfter(hoy);
                            case "ultimo_mes" -> !f.isBefore(hoy.minusMonths(1)) && !f.isAfter(hoy);
                            case "este_mes" -> f.getYear() == hoy.getYear() && f.getMonthValue() == hoy.getMonthValue();
                            case "este_anio" -> f.getYear() == hoy.getYear();
                            default -> true;
                        };
                        if (!entra) continue;
                    }
                }
            }
            if (filtroDim != null) {
                String valor = filtroDim.extractor().apply(fila);
                if (valor == null || !valor.equalsIgnoreCase(input.getFiltroValor())) continue;
            }
            String clave = dim.extractor().apply(fila);
            double aporte = numerico != null ? numerico.extractor().apply(fila) : 1.0;
            double[] acc = acumulador.computeIfAbsent(clave, k -> new double[2]);
            acc[0] += aporte;
            acc[1] += 1;
        }

        List<FilaReporte> filas = new ArrayList<>();
        double total = 0;
        for (Map.Entry<String, double[]> e : acumulador.entrySet()) {
            double valor = switch (metrica) {
                case CONTEO -> e.getValue()[1];
                case SUMA -> e.getValue()[0];
                case PROMEDIO -> e.getValue()[1] > 0 ? e.getValue()[0] / e.getValue()[1] : 0;
            };
            filas.add(new FilaReporte(e.getKey(), redondear(valor)));
            total += valor;
        }

        ordenar(filas, input.getAgruparPor());

        String etiquetaMetrica = switch (metrica) {
            case CONTEO -> "Cantidad";
            case SUMA -> "Suma de " + numerico.etiqueta();
            case PROMEDIO -> "Promedio de " + numerico.etiqueta();
        };

        return new ReporteDinamico(
                titulo(desc, input, dim, etiquetaMetrica),
                dim.etiqueta(),
                etiquetaMetrica,
                filas,
                redondear(metrica == MetricaReporte.PROMEDIO ? total / Math.max(1, filas.size()) : total),
                sugerirGrafico(input.getAgruparPor(), filas.size())
        );
    }

    // ─── Helpers de construcción del registro ──────────────────────────────────────

    @SafeVarargs
    private static Map<String, CampoExtractor<String>> dim(Map.Entry<String, CampoExtractor<String>>... e) {
        Map<String, CampoExtractor<String>> m = new LinkedHashMap<>();
        for (var x : e) m.put(x.getKey(), x.getValue());
        return m;
    }

    @SafeVarargs
    private static Map<String, CampoExtractor<Double>> num(Map.Entry<String, CampoExtractor<Double>>... e) {
        Map<String, CampoExtractor<Double>> m = new LinkedHashMap<>();
        for (var x : e) m.put(x.getKey(), x.getValue());
        return m;
    }

    private static Map.Entry<String, CampoExtractor<String>> d(String campo, String etiqueta, Function<Object, String> f) {
        return Map.entry(campo, new CampoExtractor<>(etiqueta, f));
    }

    private static Map.Entry<String, CampoExtractor<Double>> n(String campo, String etiqueta, Function<Object, Double> f) {
        return Map.entry(campo, new CampoExtractor<>(etiqueta, f));
    }

    // ─── Helpers de extracción/formato ──────────────────────────────────────────────

    private static LocalDate fecha(LocalDateTime dt) {
        return dt != null ? dt.toLocalDate() : null;
    }

    private static String mesEtiqueta(LocalDateTime dt) {
        return dt != null ? MESES[dt.getMonthValue() - 1] : "—";
    }

    private static String mesEtiqueta(LocalDate d) {
        return d != null ? MESES[d.getMonthValue() - 1] : "—";
    }

    private static String anio(LocalDateTime dt) {
        return dt != null ? String.valueOf(dt.getYear()) : "—";
    }

    private static String anio(LocalDate d) {
        return d != null ? String.valueOf(d.getYear()) : "—";
    }

    private static String nombre(Enum<?> e, String fallback) {
        return e != null ? e.name() : fallback;
    }

    private static String textoOSino(String s, String fallback) {
        return (s != null && !s.isBlank()) ? s : fallback;
    }

    private static double dbl(BigDecimal v) {
        return v != null ? v.doubleValue() : 0d;
    }

    private static int opt(Integer v) {
        return v != null ? v : 0;
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /** Ordena por mes calendario si la dimensión es temporal; si no, de mayor a menor valor. */
    private void ordenar(List<FilaReporte> filas, String dimension) {
        if ("mes".equals(dimension)) {
            List<String> orden = Arrays.asList(MESES);
            filas.sort(Comparator.comparingInt(f -> orden.indexOf(f.getEtiqueta())));
        } else if ("anio".equals(dimension) || "anioIngreso".equals(dimension)) {
            filas.sort(Comparator.comparing(FilaReporte::getEtiqueta));
        } else {
            filas.sort(Comparator.comparingDouble(FilaReporte::getValor).reversed());
        }
    }

    private TipoGrafico sugerirGrafico(String dimension, int grupos) {
        if (grupos <= 1) return TipoGrafico.KPI;
        if ("mes".equals(dimension) || "anio".equals(dimension) || "anioIngreso".equals(dimension))
            return TipoGrafico.LINEA;
        if (grupos <= 5) return TipoGrafico.PIE;
        return TipoGrafico.BARRA;
    }

    private String titulo(Descriptor desc, ReporteDinamicoInput input,
                          CampoExtractor<String> dim, String etiquetaMetrica) {
        String base = etiquetaMetrica + " · " + desc.etiqueta() + " por " + dim.etiqueta().toLowerCase();
        return input.getAnio() != null ? base + " (" + input.getAnio() + ")" : base;
    }
}
