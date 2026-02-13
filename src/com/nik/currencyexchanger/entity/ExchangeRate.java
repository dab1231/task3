package com.nik.currencyexchanger.entity;

import lombok.Data;

@Data
public class ExchangeRate {
    private int id;
    private int baseCurrencyId;
    private int targetCurrencyId;
    private double rate;
}
