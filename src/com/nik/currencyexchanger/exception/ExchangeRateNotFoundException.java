package com.nik.currencyexchanger.exception;

public class ExchangeRateNotFoundException extends RuntimeException{
    public ExchangeRateNotFoundException(){
        super("Exchange rate not found");
    }
}
