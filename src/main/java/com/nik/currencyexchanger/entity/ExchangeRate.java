package com.nik.currencyexchanger.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRate {
    private int id;
    private int baseCurrencyId;
    private int targetCurrencyId;
    private BigDecimal rate;

    public ExchangeRate(int id, int baseCurrencyId, int targetCurrencyId, BigDecimal rate) {
        this.id = id;
        this.baseCurrencyId = baseCurrencyId;
        this.targetCurrencyId = targetCurrencyId;
        this.rate = rate;
    }

    public ExchangeRate(){

    }
}
