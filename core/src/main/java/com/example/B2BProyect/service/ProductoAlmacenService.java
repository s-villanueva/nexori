package com.example.B2BProyect.service;

import com.example.B2BProyect.repository.AlmacenRepository;
import com.example.B2BProyect.repository.ProductoAlmacenRepository;
import com.example.B2BProyect.repository.ProductoRepository;
import com.example.B2BProyect.repository.dto.request.ProductoAlmacenRequest;
import com.example.B2BProyect.repository.dto.response.ProductoAlmacenDTO;
import com.example.B2BProyect.repository.entity.Almacen;
import com.example.B2BProyect.repository.entity.Producto;
import com.example.B2BProyect.repository.entity.ProductoAlmacen;
import com.example.B2BProyect.repository.entity.ProductoAlmacenId;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class ProductoAlmacenService {
    private final ProductoAlmacenRepository productoAlmacenRepository;
    private final AlmacenService almacenService;
    private final ProductoService productoService;
    private final AlmacenRepository almacenRepository;
    private final ProductoRepository productoRepository;

    @Transactional
    public void save(@NonNull ProductoAlmacenRequest request) {
        Almacen almacen = almacenRepository.findById(request.getIdAlmacen())
                .orElseThrow(() -> new EntityNotFoundException("Almacén no encontrado: " + request.getIdAlmacen()));
        Producto producto = productoRepository.findById(request.getIdProducto())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado: " + request.getIdProducto()));

        log.info("ALMACEN" + almacen.getNombre());
        log.info("PRODUCTO" + producto.getNombre());

        ProductoAlmacenId id = new ProductoAlmacenId();
        id.setIdAlmacen(request.getIdAlmacen());
        id.setIdProducto(request.getIdProducto());

        ProductoAlmacen productoAlmacen = new ProductoAlmacen();
        productoAlmacen.setId(id);
        productoAlmacen.setAlmacen(almacen);   // ← asociar la entidad
        productoAlmacen.setProducto(producto); // ← asociar la entidad
        productoAlmacen.setStock(request.getStock());
        productoAlmacen.setMax(request.getMax());
        productoAlmacen.setMin(request.getMin());
        productoAlmacen.setActivo(request.getActivo());

        productoAlmacenRepository.save(productoAlmacen);
    }

    @Transactional(readOnly = true)
    public List<ProductoAlmacenDTO> findAll() {
        return productoAlmacenRepository.findAllDTO();
    }

    @Transactional(readOnly = true)
    public List<ProductoAlmacenDTO> findByAlmacen(UUID idAlmacen) {
        return productoAlmacenRepository.findByAlmacenDTO(idAlmacen);
    }

    @Transactional(readOnly = true)
    public List<ProductoAlmacenDTO> findBySku(String sku){
        return productoAlmacenRepository.findAllByProductoSku(sku);
    }

    @Transactional(readOnly = true)
    public Optional<ProductoAlmacen> findById(ProductoAlmacenId id) {
        return productoAlmacenRepository.findById(id);
    }

    @Transactional
    public Optional<ProductoAlmacenDTO> update(ProductoAlmacenId id, ProductoAlmacenRequest dto) {
        return productoAlmacenRepository.findById(id).map(pa -> {
            if (dto.getStock() != null)  pa.setStock(dto.getStock());
            if (dto.getMax() != null)    pa.setMax(dto.getMax());
            if (dto.getMin() != null)    pa.setMin(dto.getMin());
            if (dto.getActivo() != null) pa.setActivo(dto.getActivo());
            return new ProductoAlmacenDTO(productoAlmacenRepository.save(pa));
        });
    }

    @Transactional
    public boolean delete(ProductoAlmacenId id) {
        if (!productoAlmacenRepository.existsById(id)) return false;
        productoAlmacenRepository.deleteById(id);
        return true;
    }
}
