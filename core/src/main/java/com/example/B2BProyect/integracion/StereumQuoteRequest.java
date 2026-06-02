package com.example.B2BProyect.integracion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class StereumQuoteRequest {
    private String id;
    @JsonProperty("externalUserId")
    private String externalUserId;
    private String side;
    @JsonProperty("inputAmount")
    private String inputAmount;
    @JsonProperty("inputCurrency")
    private String inputCurrency;
    @JsonProperty("inputNetwork")
    private String inputNetwork;
    @JsonProperty("outputCurrency")
    private String outputCurrency;
    @JsonProperty("outputNetwork")
    private String outputNetwork;
    private String country;
    @JsonProperty("serviceCode")
    private String serviceCode;
}
