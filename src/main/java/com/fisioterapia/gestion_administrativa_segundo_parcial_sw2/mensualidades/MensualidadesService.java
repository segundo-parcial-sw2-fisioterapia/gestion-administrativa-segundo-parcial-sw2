package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.ClinicaClient;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.PersonaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.Empleados;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.EmpleadosRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.EstadoFactura;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.Facturas;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.FacturasRepository;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.FacturaEnriquecida;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.facturas.dto.FacturaEnriquecida.EmpleadoDto;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto.CrearMensualidadInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto.MensualidadDto;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto.MensualidadEnriquecida;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto.PagoRegistrado;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.mensualidades.dto.RegistrarPagoMensualidadInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.MetodoPago;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MensualidadesService {

    private final MensualidadesRepository mensualidadesRepository;
    private final FacturasRepository facturasRepository;
    private final EmpleadosRepository empleadosRepository;
    private final ClinicaClient clinicaClient;

    /**
     * Lista mensualidades con paginación ordenadas por período descendente.
     */
    public MensualidadesPaginadas listarMensualidades(PaginaInput pagina) {
        Pageable p = toPageable(pagina, Sort.by("periodo").descending());
        Page<Mensualidades> page = mensualidadesRepository.findAll(p);
        return new MensualidadesPaginadas(page.getContent(), PaginaInfo.de(page));
    }

    /**
     * Lista mensualidades enriquecidas con nombre/CI del paciente.
     * Soporta filtro por pacienteId, estado y paginación con orden de período ascendente
     * para que aparezcan primero las más próximas a vencer.
     *
     * @param pacienteId ID del paciente (opcional).
     * @param estado     Estado de la mensualidad (opcional).
     * @param pagina     Parámetros de paginación.
     */
    public MensualidadesPaginadasEnriquecidas listarMensualidadesEnriquecidas(
            String pacienteId, EstadoMensualidad estado, PaginaInput pagina) {

        Pageable p = toPageable(pagina, Sort.by("periodo").ascending());
        Page<Mensualidades> page;

        if (pacienteId != null && estado != null) {
            page = mensualidadesRepository.findByPacienteIdAndEstado(Long.parseLong(pacienteId), estado, p);
        } else if (pacienteId != null) {
            page = mensualidadesRepository.findByPacienteId(Long.parseLong(pacienteId), p);
        } else if (estado != null) {
            page = mensualidadesRepository.findByEstado(estado, p);
        } else {
            page = mensualidadesRepository.findAll(p);
        }

        List<Long> pacienteIds = page.getContent().stream()
                .map(Mensualidades::getPacienteId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, PersonaInfo> pacientesMap = clinicaClient.obtenerPacientesBatch(pacienteIds);

        List<MensualidadEnriquecida> contenido = page.getContent().stream()
                .map(m -> {
                    MensualidadEnriquecida enriq = new MensualidadEnriquecida();
                    enriq.setId(m.getId());
                    enriq.setPlanTratamientoId(m.getPlanTratamientoId());
                    enriq.setPacienteId(m.getPacienteId());
                    enriq.setPeriodo(m.getPeriodo().toString());
                    enriq.setMonto(m.getMonto());
                    enriq.setEstado(m.getEstado().name());
                    enriq.setFechaCreacion(m.getFechaCreacion() != null ? m.getFechaCreacion().toString() : null);
                    enriq.setPaciente(pacientesMap.get(m.getPacienteId()));
                    return enriq;
                })
                .collect(Collectors.toList());

        return new MensualidadesPaginadasEnriquecidas(contenido, PaginaInfo.de(page));
    }

    /** @return La mensualidad con el ID dado, o null si no existe. */
    public Mensualidades verMensualidad(String id) {
        return mensualidadesRepository.findById(Long.parseLong(id)).orElse(null);
    }

    /** Lista todas las mensualidades de un paciente específico. */
    public List<Mensualidades> listarMensualidadesPorPaciente(String pacienteId) {
        return mensualidadesRepository.findByPacienteId(Long.parseLong(pacienteId));
    }

    /** Lista todas las mensualidades de un plan de tratamiento específico. */
    public List<Mensualidades> listarMensualidadesPorPlan(String planId) {
        return mensualidadesRepository.findByPlanTratamientoId(Long.parseLong(planId));
    }

    /** Lista las mensualidades pendientes y vencidas (no pagadas). */
    public List<Mensualidades> listarMensualidadesPendientes() {
        List<Mensualidades> pendientes = mensualidadesRepository.findByEstado(EstadoMensualidad.pendiente);
        List<Mensualidades> vencidas = mensualidadesRepository.findByEstado(EstadoMensualidad.vencida);
        pendientes.addAll(vencidas);
        return pendientes;
    }

    /**
     * Registra una nueva mensualidad en estado pendiente.
     */
    @Transactional
    public Mensualidades crearMensualidades(CrearMensualidadInput input) {
        Mensualidades mensualidad = Mensualidades.builder()
                .planTratamientoId(Long.parseLong(input.getPlanTratamientoId()))
                .pacienteId(Long.parseLong(input.getPacienteId()))
                .periodo(LocalDate.parse(input.getPeriodo()))
                .monto(input.getMonto())
                .estado(EstadoMensualidad.pendiente)
                .fechaCreacion(LocalDateTime.now())
                .build();
        return mensualidadesRepository.save(mensualidad);
    }

    /**
     * Registra el pago de una mensualidad: cambia su estado a pagada, genera la factura
     * y devuelve ambos objetos enriquecidos con datos del empleado y del paciente.
     *
     * @param input Datos del pago (mensualidadId, empleadoId, metodoPago).
     * @return PagoRegistrado con la mensualidad actualizada y la factura emitida.
     */
    @Transactional
    public PagoRegistrado registrarPagoMensualidad(RegistrarPagoMensualidadInput input) {
        Mensualidades mensualidad = mensualidadesRepository.findById(Long.parseLong(input.getMensualidadId()))
                .orElseThrow(() -> new RuntimeException("Mensualidad no encontrada: " + input.getMensualidadId()));

        mensualidad.setEstado(EstadoMensualidad.pagada);
        mensualidadesRepository.save(mensualidad);

        // Habilitar sesiones clínicas asociadas en el MS de Clínica
        clinicaClient.habilitarSesionesPorMensualidad(mensualidad.getId());

        String numeroFactura = "FAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String concepto = "Mensualidad " + mensualidad.getPeriodo().toString()
                + " - Paciente ID: " + mensualidad.getPacienteId();

        Facturas.FacturasBuilder facturaBuilder = Facturas.builder()
                .pacienteId(mensualidad.getPacienteId())
                .mensualidadId(mensualidad.getId())
                .numeroFactura(numeroFactura)
                .fechaEmision(LocalDateTime.now())
                .montoTotal(mensualidad.getMonto())
                .concepto(concepto)
                .estado(EstadoFactura.emitida)
                .fechaCreacion(LocalDateTime.now());

        if (input.getEmpleadoId() != null && !input.getEmpleadoId().trim().isEmpty() && !input.getEmpleadoId().equals("undefined")) {
            try {
                facturaBuilder.empleadoId(Long.parseLong(input.getEmpleadoId()));
            } catch (NumberFormatException e) {
                System.err.println("Error parsing empleadoId: " + input.getEmpleadoId());
            }
        }
        if (input.getMetodoPago() != null && !input.getMetodoPago().trim().isEmpty() && !input.getMetodoPago().equals("undefined")) {
            try {
                facturaBuilder.metodoPago(MetodoPago.valueOf(input.getMetodoPago().toLowerCase()));
            } catch (IllegalArgumentException e) {
                System.err.println("Error parsing metodoPago: " + input.getMetodoPago());
            }
        }

        Facturas factura = facturasRepository.save(facturaBuilder.build());

        FacturaEnriquecida facturaEnriquecida = new FacturaEnriquecida(factura);

        // Enriquecer con datos del paciente
        Map<Long, PersonaInfo> pacientesMap = clinicaClient.obtenerPacientesBatch(List.of(mensualidad.getPacienteId()));
        facturaEnriquecida.setPaciente(pacientesMap.get(mensualidad.getPacienteId()));

        // Enriquecer con datos del empleado que registró el pago
        if (factura.getEmpleadoId() != null) {
            empleadosRepository.findById(factura.getEmpleadoId()).ifPresent(emp -> {
                clinicaClient.obtenerPersona(emp.getPersonaId()).ifPresent(emp::setPersona);
                facturaEnriquecida.setEmpleado(EmpleadoDto.desde(emp));
            });
        }

        return new PagoRegistrado(MensualidadDto.desde(mensualidad), facturaEnriquecida);
    }

    private Pageable toPageable(PaginaInput p, Sort sort) {
        int pagina = p != null ? Math.max(0, p.getPagina()) : 0;
        int tamano = p != null ? Math.min(100, Math.max(1, p.getTamano())) : 20;
        return PageRequest.of(pagina, tamano, sort);
    }
}
