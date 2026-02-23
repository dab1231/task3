package com.nik.currencyexchanger.service;

import com.nik.currencyexchanger.dao.CurrencyDao;
import com.nik.currencyexchanger.dao.ExchangeRateDao;
import com.nik.currencyexchanger.dto.request.ExchangeRateRequestDto;
import com.nik.currencyexchanger.dto.request.ExchangeRequestDto;
import com.nik.currencyexchanger.dto.response.ExchangeRateResponseDto;
import com.nik.currencyexchanger.dto.response.ExchangeResponseDto;
import com.nik.currencyexchanger.entity.Currency;
import com.nik.currencyexchanger.entity.ExchangeRate;
import com.nik.currencyexchanger.exception.ExchangeRateNotFoundException;
import com.nik.currencyexchanger.mapper.CurrencyMapper;
import com.nik.currencyexchanger.mapper.ExchangeRateMapper;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExchangeRateService {

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int DECIMAL_SCALE = 6;
    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyService currencyService;
    private final ExchangeRateMapper exchangeRateMapper = Mappers.getMapper(ExchangeRateMapper.class);
    private final CurrencyDao currencyDao;
    private final CurrencyMapper currencyMapper = Mappers.getMapper(CurrencyMapper.class);

    public ExchangeRateService(ExchangeRateDao exchangeRateDao, CurrencyService currencyService, CurrencyDao currencyDao){
        this.exchangeRateDao = exchangeRateDao;
        this.currencyService = currencyService;
        this.currencyDao = currencyDao;
    }

    public List<ExchangeRateResponseDto> getAllExchangeRates(){
        var exchangeRates = exchangeRateDao.findAll();
        var allCurrencies = currencyDao.findAll();
        Map<Integer, Currency> currenciesById = new HashMap<>();
        for(com.nik.currencyexchanger.entity.Currency currency : allCurrencies){
            currenciesById.put(currency.getId(), currency);
        }
        List<ExchangeRateResponseDto> exchangeRateDtos = new ArrayList<>();
        for(ExchangeRate exchangeRate : exchangeRates){
            var baseCurrency = currenciesById.get(exchangeRate.getBaseCurrencyId());
            var targetCurrency = currenciesById.get(exchangeRate.getTargetCurrencyId());
            exchangeRateDtos.add(exchangeRateMapper.toDto(
                    exchangeRate,
                    currencyMapper.toDto(baseCurrency),
                    currencyMapper.toDto(targetCurrency)
            ));
        }
        return exchangeRateDtos;
    }

    public ExchangeRateResponseDto getExchangeRate(String baseCode, String targetCode){
        var baseId = currencyService.getCurrencyByCode(baseCode).id();
        var targetId = currencyService.getCurrencyByCode(targetCode).id();

        var exchangeRate = exchangeRateDao.findByCurrenciesId(baseId, targetId)
                .orElseThrow(() -> new ExchangeRateNotFoundException());
        return exchangeRateMapper.toDto(exchangeRate,
                currencyService.getCurrencyById(baseId),
                currencyService.getCurrencyById(targetId));
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

    public ExchangeRateResponseDto createExchangeRate(ExchangeRateRequestDto requestDto){
        var baseId = currencyService.getCurrencyByCode(requestDto.baseCode()).id();
        var targetId = currencyService.getCurrencyByCode(requestDto.targetCode()).id();

        var exchangeRate = exchangeRateDao.create(baseId, targetId, requestDto.rate());
        return exchangeRateMapper.toDto(
                exchangeRate,
                currencyService.getCurrencyById(baseId),
                currencyService.getCurrencyById(targetId)
        );
    }

    public ExchangeRateResponseDto updateExchangeRate(ExchangeRateRequestDto requestDto){
        var baseId = currencyService.getCurrencyByCode(requestDto.baseCode()).id();
        var targetId = currencyService.getCurrencyByCode(requestDto.targetCode()).id();
        var exchangeRate = exchangeRateDao.update(baseId, targetId, requestDto.rate())
                .orElseThrow(() -> new ExchangeRateNotFoundException());
        return exchangeRateMapper.toDto(
                exchangeRate,
                currencyService.getCurrencyById(baseId),
                currencyService.getCurrencyById(targetId)
        );
    }
}
