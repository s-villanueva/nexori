package com.example.B2BProyect.service;

import com.example.B2BProyect.repository.AlmacenRepository;
import com.example.B2BProyect.repository.dto.request.AlmacenRequest;
import com.example.B2BProyect.repository.dto.response.AlmacenDTO;
import com.example.B2BProyect.repository.entity.Almacen;
import com.example.B2BProyect.repository.entity.Proveedor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AlmacenService {
    private final AlmacenRepository almacenRepository;
    private final ProveedorService proveedorService;

    @Transactional
    public void save(AlmacenRequest request) {
        Almacen almacen = new Almacen();
        almacen.setNombre(request.getNombre());
        almacen.setDireccion(request.getDireccion());
        almacen.setActivo(request.getActivo());
        Proveedor pr =proveedorService.findByIdEmpresa(request.getIdEmpresa());
        if (pr != null)
            almacen.setIdProveedor(pr);
        almacenRepository.save(almacen);
    }

    @Transactional(readOnly = true)
    public List<AlmacenDTO> findAll() {
        return almacenRepository.findAll().stream().map(AlmacenDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Almacen> findById(UUID id) {
        return almacenRepository.findById(id);
    }

    public List<AlmacenDTO> findByIdEmpresa(UUID id) {
        System.out.println("He llegado");
        List<AlmacenDTO> dtos = almacenRepository.findAllByIdProveedorIdEmpresaId((id));
        return dtos;
    }

    @Transactional
    public Optional<AlmacenDTO> update(UUID id, AlmacenRequest dto) {
        return almacenRepository.findById(id).map(almacen -> {
            if (dto.getNombre() != null)    almacen.setNombre(dto.getNombre());
            if (dto.getDireccion() != null) almacen.setDireccion(dto.getDireccion());
            if (dto.getActivo() != null)    almacen.setActivo(dto.getActivo());
            if (dto.getIdEmpresa() != null)
                almacen.setIdProveedor(proveedorService.findByIdEmpresa(dto.getIdEmpresa()));
            return new AlmacenDTO(almacenRepository.save(almacen));
        });
    }

    @Transactional
    public boolean delete(UUID id) {
        if (!almacenRepository.existsById(id)) return false;
        almacenRepository.deleteById(id);
        return true;
    }
}
