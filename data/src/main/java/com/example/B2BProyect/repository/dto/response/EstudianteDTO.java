package com.example.B2BProyect.repository.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EstudianteDTO {
    private UUID id;
    private String nombre;
    private String apellido;
    private UUID materia;
    private Integer nota;
    private String nroTelefono;
    private String nroDocumento;
}
