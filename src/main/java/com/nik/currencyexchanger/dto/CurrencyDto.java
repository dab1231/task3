package com.nik.currencyexchanger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CurrencyDto {
    private int id;
    private String code;
    private String name;
    private String sign;

}
