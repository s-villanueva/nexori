package com.example.ExamenUno.controller;

import com.example.B2BProyect.repository.MateriaRepository;
import com.example.B2BProyect.service.MateriaService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/api/v1/materia")
public class MateriaController {
    @Autowired
    private MateriaService materiaService;
}
