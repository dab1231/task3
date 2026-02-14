package com.nik.currencyexchanger.exception;

public class CurrencyNotFoundException extends RuntimeException{
    public CurrencyNotFoundException(String code) {
        super("Currency with code " + code + " not found");
    }

    public CurrencyNotFoundException(int id) {
        super("Currency with id " + id + " not found");
    }
}
