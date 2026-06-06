package com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos;

import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInfo;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.compartido.PaginaInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.dto.CrearPagoInput;
import com.fisioterapia.gestion_administrativa_segundo_parcial_sw2.pagos.dto.EditarPagoInput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PagosService {

    private final PagosRepository pagosRepository;

    /** Lista pagos paginados, ordenados por fecha de pago descendente. */
    public PagosPaginados listarPagos(PaginaInput pagina) {
        Pageable p = toPageable(pagina, Sort.by("fechaPago").descending());
        Page<Pagos> page = pagosRepository.findAll(p);
        return new PagosPaginados(page.getContent(), PaginaInfo.de(page));
    }

    /** @return El pago con el ID dado, o null si no existe. */
    public Pagos verPago(String id) {
        return pagosRepository.findById(Long.parseLong(id)).orElse(null);
    }

    /** Lista los pagos de una factura específica. */
    public List<Pagos> listarPagosPorFactura(String facturaId) {
        return pagosRepository.findByFacturaId(Long.parseLong(facturaId));
    }

    /**
     * Registra un nuevo pago vinculado a una factura.
     *
     * @param input Datos del pago.
     * @return El pago creado.
     */
    @Transactional
    public Pagos crearPagos(CrearPagoInput input) {
        Pagos pago = Pagos.builder()
                .facturaId(Long.parseLong(input.getFacturaId()))
                .fechaPago(LocalDateTime.parse(input.getFechaPago()))
                .monto(input.getMonto())
                .metodoPago(input.getMetodoPago())
                .referencia(input.getReferencia())
                .registradoPor(input.getRegistradoPor() != null ? Long.parseLong(input.getRegistradoPor()) : null)
                .build();
        return pagosRepository.save(pago);
    }

    /**
     * Actualiza los datos de un pago existente.
     *
     * @param id    ID del pago.
     * @param input Campos a modificar.
     * @return El pago actualizado.
     */
    @Transactional
    public Pagos editarPago(String id, EditarPagoInput input) {
        Pagos pago = pagosRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + id));
        if (input.getFechaPago() != null) pago.setFechaPago(LocalDateTime.parse(input.getFechaPago()));
        if (input.getMonto() != null) pago.setMonto(input.getMonto());
        if (input.getMetodoPago() != null) pago.setMetodoPago(input.getMetodoPago());
        if (input.getReferencia() != null) pago.setReferencia(input.getReferencia());
        return pagosRepository.save(pago);
    }

    /**
     * Elimina un pago por su ID.
     *
     * @param id ID del pago.
     * @return true si fue eliminado.
     */
    @Transactional
    public Boolean eliminarPago(String id) {
        pagosRepository.deleteById(Long.parseLong(id));
        return true;
    }

    private Pageable toPageable(PaginaInput p, Sort sort) {
        int pagina = p != null ? Math.max(0, p.getPagina()) : 0;
        int tamano = p != null ? Math.min(100, Math.max(1, p.getTamano())) : 20;
        return PageRequest.of(pagina, tamano, sort);
    }
}
