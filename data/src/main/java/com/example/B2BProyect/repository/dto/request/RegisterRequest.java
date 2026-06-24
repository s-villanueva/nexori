package com.example.B2BProyect.repository.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.geo.Point;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String nombre;
    private String email;
    private String password;
    private Boolean activo;

    @JsonProperty("id_empresa")
    private UUID idEmpresa;
    @JsonProperty("nombre_empresa")
    private String nombreEmpresa;
    private String dominio;
    private String nit;
    @JsonProperty("razon_social")
    private String razonSocial;

    @JsonProperty("id_sucursal")
    private UUID idSucursal;
    private String nombreSucursal;
    private String direccion;
    private Point coordenadas;

    @JsonProperty("id_rol")
    private UUID idRol;
}
