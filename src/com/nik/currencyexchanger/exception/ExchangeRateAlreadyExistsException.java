package com.nik.currencyexchanger.exception;

public class ExchangeRateAlreadyExistsException extends RuntimeException{
    public ExchangeRateAlreadyExistsException() {
        super("Exchange rate for this pair already exists");
    }
}
