package com.example.B2BProyect.repository.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrdenEmpresaStats {
    private Long ordenesTotales;
    private Long pendientes;
    private BigDecimal gastoMensual;
}