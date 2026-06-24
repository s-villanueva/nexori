package com.example.B2BProyect.controller;

import com.example.B2BProyect.integracion.*;
import com.example.B2BProyect.integracion.stereum.PaymentRequest;
import com.example.B2BProyect.integracion.stereum.StereumPayResponse;
import com.example.B2BProyect.repository.FacturaRepository;
import com.example.B2BProyect.repository.entity.Factura;
import com.example.B2BProyect.repository.entity.OrdenCompra;
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

    private ConcurrentHashMap<UUID, String> clientes = new ConcurrentHashMap<>();
    @Value("${stereum.api.key}")
    private String secretKey;

    private final SistemaB2B sistemaB2B;
    private final OrdenCompraService ordenCompraService;
    @Autowired
    private FacturaRepository facturaRepository;

    @PostMapping("/charge")
    public ResponseEntity<?> charge(@RequestBody PaymentRequest request) {
//        log.info("REQUEST NAME: " + request.getOrderId());
        Factura ordenCompra = facturaRepository.findById(request.getOrderId()).get();
        OrdenCompra oc = ordenCompra.getIdOrden();
        try {
            JSONObject customer = new JSONObject();
            customer.put("name", ordenCompra.getIdOrden().getIdUsuario().getNombre().split(" ")[0]);
            customer.put("lastname", oc.getIdUsuario().getNombre().split(" ")[1]);
            customer.put("document_number", "76887344");

            JSONObject req = new JSONObject();
            req.put("country", "BO");
            req.put("amount", ordenCompra.getTotal());
            req.put("currency", "BOB");
            req.put("network", "CSL");
            req.put("charge_reason", "COMPRA A: " + oc.getIdProveedor().getIdEmpresa().getNombre());
            req.put("idempotency_key", ordenCompra.getId().toString());
            req.put("reservation_validity_time", "10");
            req.put("customer", customer);
            req.put("account_id", "a3ec8479-52f5-4fe5-855b-934b89c126f6");

//            Usuario user = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//            clientes.put(request.getOrderId(), user.getNombre());
            log.info(req.toString());
            return ok(sistemaB2B.callStereum(req));
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

        final ObjectMapper mapper = new ObjectMapper();

        String hmac = new HmacUtils("HmacSHA256",
                secretKey.getBytes(StandardCharsets.UTF_8))
                .hmacHex(body.getBytes(StandardCharsets.UTF_8));

        if (!signature.equals(hmac)) {
            log.info("X-Signature recibido: {}", signature);
            throw new Exception("MessageCode.SIGN_REQUEST_INVALID");
        }
//
//        if (System.currentTimeMillis() / 1000 - tiempo >= 3000)
//            throw new Exception("MessageCode.TIEMPO_INVALIDO");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        StereumPayResponse response = objectMapper.readValue(body, StereumPayResponse.class);
        Factura factura = facturaRepository.findByIdOrdenId(response.getTransaction().getIdempotencyKey());
        factura.setIdEstado("pagada");
        factura.setModifiedDate( LocalDateTime.ofInstant( Instant.ofEpochMilli(response.getTimestamp()), ZoneId.systemDefault()));
        facturaRepository.save(factura);
//        template.convertAndSend("/paymenting/" + response.getTransaction().getIdempotencyKey(), response.getTransaction().getStatus());
        return ok().build();
    }
//    @PostMapping(value = "/outbound", produces = MediaType.APPLICATION_JSON_VALUE, consumes = {MediaType.APPLICATION_JSON_VALUE})
//    public ResponseEntity<Void> outbound(
//            @RequestHeader("X-Signature") String signature,
//            @RequestHeader("X-Timestamp") int tiempo,
//            @RequestBody String body) throws Exception {
//
//        return ResponseEntity.ok().build();
//    }

}