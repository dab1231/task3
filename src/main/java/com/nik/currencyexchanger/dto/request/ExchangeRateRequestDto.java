package com.nik.currencyexchanger.dto.request;

import java.math.BigDecimal;

public record ExchangeRateRequestDto(String baseCode, String targetCode, BigDecimal rate) {
}
