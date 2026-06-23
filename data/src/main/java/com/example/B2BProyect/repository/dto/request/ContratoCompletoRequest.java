package com.example.B2BProyect.repository.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ContratoCompletoRequest {
    // Datos de la cabecera (ContratoEmpresaTarifa)
    private UUID idEmpresa;
    private UUID idProveedor;
    private UUID idRegla;
    private Instant vigenteDesde;
    private Instant vigenteHasta;
    private Boolean activo;

    // Listado de detalles (ContratoEmpresaDetalle sin necesidad de idContrato en el JSON de entrada)
    private List<DetalleSimpleRequest> detalles;
}
