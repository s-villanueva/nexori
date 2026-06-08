package com.example.B2BProyect.controller;

import com.example.B2BProyect.service.integration.SistemaA;
import com.example.B2BProyect.service.integration.Sistema1AuthRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/api/v1/sistema-a")
public class SistemaAController {

    private final SistemaA sistemaA;

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth() {
        try {
            JSONObject result = sistemaA.auth(
                    new Sistema1AuthRequest("root@upb.com", "Abc123**")
            );
            log.info("SistemaA auth test result: {}", result);
            return ResponseEntity.ok(result.toString(2));
        } catch (Exception e) {
            log.error("SistemaA auth test failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
