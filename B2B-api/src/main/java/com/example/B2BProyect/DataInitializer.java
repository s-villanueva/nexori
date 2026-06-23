package com.example.B2BProyect;

import com.example.B2BProyect.job.EmailSenderJob;
import com.example.B2BProyect.quartz.CronExpressionConstant;
import com.example.B2BProyect.quartz.service.JobDto;
import com.example.B2BProyect.quartz.service.JobService;
import com.example.B2BProyect.quartz.service.JobUtil;
import com.example.B2BProyect.repository.EmpresaRepository;
import com.example.B2BProyect.repository.ProveedorRepository;
import com.example.B2BProyect.repository.RolUsuarioRepository;
import com.example.B2BProyect.repository.SucursalEmpresaRepository;
import com.example.B2BProyect.repository.UsuarioRepository;
import com.example.B2BProyect.repository.entity.DetalleOrden;
import com.example.B2BProyect.repository.entity.Empresa;
import com.example.B2BProyect.repository.entity.Factura;
import com.example.B2BProyect.repository.entity.OrdenCompra;
import com.example.B2BProyect.repository.entity.Producto;
import com.example.B2BProyect.repository.entity.Proveedor;
import com.example.B2BProyect.repository.entity.RolUsuario;
import com.example.B2BProyect.repository.entity.SucursalEmpresa;
import com.example.B2BProyect.repository.entity.Usuario;
import com.example.B2BProyect.service.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RolUsuarioRepository rolUsuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ProveedorRepository proveedorRepository;
    private final SucursalEmpresaRepository sucursalEmpresaRepository;
    private final UsuarioRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JobService jobService;

    @Override
    public void run(String... args) throws Exception {
        init();

//        JobDto jobDto = EmailSenderJob.getJobDto(JobUtil.GROUP_NAME);
//        if (!jobService.existJobName(jobDto.getGroupName(), jobDto.getJobName())){
//            jobService.scheduleCronJob(jobDto, new Date(), CronExpressionConstant.CRON_START_NOW, null, "Este Job envia correos");
//        }

//        emailService.sendPassword("santiagovillanueva1@upb.edu","123546");
        Factura factura = new Factura();

        Empresa empresa = new Empresa();
        empresa.setNombre("Industrias Andinas S.A.");
        empresa.setNit("1029384756");
        empresa.setRazonSocial("Industrias Andinas Sociedad Anonima");

        Producto producto = new Producto();
        producto.setNombre("Monitor LED 24 pulgadas");
        producto.setSku("MON-LED-24");

        DetalleOrden detalle = new DetalleOrden();
        detalle.setCantidad(3);
        detalle.setPrecioUnitario(new BigDecimal("425.50"));
        detalle.setSubtotal(new BigDecimal("1276.50"));
        detalle.setIdProducto(producto);

        OrdenCompra orden = new OrdenCompra();
        orden.setId(UUID.fromString("8f2a6d0c-7b91-4d3e-9c41-2f63c1a8e902"));
        orden.setTotal(new BigDecimal("1276.50"));
        orden.setFecha(Instant.parse("2026-06-18T15:42:00Z"));
        orden.setIdEstado("pagada");
        orden.setIdEmpresaCompradora(empresa);
        orden.setDetalleOrdens(new LinkedHashSet<>(List.of(detalle)));

        detalle.setIdOrden(orden);

        factura.setId(UUID.fromString("3c1d8a5f-6e27-4f83-b9a0-91d7b2e4c620"));
        factura.setFecha(Instant.parse("2026-06-18T15:45:00Z"));
        factura.setTotal(new BigDecimal("1276.50"));
        factura.setIdEstado("pagada");
        factura.setIdOrden(orden);

//        emailService.sendFactura("santiagovillanueva1@upb.edu", factura);
    }

    private void init() {
        RolUsuario rolAdmin = ensureRole("admin", "Administrador del sistema");
        RolUsuario rolProveedor = ensureRole("proveedor", "Usuario proveedor");
        RolUsuario rolEmpresa = ensureRole("empresa", "Usuario empresa compradora");

        Empresa empresaAdmin = ensureEmpresa(
                "B2B Admin Demo",
                "0000000000",
                "B2B Admin Demo",
                "admin.demo");
        SucursalEmpresa sucursalAdmin = ensureSucursal(
                empresaAdmin,
                "Sucursal Admin Demo",
                "Av. Principal 100");
        ensureUser("Admin Demo", "admin@b2b.test", "Abc123**", rolAdmin, empresaAdmin, sucursalAdmin);
        ensureUser("root", "root@upb.com", "Abc123**", rolAdmin, empresaAdmin, sucursalAdmin);

        Empresa empresaProveedor = ensureEmpresa(
                "Proveedor Demo SRL",
                "1234567890",
                "Proveedor Demo Sociedad de Responsabilidad Limitada",
                "proveedor.demo");
        SucursalEmpresa sucursalProveedor = ensureSucursal(
                empresaProveedor,
                "Sucursal Proveedor Demo",
                "Parque Industrial, Modulo 12");
        ensureProveedor(empresaProveedor);
        ensureUser("Proveedor Demo", "proveedor@b2b.test", "Abc123**", rolProveedor, empresaProveedor, sucursalProveedor);

        Empresa empresaCompradora = ensureEmpresa(
                "Empresa Compradora Demo SA",
                "9876543210",
                "Empresa Compradora Demo Sociedad Anonima",
                "compradora.demo");
        SucursalEmpresa sucursalEmpresa = ensureSucursal(
                empresaCompradora,
                "Sucursal Empresa Demo",
                "Av. Comercio 245");
        ensureUser("Empresa Demo", "empresa@b2b.test", "Abc123**", rolEmpresa, empresaCompradora, sucursalEmpresa);
    }

    private RolUsuario ensureRole(String nombre, String descripcion) {
        return rolUsuarioRepository.findAll()
                .stream()
                .filter(rol -> rol.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseGet(() -> {
                    RolUsuario rol = new RolUsuario();
                    rol.setNombre(nombre);
                    rol.setDescripcion(descripcion);
                    return rolUsuarioRepository.save(rol);
                });
    }

    private Empresa ensureEmpresa(String nombre, String nit, String razonSocial, String dominio) {
        return empresaRepository.findAll()
                .stream()
                .filter(empresa -> empresa.getNombre().equals(nombre))
                .findFirst()
                .orElseGet(() -> {
                    Empresa empresa = new Empresa();
                    empresa.setNombre(nombre);
                    empresa.setNit(nit);
                    empresa.setRazonSocial(razonSocial);
                    empresa.setDominio(dominio);
                    empresa.setActivo(true);
                    return empresaRepository.save(empresa);
                });
    }

    private SucursalEmpresa ensureSucursal(Empresa empresa, String nombre, String direccion) {
        return sucursalEmpresaRepository.findAll()
                .stream()
                .filter(sucursal -> sucursal.getNombre().equals(nombre)
                        && sucursal.getIdEmpresa().getId().equals(empresa.getId()))
                .findFirst()
                .orElseGet(() -> {
                    SucursalEmpresa sucursal = new SucursalEmpresa();
                    sucursal.setNombre(nombre);
                    sucursal.setDireccion(direccion);
                    sucursal.setActivo(true);
                    sucursal.setIdEmpresa(empresa);
                    return sucursalEmpresaRepository.save(sucursal);
                });
    }

    private Proveedor ensureProveedor(Empresa empresa) {
        return proveedorRepository.findAll()
                .stream()
                .filter(proveedor -> proveedor.getIdEmpresa().getId().equals(empresa.getId()))
                .findFirst()
                .orElseGet(() -> {
                    Proveedor proveedor = new Proveedor();
                    proveedor.setActivo(true);
                    proveedor.setIdEmpresa(empresa);
                    return proveedorRepository.save(proveedor);
                });
    }

    private Usuario ensureUser(String nombre, String email, String password, RolUsuario rol, Empresa empresa, SucursalEmpresa sucursal) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(Usuario.builder()
                        .nombre(nombre)
                        .email(email)
                        .idRol(rol)
                        .activo(Boolean.TRUE)
                        .passwordHash(passwordEncoder.encode(password))
                        .idEmpresa(empresa)
                        .idSucursal(sucursal)
                        .build()));
    }
}
