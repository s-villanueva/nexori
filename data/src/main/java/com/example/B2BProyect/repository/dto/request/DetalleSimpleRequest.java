package com.example.B2BProyect.repository.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class DetalleSimpleRequest {
    private UUID idProducto;
    private BigDecimal porcentajeDescuento;
}
