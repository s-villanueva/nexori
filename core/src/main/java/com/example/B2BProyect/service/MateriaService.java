package com.example.B2BProyect.service;

import com.example.B2BProyect.repository.MateriaRepository;
import com.example.B2BProyect.repository.entity.Materia;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class MateriaService {
    private final MateriaRepository MateriaRepository;
    //CREAR, LISTAR Y MODIFICAR

    public List<Materia> loadAll() {
        return MateriaRepository.findAll();
    }

    public Materia loadById(UUID id){
        return MateriaRepository.findById(id).orElse(null);
    }

    public void saveMateria(Materia materia){
        MateriaRepository.save(materia);
    }

    public void updateMateria(Materia materia){
        Materia oldMateria = MateriaRepository.findById(materia.getId()).orElse(null);
        if (oldMateria == null){
            throw new EntityNotFoundException("Materia no encontrada");
        }
        if (!oldMateria.getNombre().equals(materia.getNombre())){
            oldMateria.setNombre(materia.getNombre());
        }
        if (!oldMateria.getSigla().equals(materia.getSigla())){
            oldMateria.setSigla(materia.getSigla());
        }

        MateriaRepository.save(materia);
    }
}
