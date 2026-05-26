package com.example.B2BProyect.repository.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "estudiante")
public class Estudiante {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = Integer.MAX_VALUE)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = Integer.MAX_VALUE)
    private String apellido;

    @JsonProperty(namespace = "nro_telefono")
    @Column(name = "nro_telefono", length = Integer.MAX_VALUE)
    private String nroTelefono;

    @JsonProperty(namespace = "nro_documento")
    @Column(name = "nro_documento", length = Integer.MAX_VALUE)
    private String nroDocumento;

    @Column(name = "sigla", length = 10)
    private String sigla;

    @Column(name = "nota")
    private Integer nota;

    @JoinColumn(name = "materia")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Materia materia;

}