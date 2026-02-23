package com.nik.currencyexchanger.mapper;

import com.nik.currencyexchanger.dto.response.CurrencyResponseDto;
import com.nik.currencyexchanger.dto.response.ExchangeRateResponseDto;
import com.nik.currencyexchanger.dto.response.ExchangeResponseDto;
import com.nik.currencyexchanger.entity.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(uses = CurrencyMapper.class)
public interface ExchangeRateMapper {

    @Mapping(source = "exchangeRate.id", target = "id")
    @Mapping(source = "exchangeRate.rate", target = "rate")
    @Mapping(source = "baseCurrency", target = "baseCurrency")
    @Mapping(source = "targetCurrency", target = "targetCurrency")
    ExchangeRateResponseDto toDto(
            ExchangeRate exchangeRate, CurrencyResponseDto baseCurrency, CurrencyResponseDto targetCurrency);

    @Mapping(source = "exchangeRate.rate", target = "rate")
    @Mapping(source = "baseCurrency", target = "baseCurrency")
    @Mapping(source = "targetCurrency", target = "targetCurrency")
    ExchangeResponseDto toExchangeDto(ExchangeRate exchangeRate, CurrencyResponseDto baseCurrency, CurrencyResponseDto
            targetCurrency, BigDecimal amount, BigDecimal convertedAmount);
}
