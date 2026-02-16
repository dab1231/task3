package com.nik.currencyexchanger.entity;

import lombok.Data;

@Data
public class Currency {

    private int id;
    private String code;
    private String full_name;
    private String sign;

    public Currency(int id, String code, String full_name, String sign) {
        this.id = id;
        this.code = code;
        this.full_name = full_name;
        this.sign = sign;
    }
}
