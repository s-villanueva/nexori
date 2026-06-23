package com.example.B2BProyect.repository;

import com.example.B2BProyect.repository.dto.response.FacturaDTO;
import com.example.B2BProyect.repository.dto.response.FacturaEmpresaStats;
import com.example.B2BProyect.repository.entity.Factura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FacturaRepository extends JpaRepository<Factura, UUID> {
    @Query("SELECT new " +
            "com.example.B2BProyect.repository.dto.response.FacturaDTO(" +
            "f.id, f.fecha, f.total, f.idEstado, f.idOrden) " +
            "FROM Factura f WHERE f.idOrden.id = :pIdOrden")
    List<FacturaDTO> findByOrdenDTO(@Param("pIdOrden") UUID pIdOrden);

    @Query("SELECT new" +
            " com.example.B2BProyect.repository.dto.response.FacturaDTO(f.id, f.fecha, f.total, f.idEstado, f.idOrden)" +
            " FROM Factura f")
    List<FacturaDTO> findAllDTO();

    @Query("SELECT new " +
            "com.example.B2BProyect.repository.dto.response.FacturaDTO(f.id, f.fecha, f.total, f.idEstado, f.idOrden)" +
            " FROM Factura f WHERE f.id = :pId")
    Optional<FacturaDTO> findByIdDTO(@Param("pId") UUID pId);

    @Query("SELECT new com.example.B2BProyect.repository.dto.response.FacturaDTO(" +
            "f.id, f.fecha, f.total, f.idEstado, f.idOrden)" +
            " FROM Factura f WHERE f.createdDate BETWEEN :pInit AND :pEnd")
    Page<FacturaDTO> findAllByOrderByDateDesc(
            @Param("pInit") LocalDateTime pInit,
            @Param("pEnd") LocalDateTime pEnd,
            Pageable pageable);

    @Query("SELECT new com.example.B2BProyect.repository.dto.response.FacturaEmpresaStats(" +
            "  SUM(CASE WHEN f.idEstado = 'pendiente' THEN f.total ELSE 0 END), " +
            "  SUM(CASE WHEN f.modifiedDate = CURRENT_DATE AND f.idEstado = 'pagado' THEN f.total ELSE 0 END)" +
            ") " +
            "FROM Factura f " +
            "WHERE f.idOrden.idEmpresaCompradora.id=:idEmpresa")
    FacturaEmpresaStats getStatsByEmpresa(
            @Param("idEmpresa") UUID idEmpresa
    );

    @Query("select new com.example.B2BProyect.repository.dto.response.FacturaDTO(" +
            "f.id, f.fecha, f.total, f.idEstado, f.idOrden) from Factura f WHERE f.idOrden.idEmpresaCompradora.id=:pIdEmpresa")
    Page<FacturaDTO> findAllForEmpresa(@Param("pIdEmpresa") UUID pIdEmpresa, Pageable pageable);
}

