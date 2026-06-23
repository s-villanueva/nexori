package com.example.B2BProyect.repository.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ContratoEmpresaStats {
    private Long contratosTotales;
    private BigDecimal descuentoPromedio;
    private Long vencimientosCercanos;
}
