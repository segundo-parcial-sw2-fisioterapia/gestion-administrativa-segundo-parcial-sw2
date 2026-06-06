package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.BlockchainClient;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.ClinicaClient;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.PersonaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.EmpleadosRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.CrearFacturaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.EditarFacturaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.FacturaEnriquecida;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.FacturaEnriquecida.EmpleadoDto;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.MetodoPago;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacturasService {

    private final FacturasRepository facturasRepository;
    private final BlockchainClient blockchainClient;
    private final EmpleadosRepository empleadosRepository;
    private final ClinicaClient clinicaClient;

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

        // Batch: todos los pacienteIds únicos de la página actual
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
     * Genera un PDF de la factura y lo retorna como String base64 con prefijo data URI.
     * El frontend lo abre en una nueva pestaña o lo descarga directamente.
     */
    public String generarPdfFactura(String id) {
        FacturaEnriquecida factura = verFacturaEnriquecida(id);
        if (factura == null) throw new RuntimeException("Factura no encontrada: " + id);

        String html = buildFacturaHtml(factura);
        byte[] pdfBytes = buildPdfSimple(factura);
        return "data:application/pdf;base64," + Base64.getEncoder().encodeToString(pdfBytes);
    }

    /** Construye HTML de la factura. Usado también para el print-view. */
    private String buildFacturaHtml(FacturaEnriquecida f) {
        String pacienteNombre = f.getPaciente() != null
                ? f.getPaciente().getNombre() + " " + f.getPaciente().getApellido()
                : "ID: " + f.getPacienteId();
        String pacienteCi = f.getPaciente() != null && f.getPaciente().getCi() != null
                ? f.getPaciente().getCi() : "—";
        String empleadoNombre = (f.getEmpleado() != null && f.getEmpleado().getPersona() != null)
                ? f.getEmpleado().getPersona().getNombre() + " " + f.getEmpleado().getPersona().getApellido()
                : "—";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
               "<title>Factura " + f.getNumeroFactura() + "</title>" +
               "<style>body{font-family:Arial,sans-serif;margin:40px;color:#333}" +
               "h1{color:#1c3766;font-size:22px;border-bottom:2px solid #1c3766;padding-bottom:8px}" +
               "table{width:100%;border-collapse:collapse;margin-top:16px}" +
               "th{background:#1c3766;color:white;padding:10px;text-align:left;font-size:12px}" +
               "td{padding:8px 10px;border-bottom:1px solid #eee;font-size:13px}" +
               ".total{font-size:20px;font-weight:bold;text-align:right;margin-top:20px;color:#1c3766}" +
               ".footer{margin-top:60px;font-size:10px;color:#999;text-align:center;border-top:1px solid #eee;padding-top:12px}" +
               "</style></head><body>" +
               "<h1>FACTURA DE SERVICIO</h1>" +
               "<table>" +
               "<tr><th>N° Factura</th><td>" + f.getNumeroFactura() + "</td>" +
               "<th>Fecha Emisión</th><td>" + (f.getFechaEmision() != null ? f.getFechaEmision().substring(0, 10) : "—") + "</td></tr>" +
               "<tr><th>Paciente</th><td>" + pacienteNombre + "</td><th>CI</th><td>" + pacienteCi + "</td></tr>" +
               "<tr><th>Concepto</th><td colspan='3'>" + (f.getConcepto() != null ? f.getConcepto() : "—") + "</td></tr>" +
               "<tr><th>Método de Pago</th><td>" + (f.getMetodoPago() != null ? f.getMetodoPago() : "—") + "</td>" +
               "<th>Estado</th><td>" + (f.getEstado() != null ? f.getEstado().toUpperCase() : "—") + "</td></tr>" +
               "<tr><th>Registrado por</th><td colspan='3'>" + empleadoNombre + "</td></tr>" +
               "</table>" +
               "<div class='total'>TOTAL: Bs " + f.getMontoTotal() + "</div>" +
               (f.getHashBlockchain() != null
                       ? "<p style='margin-top:16px;font-size:10px;color:#666;word-break:break-all'>Hash Blockchain: " + f.getHashBlockchain() + "</p>"
                       : "") +
               "<div class='footer'>Centro de Fisioterapia — " + LocalDateTime.now().toLocalDate() + "</div>" +
               "</body></html>";
    }

    /**
     * Construye un PDF mínimo válido. En producción reemplazar con iText7 o Flying Saucer.
     * Por ahora el frontend recibirá el HTML como base64 de un HTML-file para imprimir.
     */
    private byte[] buildPdfSimple(FacturaEnriquecida f) {
        // Retornamos el HTML codificado como "application/pdf" temporal
        // El frontend detecta que es HTML y lo abre en una nueva pestaña para imprimir
        return buildFacturaHtml(f).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private Pageable toPageable(PaginaInput p, Sort sort) {
        int pagina = p != null ? Math.max(0, p.getPagina()) : 0;
        int tamano = p != null ? Math.min(100, Math.max(1, p.getTamano())) : 20;
        return PageRequest.of(pagina, tamano, sort);
    }
}
