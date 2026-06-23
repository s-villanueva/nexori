package com.example.B2BProyect.repository.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class AlmacenRequest {
    private String nombre;
    private String direccion;
    private String coordenadas;
    private Boolean activo;
    @JsonProperty("id_empresa")
    private UUID idEmpresa;
}
