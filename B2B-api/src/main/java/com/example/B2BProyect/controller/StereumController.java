package com.example.B2BProyect.controller;

import com.example.B2BProyect.integracion.*;
import com.example.B2BProyect.integracion.stereum.PaymentRequest;
import com.example.B2BProyect.integracion.stereum.StereumPayResponse;
import com.example.B2BProyect.repository.FacturaRepository;
import com.example.B2BProyect.repository.entity.Factura;
import com.example.B2BProyect.repository.entity.OrdenCompra;
import com.example.B2BProyect.service.FacturaService;
import com.example.B2BProyect.service.OrdenCompraService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/stereum")
public class StereumController {

    private final SistemaB2B sistemaB2B;

    @PostMapping("/charge")
    public ResponseEntity<?> charge(@RequestBody PaymentRequest request) {
        try {
            return ok(sistemaB2B.callStereum(request));
        } catch (Exception e) {
            log.error("Error generando QR Stereum: {}", e.getMessage());
            String msg = e.getMessage() != null ? e.getMessage() : "Error al conectar con Stereum";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
    }

    @Autowired
    private SimpMessagingTemplate template;
    @PostMapping(value = "/outbound", produces = MediaType.APPLICATION_JSON_VALUE, consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> outbound(
            @RequestHeader("X-Signature") String signature,
            @RequestHeader("X-Timestamp") int tiempo,
            @RequestBody String body) throws Exception {
        try{
            sistemaB2B.stereumHandler(body, signature);
            return ok().build();
      } catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}