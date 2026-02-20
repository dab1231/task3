package com.nik.currencyexchanger.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Currency {

    private int id;
    private String code;
    private String fullName;
    private String sign;
}
