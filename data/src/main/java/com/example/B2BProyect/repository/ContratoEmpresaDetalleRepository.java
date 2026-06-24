package com.example.B2BProyect.repository;

import com.example.B2BProyect.repository.dto.response.ContratoEmpresaDetalleDTO;
import com.example.B2BProyect.repository.dto.response.ContratoEmpresaStats;
import com.example.B2BProyect.repository.entity.ContratoEmpresaDetalle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContratoEmpresaDetalleRepository extends JpaRepository<ContratoEmpresaDetalle, UUID> {
    @Query("SELECT new " +
            "com.example.B2BProyect.repository.dto.response.ContratoEmpresaDetalleDTO(" +
            "cd.id, cd.porcentajeDescuento, cd.idProducto.nombre, cd.idContrato.idRegla.nombre) " +
            "FROM ContratoEmpresaDetalle cd WHERE cd.idContrato.id = :pIdContrato")
    List<ContratoEmpresaDetalleDTO> findByContratoDTO(@Param("pIdContrato") UUID pIdContrato);

    @Query("SELECT new" +
            " com.example.B2BProyect.repository.dto.response.ContratoEmpresaDetalleDTO(" +
            "cd.id, cd.porcentajeDescuento, cd.idProducto.nombre, cd.idContrato.idRegla.nombre)" +
            " FROM ContratoEmpresaDetalle cd")
    List<ContratoEmpresaDetalleDTO> findAllDTO();

    @Query("SELECT new " +
            "com.example.B2BProyect.repository.dto.response.ContratoEmpresaDetalleDTO(" +
            "cd.id, cd.porcentajeDescuento, cd.idProducto.nombre, cd.idContrato.idRegla.nombre)" +
            " FROM ContratoEmpresaDetalle cd WHERE cd.id = :pId")
    Optional<ContratoEmpresaDetalleDTO> findByIdDTO(@Param("pId") UUID pId);

    @Query("SELECT new com.example.B2BProyect.repository.dto.response.ContratoEmpresaStats(" +
            "COUNT(cn), " +
            "CAST(AVG(cn.porcentajeDescuento) AS BigDecimal), " +
            "SUM(CASE WHEN cn.idContrato.vigenteHasta >= CURRENT_DATE THEN 1 ELSE 0 END)) " +
            "FROM ContratoEmpresaDetalle cn " +
            "WHERE cn.idContrato.idEmpresa.id = :pIdEmpresa")
    ContratoEmpresaStats findStatsForEmpresa(@Param("pIdEmpresa") UUID pIdEmpresa);

    @Query("SELECT new " +
            "com.example.B2BProyect.repository.dto.response.ContratoEmpresaDetalleDTO(" +
            "cd.id, cd.porcentajeDescuento, cd.idProducto.nombre, cd.idContrato.idRegla.nombre, cd.idContrato.vigenteDesde, cd.idContrato.vigenteHasta)" +
            " FROM ContratoEmpresaDetalle cd WHERE cd.idContrato.idProveedor.idEmpresa.id=:pIdEmpresa")
    Page<ContratoEmpresaDetalleDTO> findAllByIdContratoIdEmpresaIdProveedor(@Param("pIdEmpresa") UUID pIdEmpresa, Pageable pageable);

    @Query("SELECT new " +
            "com.example.B2BProyect.repository.dto.response.ContratoEmpresaDetalleDTO(" +
            "cd.id, cd.porcentajeDescuento, cd.idProducto.nombre, cd.idContrato.idRegla.nombre, cd.idContrato.vigenteDesde, cd.idContrato.vigenteHasta)" +
            " FROM ContratoEmpresaDetalle cd WHERE cd.idContrato.idEmpresa.id=:pIdEmpresa")
    Page<ContratoEmpresaDetalleDTO> findAllByIdContratoIdEmpresaIdEmpresa(@Param("pIdEmpresa") UUID pIdEmpresa, Pageable pageable);
}