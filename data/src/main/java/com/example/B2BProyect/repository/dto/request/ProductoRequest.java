package com.example.B2BProyect.repository.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductoRequest {
    private String sku;
    private String nombre;
    private String descripcion;
    private String unidadMedida;
    private String createdBy;
    private Boolean activo;
    private BigDecimal precioBase;
    private UUID idCategoria;
    private UUID idEmpresa;
}
