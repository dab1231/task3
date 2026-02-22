package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.dto.request.ExchangeRateRequestDto;
import com.nik.currencyexchanger.exception.CurrencyNotFoundException;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ExchangeRateAlreadyExistsException;
import com.nik.currencyexchanger.exception.ValidationException;
import com.nik.currencyexchanger.service.ExchangeRateService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, DataBaseException {
        var exchangeRatesDto = exchangeRateService.getAllExchangeRates();
        resp.setStatus(200);
        var jsonString = gson.toJson(exchangeRatesDto);
        resp.getWriter()
                .write(jsonString);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ExchangeRateAlreadyExistsException, CurrencyNotFoundException, DataBaseException, NumberFormatException, ValidationException {

        var baseCurrencyCode = req.getParameter("baseCurrencyCode");
        var targetCurrencyCode = req.getParameter("targetCurrencyCode");
        var rateString = req.getParameter("rate");

        if (baseCurrencyCode == null || targetCurrencyCode == null || rateString == null
                || baseCurrencyCode.isBlank() || targetCurrencyCode.isBlank() || rateString.isBlank()) {
            throw new ValidationException("The required form field is missing.");
        }

        BigDecimal rate = new BigDecimal(rateString);
        ExchangeRateRequestDto requestDto = new ExchangeRateRequestDto(baseCurrencyCode, targetCurrencyCode, rate);
        var exchangeRateDto = exchangeRateService
                .createExchangeRate(requestDto);
        resp.setStatus(201);
        var jsonString = gson.toJson(exchangeRateDto);
        resp.getWriter()
                .write(jsonString);
    }
}
