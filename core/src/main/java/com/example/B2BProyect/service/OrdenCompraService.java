package com.example.B2BProyect.service;

import com.example.B2BProyect.repository.OrdenCompraRepository;
import com.example.B2BProyect.repository.dto.request.OrdenCompraRequest;
import com.example.B2BProyect.repository.dto.request.OrdenUpdateRequest;
import com.example.B2BProyect.repository.dto.response.OrdenCompraDTO;
import com.example.B2BProyect.repository.dto.response.OrdenEmpresaStats;
import com.example.B2BProyect.repository.entity.OrdenCompra;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final EmpresaService empresaService;
    private final SucursalEmpresaService sucursalEmpresaService;
    private final UsuarioService usuarioService;

    @Transactional
    public OrdenCompraDTO save(OrdenCompraRequest request, UUID idempotency) {
        OrdenCompra orden = new OrdenCompra();
        orden.setTotal(request.getTotal());
        orden.setFecha(request.getFecha());
        orden.setFechaOrden(request.getFechaOrden());
        orden.setId(idempotency);
        orden.setVersion(request.getVersion());
        ordenCompraRepository.existsById(idempotency);
        log.info("ORDEN A GUARDAR: " + orden.getId());
        orden.setIdEstado(request.getIdEstado() != null ? request.getIdEstado() : "pendiente");
        if (request.getIdUsuario() != null) {
            orden.setIdUsuario(usuarioService.findById(request.getIdUsuario()).orElseThrow());
            orden.setIdEmpresaCompradora(orden.getIdUsuario().getIdEmpresa());
            orden.setIdSucursal(orden.getIdUsuario().getIdSucursal());
        }
        orden.setIdProveedor(proveedorService.findById(request.getIdProveedor()).orElseThrow());
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
