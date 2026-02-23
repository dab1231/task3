package com.nik.currencyexchanger.service;

import com.nik.currencyexchanger.dao.ExchangeRateDao;
import com.nik.currencyexchanger.dto.request.ExchangeRateRequestDto;
import com.nik.currencyexchanger.dto.request.ExchangeRequestDto;
import com.nik.currencyexchanger.dto.response.ExchangeResponseDto;
import com.nik.currencyexchanger.dto.response.ExchangeRateResponseDto;
import com.nik.currencyexchanger.entity.ExchangeRate;
import com.nik.currencyexchanger.exception.ExchangeRateNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateService {

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int DECIMAL_SCALE = 6;
    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyService currencyService;

    public ExchangeRateService(ExchangeRateDao exchangeRateDao, CurrencyService currencyService){
        this.exchangeRateDao = exchangeRateDao;
        this.currencyService = currencyService;
    }

    public List<ExchangeRateResponseDto> getAllExchangeRates(){
        var exchangeRates = exchangeRateDao.findAll();
        List<ExchangeRateResponseDto> exchangeRateDtos = new ArrayList<>();
        for(ExchangeRate exchangeRate : exchangeRates){
            exchangeRateDtos.add(buildDto(exchangeRate));
        }
        return exchangeRateDtos;
    }

    public ExchangeRateResponseDto getExchangeRate(String baseCode, String targetCode){
        var baseId = currencyService.getCurrencyByCode(baseCode).id();
        var targetId = currencyService.getCurrencyByCode(targetCode).id();

        var exchangeRate = exchangeRateDao.findByCurrenciesId(baseId, targetId)
                .orElseThrow(() -> new ExchangeRateNotFoundException());
        return buildDto(exchangeRate);
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
            return buildDto(exchangeRate, amount, convertedAmount);
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
            return buildDto(exchangeRate, amount, convertedAmount);
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
            return buildDto(baseToTargetRate, amount, convertedAmount);
        }
    }

    public ExchangeRateResponseDto createExchangeRate(ExchangeRateRequestDto requestDto){
        var baseId = currencyService.getCurrencyByCode(requestDto.baseCode()).id();
        var targetId = currencyService.getCurrencyByCode(requestDto.targetCode()).id();

        var responseDto = exchangeRateDao.create(baseId, targetId, requestDto.rate());
        return buildDto(responseDto);
    }

    public ExchangeRateResponseDto updateExchangeRate(ExchangeRateRequestDto requestDto){
        var baseId = currencyService.getCurrencyByCode(requestDto.baseCode()).id();
        var targetId = currencyService.getCurrencyByCode(requestDto.targetCode()).id();
        var exchangeRate = exchangeRateDao.update(baseId, targetId, requestDto.rate())
                .orElseThrow(() -> new ExchangeRateNotFoundException());
        return buildDto(exchangeRate);
    }

    private ExchangeRateResponseDto buildDto(ExchangeRate exchangeRate){
        return new ExchangeRateResponseDto(
                exchangeRate.getId(),
                currencyService.getCurrencyById(exchangeRate.getBaseCurrencyId()),
                currencyService.getCurrencyById(exchangeRate.getTargetCurrencyId()),
                exchangeRate.getRate()
        );
    }

    private ExchangeResponseDto buildDto(ExchangeRate exchangeRate, BigDecimal amount, BigDecimal convertedAmount){
        return new ExchangeResponseDto(
                currencyService.getCurrencyById(exchangeRate.getBaseCurrencyId()),
                currencyService.getCurrencyById(exchangeRate.getTargetCurrencyId()),
                exchangeRate.getRate(),
                amount,
                convertedAmount
        );
    }


}
