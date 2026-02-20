package com.nik.currencyexchanger.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ExchangeRate {

    private int id;
    private int baseCurrencyId;
    private int targetCurrencyId;
    private BigDecimal rate;
}
