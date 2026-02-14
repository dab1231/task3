package com.nik.currencyexchanger.service;

import com.nik.currencyexchanger.dao.ExchangeRateDao;
import com.nik.currencyexchanger.dto.ExchangeRateDto;
import com.nik.currencyexchanger.entity.ExchangeRate;

import java.util.ArrayList;
import java.util.List;

public class ExchangeRateService {

    private static final ExchangeRateService INSTANCE = new ExchangeRateService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();

    private ExchangeRateService(){

    }

    public List<ExchangeRateDto> findAll(){
        var exchangeRates = exchangeRateDao.findAll();
        List<ExchangeRateDto> exchangeRateDtos = new ArrayList<>();
        for(ExchangeRate exchangeRate : exchangeRates){
            exchangeRateDtos.add(buildDto(exchangeRate));
        }
        return exchangeRateDtos;
    }

    private ExchangeRateDto buildDto(ExchangeRate exchangeRate){
        return new ExchangeRateDto(
                exchangeRate.getId(),
                currencyService.findById(exchangeRate.getBaseCurrencyId()),
                currencyService.findById(exchangeRate.getTargetCurrencyId()),
                exchangeRate.getRate()
        );
    }


}
