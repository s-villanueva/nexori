package com.example.B2BProyect.controller;

import com.example.B2BProyect.integracion.*;
import com.example.B2BProyect.repository.dto.request.OrdenCompraRequest;
import com.example.B2BProyect.repository.dto.request.OrdenUpdateRequest;
import com.example.B2BProyect.repository.dto.response.OrdenCompraDTO;
import com.example.B2BProyect.repository.dto.response.OrdenEmpresaStats;
import com.example.B2BProyect.repository.entity.Empresa;
import com.example.B2BProyect.repository.entity.Proveedor;
import com.example.B2BProyect.repository.entity.Usuario;
import com.example.B2BProyect.service.*;
import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/api/v1/ordenes-compra")
public class OrdenCompraController {
    private final OrdenCompraService ordenCompraService;

    @GetMapping("/all")
    public ResponseEntity<List<OrdenCompraDTO>> findAll() {
        try {
            return ResponseEntity.ok(ordenCompraService.findAll());
        } catch (Exception e) {
            log.error("Error listando orden compra: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/supplier")
    public ResponseEntity<Page<OrdenCompraDTO>> findByEmpresaProveedora(@RequestParam String idEmpresa, @RequestParam Integer size, @RequestParam Integer page){
        try{
            return ResponseEntity.ok(ordenCompraService.findByEmpresaProveedora(UUID.fromString(idEmpresa), size, page));
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/buyer")
    public ResponseEntity<Page<OrdenCompraDTO>> findByEmpresaCompradora(@RequestParam String idEmpresa, @RequestParam Integer size, @RequestParam Integer page){
        try{
            return ResponseEntity.ok(ordenCompraService.findByEmpresaCompradora(UUID.fromString(idEmpresa), size, page));
        } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<OrdenEmpresaStats> retrieveStats(
            @RequestParam String idEmpresa){
        try {
            return ResponseEntity.ok(ordenCompraService.retrieveStats(UUID.fromString(idEmpresa)));
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/paged")
    public ResponseEntity<Page<OrdenCompraDTO>> findAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(ordenCompraService.findAllPaged(page, size));
        } catch (Exception e) {
            log.error("Error listando ordenes compra paginadas: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<OrdenCompraDTO> save(@RequestBody OrdenCompraRequest dto) {
        try {
            OrdenCompraDTO created = ordenCompraService.save(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creando orden compra: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/update-status")
    public ResponseEntity<Void> updateStatus(@RequestBody OrdenUpdateRequest ordenUpdateRequest){
        try{
            ordenCompraService.updateStatus(ordenUpdateRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e){
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenCompraDTO> update(@PathVariable UUID id, @RequestBody OrdenCompraRequest dto) {
        try {
            return ordenCompraService.update(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error actualizando orden compra: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        try {
            return ordenCompraService.delete(id)
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error eliminando orden compra: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
