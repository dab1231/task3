package com.nik.currencyexchanger.service;

import com.nik.currencyexchanger.dao.CurrencyDao;
import com.nik.currencyexchanger.dto.request.CurrencyRequestDto;
import com.nik.currencyexchanger.dto.response.CurrencyResponseDto;
import com.nik.currencyexchanger.entity.Currency;
import com.nik.currencyexchanger.exception.CurrencyNotFoundException;
import com.nik.currencyexchanger.mapper.CurrencyMapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

public class CurrencyService {

    private final CurrencyMapper currencyMapper = Mappers.getMapper(CurrencyMapper.class);
    private final CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao){
        this.currencyDao = currencyDao;
    }

    public List<CurrencyResponseDto> getAllCurrencies(){
        var currenciesEntity = currencyDao.findAll();
        List<CurrencyResponseDto> currencyDtos = new ArrayList<>();
        for(Currency currency : currenciesEntity){
            currencyDtos.add(currencyMapper.toDto(currency));
        }
        return currencyDtos;
    }

    public CurrencyResponseDto getCurrencyByCode(String code){
        var currencyOptional = currencyDao.findByCode(code);
        var currency = currencyOptional.orElseThrow(() -> new CurrencyNotFoundException(code));
        return currencyMapper.toDto(currency);
    }

    public CurrencyResponseDto getCurrencyById(int id){
        var currencyOptional = currencyDao.findById(id);
        var currency = currencyOptional.orElseThrow(() -> new CurrencyNotFoundException(id));
        return currencyMapper.toDto(currency);
    }

    public CurrencyResponseDto createCurrency(CurrencyRequestDto requestDto){
        var code = requestDto.code();
        var name = requestDto.name();
        var sign = requestDto.sign();
        var currency = currencyDao.create(code, name ,sign);
        return currencyMapper.toDto(currency);
    }
}
