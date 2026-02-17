package com.nik.currencyexchanger.service;

import com.nik.currencyexchanger.dao.ExchangeRateDao;
import com.nik.currencyexchanger.dto.ExchangeDto;
import com.nik.currencyexchanger.dto.ExchangeRateDto;
import com.nik.currencyexchanger.entity.ExchangeRate;
import com.nik.currencyexchanger.exception.ExchangeRateNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateService {

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int DECIMAL_SCALE = 6;
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

    public ExchangeDto calculateExchange(String baseCode, String targetCode, BigDecimal amount){
        var baseCurrencyId = currencyService.getCurrencyByCode(baseCode).getId();
        var targetCurrencyId = currencyService.getCurrencyByCode(targetCode).getId();
        var exchangeRateOptional = exchangeRateDao.findByCurrenciesId(baseCurrencyId, targetCurrencyId);

        if(exchangeRateOptional.isPresent()){
            var exchangeRate = exchangeRateOptional.get();
            var rate = exchangeRate.getRate();
            var convertedAmount = rate.multiply(amount).setScale(2, ROUNDING_MODE);
            return buildDto(exchangeRate, amount, convertedAmount);
        }
        else{
            var usdId = currencyService.getCurrencyByCode("USD").getId();
            var usdToBase = exchangeRateDao.findByCurrenciesId(usdId, baseCurrencyId)
                    .orElseThrow(() -> new ExchangeRateNotFoundException());
            var usdToTarget = exchangeRateDao.findByCurrenciesId(usdId, targetCurrencyId)
                    .orElseThrow(() -> new ExchangeRateNotFoundException());
            var usdToTargetRate = usdToTarget.getRate();
            var usdToBaseRate = usdToBase.getRate();

            BigDecimal newRate = usdToTargetRate.divide(usdToBaseRate, DECIMAL_SCALE, ROUNDING_MODE);
            var baseToTargetRate = new ExchangeRate(
                    0,
                    baseCurrencyId,
                    targetCurrencyId,
                    newRate
            );
            var convertedAmount = newRate.multiply(amount).setScale(2, ROUNDING_MODE);
            return buildDto(baseToTargetRate, amount, convertedAmount);
        }
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

    private ExchangeDto buildDto(ExchangeRate exchangeRate, BigDecimal amount, BigDecimal convertedAmount){
        return new ExchangeDto(
                currencyService.getCurrencyById(exchangeRate.getBaseCurrencyId()),
                currencyService.getCurrencyById(exchangeRate.getTargetCurrencyId()),
                exchangeRate.getRate(),
                amount,
                convertedAmount
        );
    }


}
