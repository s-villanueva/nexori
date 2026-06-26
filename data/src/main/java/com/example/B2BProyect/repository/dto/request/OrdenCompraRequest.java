package com.example.B2BProyect.repository.dto.request;

import com.example.B2BProyect.repository.entity.DetalleOrden;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrdenCompraRequest {
    private BigDecimal total;
    private Instant fecha;
    private LocalDate fechaOrden;
    private String idEstado;
    private List<DetalleOrdenRequest> detalles;
    private UUID idProveedor;
    private UUID idUsuario;
    private Integer version;
}
