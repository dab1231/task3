package com.nik.currencyexchanger.service;

import com.nik.currencyexchanger.dao.ExchangeRateDao;
import com.nik.currencyexchanger.dto.request.ExchangeRequestDto;
import com.nik.currencyexchanger.dto.response.ExchangeResponseDto;
import com.nik.currencyexchanger.entity.ExchangeRate;
import com.nik.currencyexchanger.exception.ExchangeRateNotFoundException;
import com.nik.currencyexchanger.mapper.ExchangeRateMapper;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class ExchangeService {

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int DECIMAL_SCALE = 6;
    private final ExchangeRateMapper exchangeRateMapper = Mappers.getMapper(ExchangeRateMapper.class);
    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyService currencyService;

    public ExchangeService(ExchangeRateDao exchangeRateDao, CurrencyService currencyService){
        this.exchangeRateDao = exchangeRateDao;
        this.currencyService = currencyService;
    }

    public ExchangeResponseDto calculateExchange(ExchangeRequestDto requestDto){
        var baseCurrencyId = currencyService.getCurrencyByCode(requestDto.baseCode()).id();
        var targetCurrencyId = currencyService.getCurrencyByCode(requestDto.targetCode()).id();
        var exchangeRateOptional = exchangeRateDao.findByCurrenciesId(baseCurrencyId, targetCurrencyId);
        var amount = requestDto.amount();

        if(exchangeRateOptional.isPresent()){
            var exchangeRate = exchangeRateOptional.get();
            var rate = exchangeRate.getRate();
            var convertedAmount = rate.multiply(amount).setScale(DECIMAL_SCALE, ROUNDING_MODE);
            return exchangeRateMapper.toExchangeDto(
                    exchangeRate,
                    currencyService.getCurrencyById(baseCurrencyId),
                    currencyService.getCurrencyById(targetCurrencyId),
                    amount,
                    convertedAmount);
        }
        else if (exchangeRateDao.findByCurrenciesId(targetCurrencyId, baseCurrencyId).isPresent()) {
            var reversedExchangeRate
                    = exchangeRateDao.findByCurrenciesId(targetCurrencyId, baseCurrencyId).get();
            var rate = reversedExchangeRate.getRate();
            var reversedRate = BigDecimal.ONE.divide(rate, DECIMAL_SCALE, ROUNDING_MODE);
            var convertedAmount = reversedRate.multiply(amount).setScale(DECIMAL_SCALE, ROUNDING_MODE);
            var exchangeRate = new ExchangeRate(
                    0,
                    baseCurrencyId,
                    targetCurrencyId,
                    reversedRate
            );
            return exchangeRateMapper.toExchangeDto(
                    exchangeRate,
                    currencyService.getCurrencyById(baseCurrencyId),
                    currencyService.getCurrencyById(targetCurrencyId),
                    amount,
                    convertedAmount);
        }
        else{
            var usdId = currencyService.getCurrencyByCode("USD").id();
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
            return exchangeRateMapper.toExchangeDto(
                    baseToTargetRate,
                    currencyService.getCurrencyById(baseCurrencyId),
                    currencyService.getCurrencyById(targetCurrencyId),
                    amount,
                    convertedAmount);
        }
    }
}
