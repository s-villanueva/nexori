package com.example.ExamenUno.controller;

import com.example.B2BProyect.repository.entity.Estudiante;
import com.example.B2BProyect.service.EstudianteService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/estudiante")
public class EstudianteController {
    private final EstudianteService estudianteService;
    @GetMapping
    public List<Estudiante> getEstudiantes(){
        return estudianteService.loadAllEstudiantes();
    }
}
