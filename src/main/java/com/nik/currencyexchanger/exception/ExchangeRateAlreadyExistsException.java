package com.nik.currencyexchanger.exception;

public class ExchangeRateAlreadyExistsException extends RuntimeException{
    public ExchangeRateAlreadyExistsException(int baseCurrencyId, int targetCurrencyId) {
        super("Exchange rate for pair base = " + baseCurrencyId + " target = " + targetCurrencyId + " pair already exists");
    }
}
