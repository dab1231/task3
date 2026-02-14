package com.nik.currencyexchanger.service;

import com.nik.currencyexchanger.dao.CurrencyDao;
import com.nik.currencyexchanger.dto.CurrencyDto;
import com.nik.currencyexchanger.entity.Currency;
import com.nik.currencyexchanger.exception.CurrencyNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class CurrencyService {

    private static final CurrencyService INSTANCE = new CurrencyService();
    private final CurrencyDao currencyDao = CurrencyDao.getInstance();

    private CurrencyService(){

    }

    public List<CurrencyDto> findAll(){
        var currenciesEntity = currencyDao.findAll();
        List<CurrencyDto> currencyDtos = new ArrayList<>();
        for(Currency currency : currenciesEntity){
            currencyDtos.add(buildDto(currency));
        }
        return currencyDtos;
    }

    public CurrencyDto findByCode(String code){
        var currencyOptional = currencyDao.findByCode(code);
        var currency = currencyOptional.orElseThrow(() -> new CurrencyNotFoundException(code));
        return buildDto(currency);
    }

    public CurrencyDto create(String name, String code, String sign){
        Currency currencyWithoutId = new Currency(0, code, name, sign);
        var currency = currencyDao.create(currencyWithoutId);
        return buildDto(currency);
    }

    private CurrencyDto buildDto(Currency currency){
        return new CurrencyDto(
                currency.getId(),
                currency.getCode(),
                currency.getFull_name(),
                currency.getSign()
        );
    }
}
