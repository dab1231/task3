package com.nik.currencyexchanger.service;

import com.nik.currencyexchanger.dao.CurrencyDao;
import com.nik.currencyexchanger.dto.response.CurrencyResponseDto;
import com.nik.currencyexchanger.dto.request.CurrencyRequestDto;
import com.nik.currencyexchanger.entity.Currency;
import com.nik.currencyexchanger.exception.CurrencyNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class CurrencyService {

    private final CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao){
        this.currencyDao = currencyDao;
    }

    public List<CurrencyResponseDto> getAllCurrencies(){
        var currenciesEntity = currencyDao.findAll();
        List<CurrencyResponseDto> currencyDtos = new ArrayList<>();
        for(Currency currency : currenciesEntity){
            currencyDtos.add(buildDto(currency));
        }
        return currencyDtos;
    }

    public CurrencyResponseDto getCurrencyByCode(String code){
        var currencyOptional = currencyDao.findByCode(code);
        var currency = currencyOptional.orElseThrow(() -> new CurrencyNotFoundException(code));
        return buildDto(currency);
    }

    public CurrencyResponseDto getCurrencyById(int id){
        var currencyOptional = currencyDao.findById(id);
        var currency = currencyOptional.orElseThrow(() -> new CurrencyNotFoundException(id));
        return buildDto(currency);
    }

    public CurrencyResponseDto createCurrency(CurrencyRequestDto requestDto){
        var code = requestDto.code();
        var name = requestDto.name();
        var sign = requestDto.sign();
        var currency = currencyDao.create(code, name ,sign);
        return buildDto(currency);
    }

    private CurrencyResponseDto buildDto(Currency currency){
        return new CurrencyResponseDto(
                currency.getId(),
                currency.getCode(),
                currency.getFullName(),
                currency.getSign()
        );
    }
}
