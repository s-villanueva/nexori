package com.example.B2BProyect.repository;

import com.example.B2BProyect.repository.dto.response.DetalleOrdenDTO;
import com.example.B2BProyect.repository.entity.DetalleOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, UUID> {
    @Query("SELECT new com.example.B2BProyect.repository.dto.response.DetalleOrdenDTO" +
            "(d.id, d.cantidad, d.precioUnitario, d.subtotal, d.idProducto.nombre) FROM DetalleOrden d WHERE d.idOrden.id = :pIdOrden")
    List<DetalleOrdenDTO> findByOrden(@Param("pIdOrden") UUID pIdOrden);

    @Query("SELECT d FROM DetalleOrden d WHERE d.idProducto.nombre = :pNombre")
    List<DetalleOrden> findByProducto(@Param("pNombre") String pNombre);

    @Query("SELECT d FROM DetalleOrden d WHERE d.almacen.id = :pIdAlmacen")
    List<DetalleOrden> findByAlmacen(@Param("pIdAlmacen") UUID pIdAlmacen);

    @Query("SELECT d FROM DetalleOrden d WHERE d.almacen.nombre = :pNombre")
    List<DetalleOrden> findByAlmacenNombre(@Param("pNombre") String pNombre);

    Collection<Object> findByIdOrdenId(UUID idOrdenId);
}