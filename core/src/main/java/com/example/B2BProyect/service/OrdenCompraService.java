package com.example.B2BProyect.service;

import com.example.B2BProyect.repository.FacturaRepository;
import com.example.B2BProyect.repository.OrdenCompraRepository;
import com.example.B2BProyect.repository.dto.request.OrdenCompraRequest;
import com.example.B2BProyect.repository.dto.request.OrdenUpdateRequest;
import com.example.B2BProyect.repository.dto.response.OrdenCompraDTO;
import com.example.B2BProyect.repository.dto.response.OrdenEmpresaStats;
import com.example.B2BProyect.repository.entity.Factura;
import com.example.B2BProyect.repository.entity.OrdenCompra;
import com.example.B2BProyect.repository.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class OrdenCompraService {
    private final OrdenCompraRepository ordenCompraRepository;
    private final ProveedorService proveedorService;
    private final UsuarioService usuarioService;
    private final FacturaRepository facturaRepository;

    @Transactional
    public OrdenCompraDTO save(OrdenCompraRequest request) {
        UUID idempotency = UUID.randomUUID();
        Optional<OrdenCompra> existing = ordenCompraRepository.findById(idempotency);
        if (existing.isPresent()) {
            log.info("Orden ya existe, retornando existente: {}", idempotency);
            return new OrdenCompraDTO(existing.get());
        }

        OrdenCompra orden = new OrdenCompra();
        orden.setId(idempotency);
        orden.setTotal(request.getTotal());
        orden.setFecha(request.getFecha());
        orden.setFechaOrden(request.getFechaOrden());
        orden.setIdEstado(request.getIdEstado() != null ? request.getIdEstado() : "pendiente");

        if (request.getIdUsuario() != null) {
            Usuario usuario = usuarioService.findById(request.getIdUsuario()).orElseThrow();
            orden.setIdUsuario(usuario);
            orden.setIdEmpresaCompradora(usuario.getIdEmpresa());
            orden.setIdSucursal(usuario.getIdSucursal());
        }

        orden.setIdProveedor(proveedorService.findById(request.getIdProveedor()).orElseThrow());

        log.info("Guardando nueva orden: {}", orden.getId());
        return new OrdenCompraDTO(ordenCompraRepository.save(orden));
    }

    @Transactional(readOnly = true)
    public List<OrdenCompraDTO> findAll() {
        return ordenCompraRepository.findAll().stream().map(OrdenCompraDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public Optional<OrdenCompra> findById(UUID id) {
        return ordenCompraRepository.findById(id);
    }
    @Transactional
    public void updateStatus(OrdenUpdateRequest ordenUpdateRequest){
        OrdenCompra ordenCompra = ordenCompraRepository.findById(ordenUpdateRequest.getId()).orElseThrow();
        ordenCompra.setIdEstado(ordenUpdateRequest.getIdEstado());

        if (ordenCompra.getIdEstado().equals("aprobado")) {
            Factura factura = new Factura();
            factura.setFecha(Instant.now());
            factura.setTotal(ordenCompra.getTotal());
            factura.setIdOrden(ordenCompra);
            factura.setIdEstado("pendiente");
            factura.setCreatedBy(ordenCompra.getCreatedBy());
            factura.setCreatedDate(LocalDateTime.now());
            factura.setModifiedBy(ordenCompra.getIdProveedor().getIdEmpresa().getNombre());
            facturaRepository.save(factura);
        }
    }

    @Transactional
    public Optional<OrdenCompraDTO> update(UUID id, OrdenCompraRequest dto) {
        return ordenCompraRepository.findById(id).map(orden -> {
            if (dto.getTotal() != null)      orden.setTotal(dto.getTotal());
            if (dto.getFecha() != null)      orden.setFecha(dto.getFecha());
            if (dto.getFechaOrden() != null) orden.setFechaOrden(dto.getFechaOrden());
            if (dto.getIdEstado() != null)   orden.setIdEstado(dto.getIdEstado());
            if (dto.getIdProveedor() != null)
                proveedorService.findById(dto.getIdProveedor()).ifPresent(orden::setIdProveedor);
//            if (dto.getIdEmpresaCompradora() != null)
//                empresaService.findById(dto.getIdEmpresaCompradora()).ifPresent(orden::setIdEmpresaCompradora);
//            if (dto.getIdSucursal() != null)
//                sucursalEmpresaService.findById(dto.getIdSucursal()).ifPresent(orden::setIdSucursal);
            if (dto.getIdUsuario() != null){
                orden.setIdUsuario(usuarioService.findById(dto.getIdUsuario()).orElseThrow());
                orden.setIdEmpresaCompradora(orden.getIdUsuario().getIdEmpresa());
                orden.setIdSucursal(orden.getIdUsuario().getIdSucursal());
            }
            return new OrdenCompraDTO(ordenCompraRepository.save(orden));
        });
    }

    @Transactional
    public boolean delete(UUID id) {
        if (!ordenCompraRepository.existsById(id)) return false;
        ordenCompraRepository.deleteById(id);
        return true;
    }

    @Transactional(readOnly = true)
    public Page<OrdenCompraDTO> findByEmpresaCompradora(UUID idEmpresa, Integer size, Integer page){
        log.info("ID " + idEmpresa);
        return ordenCompraRepository.retrieveAllFromEmpresaCompradora(idEmpresa, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<OrdenCompraDTO> findByEmpresaProveedora(UUID idEmpresa, Integer size, Integer page){
        log.info("ID " + idEmpresa);
        return ordenCompraRepository.retrieveAllFromEmpresaProveedora(idEmpresa, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<OrdenCompraDTO> findAllPaged(int page, int size) {
        return ordenCompraRepository.findAll(PageRequest.of(page, size)).map(OrdenCompraDTO::new);
    }

    @Transactional(readOnly = true)
    public OrdenEmpresaStats retrieveStats(UUID id){
        return ordenCompraRepository.obtenerStats(id);
    }

    @Transactional(readOnly = true)
    public Page<OrdenCompraDTO> findAllByOrderByDateDesc(LocalDateTime pInit, LocalDateTime pEnd, Pageable pageable) {
        return ordenCompraRepository.findAllByOrderByDateDesc(pInit, pEnd, pageable);
    }
}
