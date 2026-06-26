package com.example.B2BProyect.integracion;

import com.example.B2BProyect.integracion.stereum.PaymentRequest;
import com.example.B2BProyect.integracion.stereum.StereuemApiResponse;
import com.example.B2BProyect.integracion.stereum.StereumPayResponse;
import com.example.B2BProyect.repository.FacturaRepository;
import com.example.B2BProyect.repository.entity.Factura;
import com.example.B2BProyect.repository.entity.OrdenCompra;
import com.example.B2BProyect.service.EmailService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
public class SistemaB2B {

//    @Value("${sistemaB2B.url-base}")
//    private String urlBase;

    @Value("${stereum.api.url}")
    private String stereumUrl;
    @Value("${stereum.api.key}")
    private String secretKey;

//    @Value("${sistemaB2B.connect-timeout:10000}")
//    private int connectTimeout;
//
//    @Value("${bsistemaB2B2b.read-timeout:40000}")
//    private int readTimeout;

    @Value("${stereum.api.key}")
    private String stereumToken;
    @Autowired
    private FacturaRepository facturaRepository;
    private String token;
    @Autowired
    private EmailService emailService;

    public B2BAuthResponse auth(JSONObject request) throws Exception {
        RestClient restClient = create();

        ResponseEntity<B2BAuthResponse> response;
        try {
            response = restClient.post()
                    .uri("http://localhost:8080" + "/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request.toString())
                    .retrieve()
                    .toEntity(B2BAuthResponse.class);
        } catch (Exception e) {
            log.error("Error calling B2B auth. ", e);
            throw e;
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("B2B auth failed with status: {}", response.getStatusCode().value());
            throw new Exception("B2B auth failed");
        }

        log.info("B2B JWT obtained: {}", response.getBody().getAccessToken());
        return response.getBody();
    }

    public StereuemApiResponse callStereum(PaymentRequest request) throws Exception {
        Factura ordenCompra = facturaRepository.findById(request.getOrderId()).get();
        OrdenCompra oc = ordenCompra.getIdOrden();
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
        RestClient restClient = create();

        ResponseEntity<StereuemApiResponse> response;
        try {
            response = restClient.post()
                    .uri(stereumUrl + "/api/v1/transactions/create-charge")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("x-api-key", stereumToken)
                    .body(req.toString())
                    .retrieve()
                    .toEntity(StereuemApiResponse.class);
        } catch (Exception e) {
            log.error("Error calling Stereum API. ", e);
            throw e;
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Stereum API failed with status: {}", response.getStatusCode().value());
            throw new Exception("Stereum API call failed");
        }

        log.info("Stereum response: {}", response.getBody());
        return response.getBody();
    }

    public void stereumHandler(String body, String signature) throws Exception {
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
        Factura factura = facturaRepository.findById(response.getTransaction().getIdempotencyKey()).orElseThrow();
        factura.setIdEstado("pagada");
        factura.setModifiedDate( LocalDateTime.ofInstant( Instant.ofEpochMilli(response.getTimestamp()), ZoneId.systemDefault()));
        facturaRepository.save(factura);
        emailService.sendFactura(factura.getIdOrden().getIdUsuario().getEmail(), factura);
    }

//    public List<B2BEmpresasResponse> listCategorias() throws Exception {
//        RestClient restClient = create();
//        try {
//            return restClient.get()
//                    .uri(urlBase + "/api/v1/empresas")
//                    .accept(MediaType.APPLICATION_JSON)
//                    .header("Authorization", "Bearer " + token)
//                    .retrieve()
//                    .body(new ParameterizedTypeReference<>() {});
//        } catch (Exception e) {
//            log.error("Error calling B2B /empresas. ", e);
//            throw e;
//        }
//    }
//
//    public UsersMeResponse list2() throws Exception {
//        RestClient restClient = create();
//        try {
//            return restClient.get()
//                    .uri(urlBase + "/api/v1/users/me")
//                    .accept(MediaType.APPLICATION_JSON)
//                    .header("Authorization", "Bearer " + token)
//                    .retrieve()
//                    .body(new ParameterizedTypeReference<>() {});
//        } catch (Exception e) {
//            log.error("Error calling B2B /users/me. ", e);
//            throw e;
//        }
//    }

    /*public StereumQuoteResponse createQuote(StereumQuoteRequest request) throws Exception {
        RestClient restClient = create();

        ResponseEntity<StereumQuoteResponse> response;
        try {
            response = restClient.post()
                    .uri(stereumUrl + "/api/v1/otc/quotes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("x-api-key", stereumToken)
                    .body(request)
                    .retrieve()
                    .toEntity(StereumQuoteResponse.class);
        } catch (Exception e) {
            log.error("Error calling Stereum createQuote. ", e);
            throw e;
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Stereum createQuote failed with status: {}", response.getStatusCode().value());
            throw new Exception("Stereum createQuote failed");
        }

        log.info("Stereum quote response: {}", response.getBody());
        return response.getBody();
    }

    public List<StereumBankResponse> listBanks(String country) throws Exception {
        RestClient restClient = create();
        try {
            List<StereumBankResponse> banks = restClient.get()
                    .uri(stereumUrl + "/api/v1/banks?country=" + country)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("x-api-key", stereumToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            log.info("Stereum banks for {}: {}", country, banks);
            return banks;
        } catch (Exception e) {
            log.error("Error calling Stereum listBanks. ", e);
            throw e;
        }
    }*/

    private RestClient create() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
//        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));
//        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        return RestClient.builder().requestFactory(factory).build();
    }
}
