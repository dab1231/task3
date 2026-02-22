package com.nik.currencyexchanger.dto.request;

import java.math.BigDecimal;

public record ExchangeRequestDto(String baseCode, String targetCode, BigDecimal amount) {
}
