package com.nik.currencyexchanger.validation;

import com.nik.currencyexchanger.exception.ValidationException;

import java.math.BigDecimal;

public class Validator {

    public static String validateCurrencyCode(String code){

        if (code == null || code.isBlank()) throw new ValidationException("Currency code is missing");
        code = code.trim();
        if (!code.equals(code.toUpperCase())) throw new ValidationException("Currency code must be upper case");
        return code;
    }

    public static BigDecimal validateRate(String rate) {
        if (rate == null || rate.isBlank()) throw new ValidationException("Rate is missing");
        try {
            var value = new BigDecimal(rate.trim());
            if (value.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("Rate must be positive");
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid rate format");
        }
    }

    public static BigDecimal validateAmount(String amount) {
        if (amount == null || amount.isBlank()) throw new ValidationException("Amount is missing");
        try {
            var value = new BigDecimal(amount.trim());
            if (value.compareTo(BigDecimal.ZERO) <= 0) throw new ValidationException("Amount must be positive");
            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid amount format");
        }
    }

    public static String validateCurrencyPair(String pair) {
        if (pair == null) throw new ValidationException("Currency pair is missing");
        if (pair.length() != 6) {
            throw new ValidationException("Currency codes for the pair are missing from the address.");
        }
        return pair;
    }

    public static String validateSign(String sign){
        if(sign == null || sign.isBlank()) throw new ValidationException("Sign is missing");
        if(sign.length() > 3) throw new ValidationException("Invalid format of sign");
        return sign;
    }

    public static String validateName(String name){
        if(name == null || name.isBlank()) throw new ValidationException("Name is missing");
        return name;
    }
}
