package com.example.B2BProyect.service;

import com.example.B2BProyect.repository.ContratoEmpresaDetalleRepository;
import com.example.B2BProyect.repository.ProveedorRepository;
import com.example.B2BProyect.repository.dto.request.ContratoEmpresaDetalleRequest;
import com.example.B2BProyect.repository.dto.response.ContratoEmpresaDetalleDTO;
import com.example.B2BProyect.repository.dto.response.ContratoEmpresaStats;
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
public class ContratoEmpresaDetalleService {
    private final ContratoEmpresaDetalleRepository contratoEmpresaDetalleRepository;
    private final ProductoService productoService;
    private final ContratoEmpresaTarifaService contratoEmpresaTarifaService;
    private final ProveedorRepository proveedorRepository;

    @Transactional
    public void save(ContratoEmpresaDetalleRequest request) {
        ContratoEmpresaDetalle detalle = new ContratoEmpresaDetalle();
        detalle.setPorcentajeDescuento(request.getPorcentajeDescuento());
        if (request.getIdProducto() != null)
            productoService.findById(request.getIdProducto()).ifPresent(detalle::setIdProducto);
        if (request.getIdContrato() != null)
            contratoEmpresaTarifaService.findById(request.getIdContrato()).ifPresent(detalle::setIdContrato);
        contratoEmpresaDetalleRepository.save(detalle);
    }

    @Transactional(readOnly = true)
    public List<ContratoEmpresaDetalleDTO> findAll() {
        return contratoEmpresaDetalleRepository.findAll().stream().map(ContratoEmpresaDetalleDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public Page<ContratoEmpresaDetalleDTO> findAllByEmpresa(UUID idEmpresa, Integer page, Integer size){
        Page<ContratoEmpresaDetalleDTO> empresaDetalleDTOS;
        if(proveedorRepository.existsProveedorByIdEmpresaId((idEmpresa))) {
            empresaDetalleDTOS = contratoEmpresaDetalleRepository.findAllByIdContratoIdEmpresaIdProveedor(idEmpresa, PageRequest.of(page, size));
        } else {
            empresaDetalleDTOS = contratoEmpresaDetalleRepository.findAllByIdContratoIdEmpresaIdEmpresa(idEmpresa, PageRequest.of(page, size));
        }
        return empresaDetalleDTOS;
    }

    @Transactional(readOnly = true)
    public ContratoEmpresaStats retrieveStatsFromEmpresa(UUID idEmpresa){
        return contratoEmpresaDetalleRepository.findStatsForEmpresa(idEmpresa);
    }

    @Transactional(readOnly = true)
    public Optional<ContratoEmpresaDetalle> findById(UUID id) {
        return contratoEmpresaDetalleRepository.findById(id);
    }

    @Transactional
    public Optional<ContratoEmpresaDetalleDTO> update(UUID id, ContratoEmpresaDetalleRequest dto) {
        return contratoEmpresaDetalleRepository.findById(id).map(detalle -> {
            if (dto.getPorcentajeDescuento() != null) detalle.setPorcentajeDescuento(dto.getPorcentajeDescuento());
            if (dto.getIdProducto() != null)
                productoService.findById(dto.getIdProducto()).ifPresent(detalle::setIdProducto);
            if (dto.getIdContrato() != null)
                contratoEmpresaTarifaService.findById(dto.getIdContrato()).ifPresent(detalle::setIdContrato);
            return new ContratoEmpresaDetalleDTO(contratoEmpresaDetalleRepository.save(detalle));
        });
    }

    @Transactional
    public boolean delete(UUID id) {
        if (!contratoEmpresaDetalleRepository.existsById(id)) return false;
        contratoEmpresaDetalleRepository.deleteById(id);
        return true;
    }
}
