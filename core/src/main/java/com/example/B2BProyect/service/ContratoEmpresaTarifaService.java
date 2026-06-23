package com.example.B2BProyect.service;

import com.example.B2BProyect.repository.ContratoEmpresaTarifaRepository;
import com.example.B2BProyect.repository.ContratoEmpresaDetalleRepository;
import com.example.B2BProyect.repository.dto.request.ContratoCompletoRequest;
import com.example.B2BProyect.repository.dto.request.ContratoEmpresaTarifaRequest;
import com.example.B2BProyect.repository.dto.request.ContratoEmpresaDetalleRequest;
import com.example.B2BProyect.repository.dto.request.DetalleSimpleRequest;
import com.example.B2BProyect.repository.dto.response.ContratoEmpresaTarifasDTO;
import com.example.B2BProyect.repository.entity.ContratoEmpresaTarifa;
import com.example.B2BProyect.repository.entity.ContratoEmpresaDetalle;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ContratoEmpresaTarifaService {
    private final ContratoEmpresaTarifaRepository contratoEmpresaTarifaRepository;
    private final ContratoEmpresaDetalleRepository contratoEmpresaDetalleRepository;
    private final EmpresaService empresaService;
    private final ProveedorService proveedorService;
    private final TarifaReglaService tarifaReglaService;
    private final ProductoService productoService;

    @Transactional
    public ContratoEmpresaTarifasDTO save(ContratoEmpresaTarifaRequest request) {
        ContratoEmpresaTarifa contrato = new ContratoEmpresaTarifa();
        contrato.setVigenteDesde(request.getVigenteDesde());
        contrato.setVigenteHasta(request.getVigenteHasta());
        contrato.setActivo(request.getActivo() != null ? request.getActivo() : true);
        if (request.getIdEmpresa() != null)
            empresaService.findById(request.getIdEmpresa()).ifPresent(contrato::setIdEmpresa);
        if (request.getIdProveedor() != null)
            proveedorService.findById(request.getIdProveedor()).ifPresent(contrato::setIdProveedor);
        if (request.getIdRegla() != null)
            tarifaReglaService.findById(request.getIdRegla()).ifPresent(contrato::setIdRegla);
            
        ContratoEmpresaTarifa savedContrato = contratoEmpresaTarifaRepository.save(contrato);

        if (request.getDetalles() != null) {
            for (ContratoEmpresaDetalleRequest detReq : request.getDetalles()) {
                ContratoEmpresaDetalle detalle = new ContratoEmpresaDetalle();
                detalle.setPorcentajeDescuento(detReq.getPorcentajeDescuento());
                detalle.setIdContrato(savedContrato);
                if (detReq.getIdProducto() != null) {
                    productoService.findById(detReq.getIdProducto()).ifPresent(detalle::setIdProducto);
                }
                contratoEmpresaDetalleRepository.save(detalle);
            }
        }

        return new ContratoEmpresaTarifasDTO(savedContrato);
    }

    @Transactional(readOnly = true)
    public List<ContratoEmpresaTarifasDTO> findAll() {
        return contratoEmpresaTarifaRepository.findAll().stream().map(ContratoEmpresaTarifasDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ContratoEmpresaTarifa> findById(UUID id) {
        return contratoEmpresaTarifaRepository.findById(id);
    }

    @Transactional
    public Optional<ContratoEmpresaTarifasDTO> update(UUID id, ContratoEmpresaTarifaRequest dto) {
        return contratoEmpresaTarifaRepository.findById(id).map(contrato -> {
            if (dto.getVigenteDesde() != null) contrato.setVigenteDesde(dto.getVigenteDesde());
            if (dto.getVigenteHasta() != null) contrato.setVigenteHasta(dto.getVigenteHasta());
            if (dto.getActivo() != null)       contrato.setActivo(dto.getActivo());
            if (dto.getIdEmpresa() != null)
                empresaService.findById(dto.getIdEmpresa()).ifPresent(contrato::setIdEmpresa);
            if (dto.getIdProveedor() != null)
                proveedorService.findById(dto.getIdProveedor()).ifPresent(contrato::setIdProveedor);
            if (dto.getIdRegla() != null)
                tarifaReglaService.findById(dto.getIdRegla()).ifPresent(contrato::setIdRegla);
            return new ContratoEmpresaTarifasDTO(contratoEmpresaTarifaRepository.save(contrato));
        });
    }

    @Transactional
    public boolean delete(UUID id) {
        if (!contratoEmpresaTarifaRepository.existsById(id)) return false;
        contratoEmpresaTarifaRepository.deleteById(id);
        return true;
    }

    public ContratoEmpresaTarifasDTO saveCompleto(ContratoCompletoRequest request) {
        // 1. Guardar la Cabecera (ContratoEmpresaTarifa)
        ContratoEmpresaTarifa contrato = new ContratoEmpresaTarifa();
        contrato.setVigenteDesde(request.getVigenteDesde());
        contrato.setVigenteHasta(request.getVigenteHasta());
        contrato.setActivo(request.getActivo() != null ? request.getActivo() : true);

        if (request.getIdEmpresa() != null)
            empresaService.findById(request.getIdEmpresa()).ifPresent(contrato::setIdEmpresa);
        if (request.getIdProveedor() != null)
            proveedorService.findById(request.getIdProveedor()).ifPresent(contrato::setIdProveedor);
        if (request.getIdRegla() != null)
            tarifaReglaService.findById(request.getIdRegla()).ifPresent(contrato::setIdRegla);

        // Guardamos la cabecera primero para generar el UUID (id)
        ContratoEmpresaTarifa contratoGuardado = contratoEmpresaTarifaRepository.save(contrato);

        // 2. Iterar y Guardar los Detalles vinculándolos al Contrato recién guardado
        if (request.getDetalles() != null) {
            for (DetalleSimpleRequest detReq : request.getDetalles()) {
                ContratoEmpresaDetalle detalle = new ContratoEmpresaDetalle();
                detalle.setPorcentajeDescuento(detReq.getPorcentajeDescuento());
                detalle.setIdContrato(contratoGuardado); // Vinculamos la FK obligatoria

                if (detReq.getIdProducto() != null) {
                    productoService.findById(detReq.getIdProducto()).ifPresent(detalle::setIdProducto);
                }

                contratoEmpresaDetalleRepository.save(detalle);
            }
        }

        return new ContratoEmpresaTarifasDTO(contratoGuardado);
    }

    @Transactional(readOnly = true)
    public Page<ContratoEmpresaTarifasDTO> findAllPaged(int page, int size) {
        return contratoEmpresaTarifaRepository.findAll(PageRequest.of(page, size)).map(ContratoEmpresaTarifasDTO::new);
    }
}
