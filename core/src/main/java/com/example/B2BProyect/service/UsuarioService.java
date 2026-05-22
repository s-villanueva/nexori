package com.example.B2BProyect.service;

import com.example.B2BProyect.repository.UsuarioRepository;
import com.example.B2BProyect.repository.entity.Usuario;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void save(Usuario usuario) {
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findById(UUID id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    public Optional<Usuario> patch(UUID id, Usuario changes) {
        return usuarioRepository.findById(id).map(usuario -> {
            if (changes.getNombre() != null) usuario.setNombre(changes.getNombre());
            if (changes.getEmail() != null) usuario.setEmail(changes.getEmail());
            if (changes.getPasswordHash() != null) usuario.setPasswordHash(changes.getPasswordHash());
            if (changes.getActivo() != null) usuario.setActivo(changes.getActivo());
            if (changes.getIdEmpresa() != null) usuario.setIdEmpresa(changes.getIdEmpresa());
            if (changes.getIdSucursal() != null) usuario.setIdSucursal(changes.getIdSucursal());
            if (changes.getIdRol() != null) usuario.setIdRol(changes.getIdRol());
            return usuarioRepository.save(usuario);
        });
    }
}
