package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.ClinicaClient;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.config.PersonaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.dto.CrearEmpleadoInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.empleados.dto.EditarEmpleadoInput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpleadosService {

    private final EmpleadosRepository empleadosRepository;
    private final ClinicaClient clinicaClient;

    /**
     * Lista empleados paginados y enriquece cada uno con datos de persona desde clinica.
     * El enriquecimiento es best-effort: si clinica no responde, persona queda null.
     *
     * @param pagina Parámetros de paginación (página y tamaño).
     * @return Página de empleados con información de persona inyectada.
     */
    public EmpleadosPaginados listarEmpleados(PaginaInput pagina) {
        Pageable pageable = toPageable(pagina);
        Page<Empleados> page = empleadosRepository.findAll(pageable);

        List<Long> personaIds = page.getContent().stream()
                .map(Empleados::getPersonaId)
                .collect(Collectors.toList());

        Map<Long, PersonaInfo> personasMap = clinicaClient.obtenerPersonasBatch(personaIds);

        page.getContent().forEach(emp ->
                emp.setPersona(personasMap.get(emp.getPersonaId()))
        );

        return new EmpleadosPaginados(page.getContent(), PaginaInfo.de(page));
    }

    /**
     * Obtiene el detalle de un empleado por ID, enriquecido con datos de persona.
     *
     * @param id ID del empleado.
     * @return El empleado con datos de persona o null si no existe.
     */
    public Empleados verEmpleado(String id) {
        return empleadosRepository.findById(Long.parseLong(id)).map(emp -> {
            clinicaClient.obtenerPersona(emp.getPersonaId()).ifPresent(emp::setPersona);
            return emp;
        }).orElse(null);
    }

    /**
     * Busca empleados por cargo o especialidad, enriquecidos con datos de persona.
     * Usado por el selector FK del frontend para enlazar empleadoId en otros módulos.
     *
     * @param termino Cadena de búsqueda (case-insensitive).
     * @return Lista de empleados coincidentes con datos de persona.
     */
    public List<Empleados> buscarEmpleados(String termino) {
        List<Empleados> empleados = empleadosRepository.buscarPorCargoOEspecialidad(termino);
        List<Long> ids = empleados.stream().map(Empleados::getPersonaId).collect(Collectors.toList());
        Map<Long, PersonaInfo> personasMap = clinicaClient.obtenerPersonasBatch(ids);
        empleados.forEach(emp -> emp.setPersona(personasMap.get(emp.getPersonaId())));
        return empleados;
    }

    /**
     * Busca el empleado cuyo personaId coincide con el ID de persona del JWT.
     * Usado por el frontend para auto-rellenar empleadoId en formularios de pago.
     *
     * @param personaId ID de la persona en clinica (del campo sub/persona.id del JWT).
     * @return El empleado activo de esa persona, enriquecido con datos de persona, o null.
     */
    public Empleados verEmpleadoPorPersonaId(String personaId) {
        return empleadosRepository.findByPersonaId(Long.parseLong(personaId)).map(emp -> {
            clinicaClient.obtenerPersona(emp.getPersonaId()).ifPresent(emp::setPersona);
            return emp;
        }).orElse(null);
    }

    /**
     * Registra un nuevo empleado con referencia a su persona en el microservicio clínico.
     *
     * @param input Datos laborales del empleado.
     * @return El empleado creado.
     */
    @Transactional
    public Empleados crearEmpleados(CrearEmpleadoInput input) {
        Empleados empleado = Empleados.builder()
                .personaId(Long.parseLong(input.getPersonaId()))
                .cargo(input.getCargo())
                .especialidad(input.getEspecialidad())
                .salarioBase(input.getSalarioBase())
                .tipoContrato(input.getTipoContrato())
                .fechaIngreso(LocalDate.parse(input.getFechaIngreso()))
                .build();
        return empleadosRepository.save(empleado);
    }

    /**
     * Actualiza los datos laborales de un empleado existente.
     *
     * @param id    ID del empleado.
     * @param input Campos a modificar.
     * @return El empleado actualizado.
     */
    @Transactional
    public Empleados editarEmpleado(String id, EditarEmpleadoInput input) {
        Empleados empleado = empleadosRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado: " + id));
        if (input.getCargo() != null) empleado.setCargo(input.getCargo());
        if (input.getEspecialidad() != null) empleado.setEspecialidad(input.getEspecialidad());
        if (input.getSalarioBase() != null) empleado.setSalarioBase(input.getSalarioBase());
        if (input.getTipoContrato() != null) empleado.setTipoContrato(input.getTipoContrato());
        if (input.getFechaBaja() != null) empleado.setFechaBaja(LocalDate.parse(input.getFechaBaja()));
        if (input.getEstadoLaboral() != null) empleado.setEstadoLaboral(input.getEstadoLaboral());
        return empleadosRepository.save(empleado);
    }

    /**
     * Elimina un empleado por su ID.
     *
     * @param id ID del empleado a eliminar.
     * @return true si fue eliminado exitosamente.
     */
    @Transactional
    public Boolean eliminarEmpleado(String id) {
        empleadosRepository.deleteById(Long.parseLong(id));
        return true;
    }

    private Pageable toPageable(PaginaInput pagina) {
        int p = pagina != null ? Math.max(0, pagina.getPagina()) : 0;
        int t = pagina != null ? Math.min(100, Math.max(1, pagina.getTamano())) : 20;
        return PageRequest.of(p, t, Sort.by("id").ascending());
    }
}
