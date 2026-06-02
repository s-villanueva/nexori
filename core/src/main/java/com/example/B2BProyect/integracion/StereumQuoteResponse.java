package com.example.B2BProyect.integracion;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class StereumQuoteResponse {
    private String id;
    private String pair;
    private String side;
    @JsonProperty("inputAmount")
    private double inputAmount;
    @JsonProperty("inputCurrency")
    private String inputCurrency;
    @JsonProperty("outputAmount")
    private double outputAmount;
    @JsonProperty("outputCurrency")
    private String outputCurrency;
    @JsonProperty("exchangeRate")
    private double exchangeRate;
    @JsonProperty("serviceFee")
    private double serviceFee;
    @JsonProperty("feeCurrency")
    private String feeCurrency;
    @JsonProperty("expireAt")
    private long expireAt;
    @JsonProperty("expiresInSeconds")
    private int expiresInSeconds;
}
