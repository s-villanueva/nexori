package com.example.B2BProyect.repository;

import com.example.B2BProyect.repository.dto.response.OrdenCompraDTO;
import com.example.B2BProyect.repository.dto.response.OrdenEmpresaStats;
import com.example.B2BProyect.repository.entity.OrdenCompra;
import com.example.B2BProyect.repository.proyecciones.OrdenCompraProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, UUID> {
    @Query("SELECT new " +
            "com.example.B2BProyect.repository.dto.response.OrdenCompraDTO(" +
            "o.id, o.total, o.fecha, o.fechaOrden, o.idEstado, " +
            "o.idProveedor.idEmpresa.nombre, o.idEmpresaCompradora.nombre, " +
            "o.idSucursal.nombre, o.idUsuario.nombre) " +
            "FROM OrdenCompra o WHERE o.idProveedor.idEmpresa.nombre = :pNombre")
    List<OrdenCompraDTO> findByProveedorDTO(@Param("pNombre") String pNombre);

    @Query("SELECT new " +
            "com.example.B2BProyect.repository.dto.response.OrdenCompraDTO(" +
            "o.id, o.total, o.fecha, o.fechaOrden, o.idEstado, " +
            "o.idProveedor.idEmpresa.nombre, o.idEmpresaCompradora.nombre, " +
            "o.idSucursal.nombre, o.idUsuario.nombre) " +
            "FROM OrdenCompra o WHERE o.idEmpresaCompradora.nombre = :pNombre")
    List<OrdenCompraDTO> findByEmpresaDTO(@Param("pNombre") String pNombre);

    @Query("SELECT new" +
            " com.example.B2BProyect.repository.dto.response.OrdenCompraDTO(" +
            "o.id, o.total, o.fecha, o.fechaOrden, o.idEstado," +
            " o.idProveedor.idEmpresa.nombre, o.idEmpresaCompradora.nombre," +
            " o.idSucursal.nombre, o.idUsuario.nombre)" +
            " FROM OrdenCompra o")
    List<OrdenCompraDTO> findAllDTO();

    @Query("SELECT new " +
            "com.example.B2BProyect.repository.dto.response.OrdenCompraDTO(" +
            "o.id, o.total, o.fecha, o.fechaOrden, o.idEstado, " +
            "o.idProveedor.idEmpresa.nombre, o.idEmpresaCompradora.nombre, " +
            "o.idSucursal.nombre, o.idUsuario.nombre)" +
            " FROM OrdenCompra o WHERE o.id = :pId")
    Optional<OrdenCompraDTO> findByIdDTO(@Param("pId") UUID pId);

    // proyección para listar resumen de órdenes
    @Query("SELECT o.id AS id, o.fecha AS fecha, o.idEstado AS idEstado, o.total AS total FROM OrdenCompra o")
    List<OrdenCompraProjection> findResumenOrdenes();

    @Query("SELECT new com.example.B2BProyect.repository.dto.response.OrdenCompraDTO(" +
            "o.id, o.total, o.fecha, o.fechaOrden, o.idEstado, " +
            "o.idProveedor.idEmpresa.nombre, o.idEmpresaCompradora.nombre, " +
            "o.idSucursal.nombre, o.idUsuario.nombre) " +
            "FROM OrdenCompra o WHERE o.createdDate BETWEEN :pInit AND :pEnd")
    Page<OrdenCompraDTO> findAllByOrderByDateDesc(
            @Param("pInit") LocalDateTime pInit,
            @Param("pEnd") LocalDateTime pEnd,
            Pageable pageable);

    @Query("""
SELECT new com.example.B2BProyect.repository.dto.response.OrdenEmpresaStats(
    COUNT(oc),
    SUM(CASE WHEN oc.idEstado = 'PENDIENTE' THEN 1 ELSE 0 END),
    CAST(COALESCE(SUM(
        CASE 
            WHEN EXTRACT(MONTH FROM oc.fecha) = EXTRACT(MONTH FROM CURRENT_DATE)
             AND EXTRACT(YEAR FROM oc.fechaOrden) = EXTRACT(YEAR FROM CURRENT_DATE)
            THEN oc.total
            ELSE 0.0
        END
    ), 0.0) AS bigdecimal)
)
FROM OrdenCompra oc
WHERE oc.idEmpresaCompradora.id=:pId
""")
    OrdenEmpresaStats obtenerStats(UUID pId);

    @Query("SELECT new com.example.B2BProyect.repository.dto.response.OrdenCompraDTO( o.id, o.total, o.fecha, o.fechaOrden, o.idEstado, o.idProveedor.idEmpresa.nombre, o.idEmpresaCompradora.nombre, o.idSucursal.nombre, o.idUsuario.nombre)" +
            " FROM OrdenCompra o WHERE o.idEmpresaCompradora.id=:pIdEmpresa")
    Page<OrdenCompraDTO> retrieveAllFromEmpresaCompradora(@Param("pIdEmpresa") UUID pIdEmpresa, Pageable pageable);

    @Query("SELECT new com.example.B2BProyect.repository.dto.response.OrdenCompraDTO( o.id, o.total, o.fecha, o.fechaOrden, o.idEstado, o.idProveedor.idEmpresa.nombre, o.idEmpresaCompradora.nombre, o.idSucursal.nombre, o.idUsuario.nombre)" +
            " FROM OrdenCompra o WHERE o.idProveedor.idEmpresa.id=:pIdEmpresa")
    Page<OrdenCompraDTO> retrieveAllFromEmpresaProveedora(@Param("pIdEmpresa") UUID pIdEmpresa, Pageable pageable);
}