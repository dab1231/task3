package com.nik.currencyexchanger.exception;

public class DataBaseException extends RuntimeException{
    public DataBaseException(String message, Throwable cause){
        super(message, cause);
    }

    public DataBaseException(String message){
        super(message);
    }
}
