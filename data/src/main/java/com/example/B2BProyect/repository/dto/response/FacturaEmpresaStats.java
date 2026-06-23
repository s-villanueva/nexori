package com.example.B2BProyect.repository.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor

public class FacturaEmpresaStats {
    private BigDecimal faltaPago;
    private BigDecimal pagadoHoy;
}
