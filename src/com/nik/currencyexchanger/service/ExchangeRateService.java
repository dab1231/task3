package com.nik.currencyexchanger.service;

import com.nik.currencyexchanger.dao.ExchangeRateDao;
import com.nik.currencyexchanger.dto.CurrencyDto;
import com.nik.currencyexchanger.dto.ExchangeRateDto;
import com.nik.currencyexchanger.entity.ExchangeRate;
import com.nik.currencyexchanger.exception.ExchangeRateNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateService {

    private static final ExchangeRateService INSTANCE = new ExchangeRateService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private final CurrencyService currencyService = CurrencyService.getInstance();

    private ExchangeRateService(){

    }

    public static ExchangeRateService getInstance(){
        return INSTANCE;
    }

    public List<ExchangeRateDto> getAllExchangeRates(){
        var exchangeRates = exchangeRateDao.findAll();
        List<ExchangeRateDto> exchangeRateDtos = new ArrayList<>();
        for(ExchangeRate exchangeRate : exchangeRates){
            exchangeRateDtos.add(buildDto(exchangeRate));
        }
        return exchangeRateDtos;
    }

    public ExchangeRateDto getExchangeRate(String baseCode, String targetCode){
        var baseId = currencyService.getCurrencyByCode(baseCode).getId();
        var targetId = currencyService.getCurrencyByCode(targetCode).getId();

        var exchangeRate = exchangeRateDao.findByCurrenciesId(baseId, targetId)
                .orElseThrow(() -> new ExchangeRateNotFoundException());
        return buildDto(exchangeRate);
    }

    public ExchangeRateDto createExchangeRate(String baseCode, String targetCode, BigDecimal rate){
        var baseId = currencyService.getCurrencyByCode(baseCode).getId();
        var targetId = currencyService.getCurrencyByCode(targetCode).getId();

        var exchangeRate = exchangeRateDao.save(baseId, targetId, rate);
        return buildDto(exchangeRate);
    }

    public ExchangeRateDto updateExchangeRate(String baseCode, String targetCode, BigDecimal rate){
        var baseId = currencyService.getCurrencyByCode(baseCode).getId();
        var targetId = currencyService.getCurrencyByCode(targetCode).getId();
        var exchangeRate = exchangeRateDao.update(baseId, targetId, rate)
                .orElseThrow(() -> new ExchangeRateNotFoundException());
        return buildDto(exchangeRate);
    }

    private ExchangeRateDto buildDto(ExchangeRate exchangeRate){
        return new ExchangeRateDto(
                exchangeRate.getId(),
                currencyService.getCurrencyById(exchangeRate.getBaseCurrencyId()),
                currencyService.getCurrencyById(exchangeRate.getTargetCurrencyId()),
                exchangeRate.getRate()
        );
    }


}
