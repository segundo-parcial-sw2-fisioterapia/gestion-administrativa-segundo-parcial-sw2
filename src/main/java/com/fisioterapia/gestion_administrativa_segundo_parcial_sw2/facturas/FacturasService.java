package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.BlockchainClient;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.ClinicaClient;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.PersonaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.S3Service;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.EmpleadosRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.CrearFacturaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.EditarFacturaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.FacturaEnriquecida;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.FacturaEnriquecida.EmpleadoDto;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.MetodoPago;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacturasService {

    private final FacturasRepository facturasRepository;
    private final BlockchainClient blockchainClient;
    private final EmpleadosRepository empleadosRepository;
    private final ClinicaClient clinicaClient;
    private final S3Service s3Service;

    private static final Color AZUL_PRIMARIO = new Color(28, 55, 102);

    /**
     * Lista facturas con paginación ordenadas por fecha de emisión descendente.
     */
    public FacturasPaginadas listarFacturas(PaginaInput pagina) {
        Pageable p = toPageable(pagina, Sort.by("fechaEmision").descending());
        Page<Facturas> page = facturasRepository.findAll(p);
        return new FacturasPaginadas(page.getContent(), PaginaInfo.de(page));
    }

    /**
     * Lista facturas enriquecidas con datos del paciente desde clinica (batch).
     * Filtra opcionalmente por mes y año.
     *
     * @param mes   Mes (1-12). Si es null no filtra.
     * @param anio  Año (ej: 2026). Si es null no filtra.
     * @param pagina Paginación.
     */
    public FacturasEnriquecidaPaginadas listarFacturasEnriquecidas(Integer mes, Integer anio, PaginaInput pagina) {
        Pageable p = toPageable(pagina, Sort.by("fechaEmision").descending());
        Page<Facturas> page;

        if (mes != null && anio != null) {
            YearMonth ym = YearMonth.of(anio, mes);
            LocalDateTime desde = ym.atDay(1).atStartOfDay();
            LocalDateTime hasta = ym.atEndOfMonth().atTime(23, 59, 59);
            page = facturasRepository.findByFechaEmisionBetween(desde, hasta, p);
        } else if (anio != null) {
            LocalDateTime desde = LocalDate.of(anio, 1, 1).atStartOfDay();
            LocalDateTime hasta = LocalDate.of(anio, 12, 31).atTime(23, 59, 59);
            page = facturasRepository.findByFechaEmisionBetween(desde, hasta, p);
        } else {
            page = facturasRepository.findAll(p);
        }

        List<Long> pacienteIds = page.getContent().stream()
                .map(Facturas::getPacienteId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, PersonaInfo> pacientesMap = clinicaClient.obtenerPacientesBatch(pacienteIds);

        List<FacturaEnriquecida> contenido = page.getContent().stream()
                .map(f -> {
                    FacturaEnriquecida enriq = new FacturaEnriquecida(f);
                    enriq.setPaciente(pacientesMap.get(f.getPacienteId()));
                    return enriq;
                })
                .collect(Collectors.toList());

        return new FacturasEnriquecidaPaginadas(contenido, PaginaInfo.de(page));
    }

    /** @return La factura con el ID dado, o null si no existe. */
    public Facturas verFactura(String id) {
        return facturasRepository.findById(Long.parseLong(id)).orElse(null);
    }

    /** Lista todas las facturas de un paciente específico. */
    public List<Facturas> listarFacturasPorPaciente(String pacienteId) {
        return facturasRepository.findByPacienteId(Long.parseLong(pacienteId));
    }

    /** Lista todas las facturas asociadas a una mensualidad específica. */
    public List<Facturas> listarFacturasPorMensualidad(String mensualidadId) {
        return facturasRepository.findByMensualidadId(Long.parseLong(mensualidadId));
    }

    /** Crea una nueva factura en estado emitida. */
    @Transactional
    public Facturas crearFacturas(CrearFacturaInput input) {
        Facturas.FacturasBuilder builder = Facturas.builder()
                .pacienteId(Long.parseLong(input.getPacienteId()))
                .numeroFactura(input.getNumeroFactura())
                .fechaEmision(LocalDateTime.parse(input.getFechaEmision()))
                .montoTotal(input.getMontoTotal())
                .estado(EstadoFactura.emitida)
                .fechaCreacion(LocalDateTime.now())
                .urlDocumento(input.getUrlDocumento());
        if (input.getMensualidadId() != null) builder.mensualidadId(Long.parseLong(input.getMensualidadId()));
        if (input.getConcepto() != null) builder.concepto(input.getConcepto());
        if (input.getEmpleadoId() != null) builder.empleadoId(Long.parseLong(input.getEmpleadoId()));
        if (input.getMetodoPago() != null) builder.metodoPago(MetodoPago.valueOf(input.getMetodoPago()));
        return facturasRepository.save(builder.build());
    }

    /** Actualiza los campos modificables de una factura existente. */
    @Transactional
    public Facturas editarFactura(String id, EditarFacturaInput input) {
        Facturas factura = facturasRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + id));
        if (input.getPacienteId() != null) factura.setPacienteId(Long.parseLong(input.getPacienteId()));
        if (input.getNumeroFactura() != null) factura.setNumeroFactura(input.getNumeroFactura());
        if (input.getFechaEmision() != null) factura.setFechaEmision(LocalDateTime.parse(input.getFechaEmision()));
        if (input.getMontoTotal() != null) factura.setMontoTotal(input.getMontoTotal());
        if (input.getEstado() != null) factura.setEstado(input.getEstado());
        if (input.getMensualidadId() != null) factura.setMensualidadId(Long.parseLong(input.getMensualidadId()));
        if (input.getConcepto() != null) factura.setConcepto(input.getConcepto());
        if (input.getEmpleadoId() != null) factura.setEmpleadoId(Long.parseLong(input.getEmpleadoId()));
        if (input.getMetodoPago() != null) factura.setMetodoPago(MetodoPago.valueOf(input.getMetodoPago()));
        if (input.getUrlDocumento() != null) factura.setUrlDocumento(input.getUrlDocumento());
        return facturasRepository.save(factura);
    }

    /** Anula una factura cambiando su estado a anulada. */
    @Transactional
    public Facturas anularFactura(String id) {
        Facturas f = facturasRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + id));
        f.setEstado(EstadoFactura.anulada);
        return facturasRepository.save(f);
    }

    /** Elimina una factura del sistema por su ID. */
    @Transactional
    public Boolean eliminarFactura(String id) {
        facturasRepository.deleteById(Long.parseLong(id));
        return true;
    }

    /** Registra el hash de la factura en blockchain-firmas (MS-6). */
    @Transactional
    public Facturas registrarFacturaEnBlockchain(String id) {
        Facturas factura = facturasRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Factura no encontrada: " + id));
        String contenido = "FACTURA:" + factura.getNumeroFactura() + "|MONTO:" + factura.getMontoTotal();
        blockchainClient.firmar(contenido, "ADMINISTRATIVO").ifPresent(respuesta -> {
            factura.setHashBlockchain(respuesta.getHash());
            factura.setTxBlockchain(respuesta.getTxHash());
            factura.setFechaRegistroBlockchain(LocalDateTime.now());
        });
        return facturasRepository.save(factura);
    }

    /**
     * Retorna una factura enriquecida con datos del paciente y empleado.
     */
    public FacturaEnriquecida verFacturaEnriquecida(String id) {
        Facturas factura = facturasRepository.findById(Long.parseLong(id)).orElse(null);
        if (factura == null) return null;

        FacturaEnriquecida enriq = new FacturaEnriquecida(factura);

        Map<Long, PersonaInfo> pacientesMap = clinicaClient.obtenerPacientesBatch(List.of(factura.getPacienteId()));
        enriq.setPaciente(pacientesMap.get(factura.getPacienteId()));

        if (factura.getEmpleadoId() != null) {
            empleadosRepository.findById(factura.getEmpleadoId()).ifPresent(emp -> {
                clinicaClient.obtenerPersona(emp.getPersonaId()).ifPresent(emp::setPersona);
                enriq.setEmpleado(EmpleadoDto.desde(emp));
            });
        }

        return enriq;
    }

    /**
     * Genera el PDF de la factura con OpenPDF y lo retorna como base64 data URI.
     * El frontend lo abre en una nueva pestaña para visualización o descarga.
     */
    public String generarPdfFactura(String id) {
        FacturaEnriquecida factura = verFacturaEnriquecida(id);
        if (factura == null) throw new RuntimeException("Factura no encontrada: " + id);
        byte[] pdfBytes = generarPdfBytes(factura);
        return "data:application/pdf;base64," + Base64.getEncoder().encodeToString(pdfBytes);
    }

    /**
     * Genera el PDF de la factura con OpenPDF, lo sube a S3 y retorna la URL pública.
     * Llamado automáticamente desde MensualidadesService al registrar un pago.
     *
     * @param factura         Entidad Facturas recién creada (tiene el numeroFactura).
     * @param facturaEnriquecida DTO con datos del paciente y empleado.
     * @return URL pública del PDF en S3 (https://bucket.s3.region.amazonaws.com/facturas/...).
     */
    public String generarYSubirPdfFactura(Facturas factura, FacturaEnriquecida facturaEnriquecida) {
        byte[] pdfBytes = generarPdfBytes(facturaEnriquecida);
        String key = "facturas/" + factura.getNumeroFactura() + ".pdf";
        return s3Service.subirArchivo(key, pdfBytes, "application/pdf");
    }

    /**
     * Genera los bytes PDF de una factura usando OpenPDF.
     * Diseño profesional con colores institucionales del centro de fisioterapia.
     */
    public byte[] generarPdfBytes(FacturaEnriquecida f) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50f, 50f, 60f, 60f);

        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font fTitulo = new Font(Font.HELVETICA, 20, Font.BOLD, AZUL_PRIMARIO);
            Font fSubtitulo = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
            Font fEncabezado = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            Font fValor = new Font(Font.HELVETICA, 10);
            Font fTotal = new Font(Font.HELVETICA, 16, Font.BOLD, AZUL_PRIMARIO);
            Font fFooter = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.LIGHT_GRAY);
            Font fHash = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.GRAY);

            // --- Encabezado ---
            Paragraph titulo = new Paragraph("FACTURA DE SERVICIO", fTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(4f);
            doc.add(titulo);

            Paragraph subtitulo = new Paragraph("Centro de Rehabilitación y Fisioterapia", fSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(24f);
            doc.add(subtitulo);

            // --- Tabla de datos ---
            PdfPTable tabla = new PdfPTable(new float[]{35f, 65f});
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(4f);
            tabla.setSpacingAfter(20f);

            agregarFila(tabla, "Nº Factura", f.getNumeroFactura(), fEncabezado, fValor);

            String fechaStr = f.getFechaEmision() != null && f.getFechaEmision().length() >= 10
                    ? f.getFechaEmision().substring(0, 10) : "—";
            agregarFila(tabla, "Fecha de Emisión", fechaStr, fEncabezado, fValor);

            String paciente = f.getPaciente() != null
                    ? f.getPaciente().getNombre() + " " + f.getPaciente().getApellido()
                    : "Paciente ID: " + f.getPacienteId();
            agregarFila(tabla, "Paciente", paciente, fEncabezado, fValor);

            String ci = (f.getPaciente() != null && f.getPaciente().getCi() != null)
                    ? f.getPaciente().getCi() : "—";
            agregarFila(tabla, "CI / Documento", ci, fEncabezado, fValor);

            String concepto = f.getConcepto() != null ? f.getConcepto() : "—";
            agregarFila(tabla, "Concepto", concepto, fEncabezado, fValor);

            String metodoPago = f.getMetodoPago() != null ? f.getMetodoPago().toUpperCase() : "—";
            agregarFila(tabla, "Método de Pago", metodoPago, fEncabezado, fValor);

            String estado = f.getEstado() != null ? f.getEstado().toUpperCase() : "—";
            agregarFila(tabla, "Estado", estado, fEncabezado, fValor);

            String empleado = "—";
            if (f.getEmpleado() != null && f.getEmpleado().getPersona() != null) {
                var p = f.getEmpleado().getPersona();
                empleado = p.getNombre() + " " + p.getApellido() + " (" + f.getEmpleado().getCargo() + ")";
            }
            agregarFila(tabla, "Registrado por", empleado, fEncabezado, fValor);

            doc.add(tabla);

            // --- Total ---
            Paragraph total = new Paragraph("TOTAL:  Bs " + f.getMontoTotal(), fTotal);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingAfter(20f);
            doc.add(total);

            // --- Hash blockchain (si disponible) ---
            if (f.getHashBlockchain() != null) {
                Paragraph hashP = new Paragraph("Verificación Blockchain (SHA-256): " + f.getHashBlockchain(), fHash);
                hashP.setSpacingBefore(8f);
                hashP.setSpacingAfter(4f);
                doc.add(hashP);
                if (f.getTxBlockchain() != null) {
                    Paragraph txP = new Paragraph("Tx Sepolia: " + f.getTxBlockchain(), fHash);
                    doc.add(txP);
                }
            }

            // --- Pie de página ---
            Paragraph footer = new Paragraph("Documento emitido el " + LocalDate.now() +
                    " — Centro de Fisioterapia", fFooter);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30f);
            doc.add(footer);

        } catch (DocumentException e) {
            throw new RuntimeException("Error generando PDF de factura: " + e.getMessage(), e);
        } finally {
            if (doc.isOpen()) doc.close();
        }

        return baos.toByteArray();
    }

    private void agregarFila(PdfPTable tabla, String etiqueta, String valor,
                              Font fEncabezado, Font fValor) {
        PdfPCell celda = new PdfPCell(new Phrase(etiqueta, fEncabezado));
        celda.setBackgroundColor(AZUL_PRIMARIO);
        celda.setPadding(8f);
        celda.setBorderColor(Color.LIGHT_GRAY);
        tabla.addCell(celda);

        PdfPCell valor2 = new PdfPCell(new Phrase(valor != null ? valor : "—", fValor));
        valor2.setPadding(8f);
        valor2.setBorderColor(Color.LIGHT_GRAY);
        tabla.addCell(valor2);
    }

    private Pageable toPageable(PaginaInput p, Sort sort) {
        int pagina = p != null ? Math.max(0, p.getPagina()) : 0;
        int tamano = p != null ? Math.min(100, Math.max(1, p.getTamano())) : 20;
        return PageRequest.of(pagina, tamano, sort);
    }
}
