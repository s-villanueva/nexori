package com.example.B2BProyect.service;

import com.example.B2BProyect.repository.EstudianteRepository;
import com.example.B2BProyect.repository.dto.response.EstudianteDTO;
import com.example.B2BProyect.repository.entity.Estudiante;
import com.example.B2BProyect.repository.entity.Materia;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EstudianteService {
    //CREAR. LISTAR Y MODIFICAR
    private final EstudianteRepository estudianteRepository;
    private final MateriaService materiaService;

    @Transactional(readOnly = true)
    public List<Estudiante> loadAllEstudiantes(){
        return estudianteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Estudiante loadEstudianteById(UUID id){
        return estudianteRepository.findById(id).orElse(null);
    }

    @Transactional
    public void saveEstudiante(EstudianteDTO estudiante){
        Estudiante saver = new Estudiante();
        saver.setNombre(estudiante.getNombre());
        saver.setApellido(estudiante.getApellido());
        saver.setNota(estudiante.getNota());
        saver.setNroTelefono(estudiante.getNroTelefono());
        saver.setNroDocumento(estudiante.getNroDocumento());
        saver.setMateria(materiaService.loadById(estudiante.getMateria()));
        estudianteRepository.save(saver);
    }

    @Transactional
    public void updateEstudiante(EstudianteDTO estudiante){
        Estudiante oldEstudiante =  loadEstudianteById(estudiante.getId());
        if (oldEstudiante == null){
            throw new EntityNotFoundException("Estudiante no encontrado");
        }
        if (!oldEstudiante.getNombre().equals(estudiante.getNombre())){
            oldEstudiante.setNombre(estudiante.getNombre());
        }
        if (!oldEstudiante.getApellido().equals(estudiante.getApellido())){
            oldEstudiante.setNombre(estudiante.getNombre());
        }
        Materia oldMateria = materiaService.loadById(estudiante.getMateria());
        Materia currentMateria = materiaService.loadById(estudiante.getMateria());

        if (oldMateria == null || currentMateria == null){
            throw new EntityNotFoundException("Materia no encontrada");
        }

        if (oldMateria != currentMateria){
            oldEstudiante.setMateria(currentMateria);
        }

        if (!oldEstudiante.getNota().equals(estudiante.getNota())){
            oldEstudiante.setNombre(estudiante.getNombre());
        }
        if (!oldEstudiante.getNroTelefono().equals(estudiante.getNroTelefono())){
            oldEstudiante.setNombre(estudiante.getNombre());
        }
        if (!oldEstudiante.getNroDocumento().equals(estudiante.getNroDocumento())){
            oldEstudiante.setNombre(estudiante.getNombre());
        }
    }
}
