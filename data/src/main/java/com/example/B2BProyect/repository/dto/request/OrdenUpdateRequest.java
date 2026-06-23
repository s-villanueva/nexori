package com.example.B2BProyect.repository.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class OrdenUpdateRequest {
    private UUID id;
    private String idEstado;
}
