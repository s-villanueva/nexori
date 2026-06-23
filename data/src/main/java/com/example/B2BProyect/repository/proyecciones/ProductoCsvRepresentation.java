package com.example.B2BProyect.repository.proyecciones;

import com.opencsv.bean.CsvBindByName;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductoCsvRepresentation {
    @CsvBindByName(column = "sku")
    private String sku;
    @CsvBindByName(column = "nombre")
    private String nombre;
    @CsvBindByName(column = "descripcion")
    private String descripcion;
    @CsvBindByName(column = "unidad_medida")
    private String unidadMedida;
//    @CsvBindByName(column = "activo")
//    private Boolean activo;
    @CsvBindByName(column = "precio_base")
    private BigDecimal precioBase;
    @CsvBindByName(column = "id_categoria")
    private UUID idCategoria;;
}
