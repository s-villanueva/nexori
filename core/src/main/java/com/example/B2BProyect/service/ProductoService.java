package com.example.B2BProyect.service;

import com.example.B2BProyect.repository.PrecioBaseRepository;
import com.example.B2BProyect.repository.ProductoRepository;
import com.example.B2BProyect.repository.dto.request.PrecioBaseRequest;
import com.example.B2BProyect.repository.dto.request.ProductoRequest;
import com.example.B2BProyect.repository.dto.response.ProductoDTO;
import com.example.B2BProyect.repository.entity.*;
import com.example.B2BProyect.repository.proyecciones.ProductoCsvRepresentation;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.lang.model.element.PackageElement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;
    private final PrecioBaseRepository precioBaseRepository;
    private final ProveedorService proveedorService;
    private final EmpresaService empresaService;

//    @CacheEvict(cacheNames = "productos", allEntries = true)
    @Transactional
    public void save(ProductoRequest request) {
        Producto pr = fromReqToProduct(request);
        PrecioBase precioBase = new PrecioBase();
        precioBase.setVigenteDesde(LocalDateTime.now().toInstant(ZoneOffset.UTC));
        precioBase.setPrecioBase(request.getPrecioBase());
        precioBase.setIdProducto(pr);
        precioBase.setIdProveedor(pr.getIdProveedor());
        precioBaseRepository.save(precioBase);
    }

    public Producto fromReqToProduct(ProductoRequest request){
        Producto producto = new Producto();
        producto.setSku(request.getSku());
        producto.setNombre(request.getNombre());
        System.out.println("REQUEST: " + request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setUnidadMedida(request.getUnidadMedida());
        producto.setActivo(request.getActivo());
        producto.setCreatedBy(request.getCreatedBy());
        producto.setCreatedDate(LocalDateTime.now());
        Proveedor proveedor = proveedorService.findByIdEmpresa(request.getIdEmpresa());
        if (request.getIdCategoria() != null)
            categoriaService.findById(request.getIdCategoria()).ifPresent(producto::setIdCategoria);
        if (request.getIdEmpresa() != null)
            producto.setIdProveedor(proveedor);
        return productoRepository.save(producto);
    }

    @Transactional
    public Integer manageCsvFile(MultipartFile file, UUID id){
        Empresa empresa = empresaService.findById(id).orElseThrow();
        Set<ProductoRequest> productos = parseCsv(file);
        productos.forEach(productoRequest -> { productoRequest.setCreatedBy(empresa.getNombre());
            productoRequest.setIdEmpresa(empresa.getId()); });
        List<Producto> prods = prepareData(productos);
        productoRepository.saveAll(prods);
        return prods.size();
    }

    @Async("taskLog")
    public List<Producto> prepareData(Set<ProductoRequest> requests){
        List<Producto> productoList = new ArrayList<>();
        requests.forEach(request -> {
            Producto pr = fromReqToProduct(request);
            PrecioBase precioBase = new PrecioBase();
            precioBase.setVigenteDesde(LocalDateTime.now().toInstant(ZoneOffset.UTC));
            precioBase.setPrecioBase(request.getPrecioBase());
            precioBase.setIdProducto(pr);
            precioBase.setIdProveedor(pr.getIdProveedor());
            productoList.add(pr);
            precioBaseRepository.save(precioBase);
        });
        return productoList;
    }

    private Set<ProductoRequest> parseCsv(MultipartFile file) {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            HeaderColumnNameMappingStrategy<ProductoCsvRepresentation> strategy =
                    new HeaderColumnNameMappingStrategy<>();
            strategy.setType(ProductoCsvRepresentation.class);
            CsvToBean<ProductoCsvRepresentation> csvToBean =
                    new CsvToBeanBuilder<ProductoCsvRepresentation>(reader)
                            .withMappingStrategy(strategy)
                            .withIgnoreEmptyLine(true)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build();
            return csvToBean.parse().stream().map(csvLine ->
                    ProductoRequest.builder()
                            .sku(csvLine.getSku())
                            .nombre(csvLine.getNombre())
                            .descripcion(csvLine.getDescripcion())
                            .unidadMedida(csvLine.getUnidadMedida())
                            .activo(true)
                            .precioBase(csvLine.getPrecioBase())
                            .build()
            ).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    @Cacheable(cacheNames = "productos")
    @Transactional(readOnly = true)
    public List<ProductoDTO> findAll() {
        return productoRepository.findAll().stream().map(ProductoDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> findAllPaged(int page, int size) {
        return productoRepository.findAllPaged(PageRequest.of(page, size));
    }
    @Transactional(readOnly = true)
    public Page<ProductoDTO> findAllOfProvider(UUID idEmpresa, int page, int size){
        Page<ProductoDTO> productoDTOS = productoRepository.findAllByEmpresa(idEmpresa, PageRequest.of(page,size));
        return productoDTOS;
    }

    @Transactional(readOnly = true)
    public Optional<Producto> findById(UUID id) {
        return productoRepository.findById(id);
    }

    @CacheEvict(cacheNames = "productos", allEntries = true)
    @Transactional
    public Optional<ProductoDTO> update(UUID id, ProductoRequest dto) {
        return productoRepository.findById(id).map(producto -> {
            if (dto.getSku() != null)          producto.setSku(dto.getSku());
            if (dto.getNombre() != null)       producto.setNombre(dto.getNombre());
            if (dto.getDescripcion() != null)  producto.setDescripcion(dto.getDescripcion());
            if (dto.getUnidadMedida() != null) producto.setUnidadMedida(dto.getUnidadMedida());
            if (dto.getActivo() != null)       producto.setActivo(dto.getActivo());
            if (dto.getIdCategoria() != null)
                categoriaService.findById(dto.getIdCategoria()).ifPresent(producto::setIdCategoria);
            if (dto.getIdEmpresa() != null)
                producto.setIdProveedor(proveedorService.findByIdEmpresa(dto.getIdEmpresa()));
            return new ProductoDTO(productoRepository.save(producto));
        });
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> findByProveedor(UUID idProveedor) {
        return productoRepository.findByIdProveedorId(idProveedor)
                .stream().map(ProductoDTO::new).toList();
    }

    @CacheEvict(cacheNames = "productos", allEntries = true)
    @Transactional
    public boolean delete(UUID id) {
        if (!productoRepository.existsById(id)) return false;
        productoRepository.deleteById(id);
        return true;
    }
}
