package com.nik.currencyexchanger.exception;

public class CurrencyAlreadyExistsException extends RuntimeException{
    public CurrencyAlreadyExistsException(String code){
        super("Currency with code = " + code + "already exists");
    }
}
