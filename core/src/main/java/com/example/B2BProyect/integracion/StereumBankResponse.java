package com.example.B2BProyect.integracion;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class StereumBankResponse {
    private String type;
    private String code;
    private String description;
    private String icon;
}
