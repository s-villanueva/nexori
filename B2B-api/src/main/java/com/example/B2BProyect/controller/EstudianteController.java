package com.example.B2BProyect.controller;

import com.example.B2BProyect.repository.dto.response.EstudianteDTO;
import com.example.B2BProyect.repository.entity.Estudiante;
import com.example.B2BProyect.service.EstudianteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/estudiante")
public class EstudianteController {
    private final EstudianteService estudianteService;
    @GetMapping
    public ResponseEntity<List<Estudiante>> getEstudiantes(){
        try{
            return ResponseEntity.ok(estudianteService.loadAllEstudiantes());
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/guardar")
    public ResponseEntity<Void> saveByDTO(@RequestBody EstudianteDTO estudiante){
        try {
            estudianteService.saveEstudiante(estudiante);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    @PostMapping("/cambiar")
    public ResponseEntity<Void> updateByDTO(@RequestBody EstudianteDTO estudiante){
        try{
            estudianteService.updateEstudiante(estudiante);
            return ResponseEntity.ok().build();
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
