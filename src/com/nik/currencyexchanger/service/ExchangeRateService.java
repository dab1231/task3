package com.nik.currencyexchanger.service;

import com.nik.currencyexchanger.dao.ExchangeRateDao;
import com.nik.currencyexchanger.dto.ExchangeDto;
import com.nik.currencyexchanger.dto.ExchangeRateDto;
import com.nik.currencyexchanger.entity.ExchangeRate;

import java.util.List;

public class ExchangeRateService {

    private static final ExchangeRateService INSTANCE = new ExchangeRateService();
    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();

    private ExchangeRateService(){

    }




}
