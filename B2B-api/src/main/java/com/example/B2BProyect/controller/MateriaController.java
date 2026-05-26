package com.example.B2BProyect.controller;

import com.example.B2BProyect.repository.entity.Estudiante;
import com.example.B2BProyect.repository.entity.Materia;
import com.example.B2BProyect.service.MateriaService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/api/v1/materia")
public class MateriaController {
    private MateriaService materiaService;

    @GetMapping
    public ResponseEntity<List<Materia>> getMaterias(){
        try {
            return ResponseEntity.ok(materiaService.loadAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/guardar")
    public ResponseEntity<Void> saveMateria(@RequestBody Materia materia){
        try {
            System.out.println(materia.getNombre());
            materiaService.saveMateria(materia);
            return ResponseEntity.ok().build();
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/update")
    public ResponseEntity<Void> updateMateria(@RequestBody Materia materia){
        try {
            materiaService.updateMateria(materia);
            return ResponseEntity.ok().build();
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
