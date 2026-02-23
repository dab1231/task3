package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.dto.request.ExchangeRateRequestDto;
import com.nik.currencyexchanger.exception.CurrencyNotFoundException;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ExchangeRateAlreadyExistsException;
import com.nik.currencyexchanger.exception.ValidationException;
import com.nik.currencyexchanger.service.ExchangeRateService;
import com.nik.currencyexchanger.validation.Validator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    private ExchangeRateService exchangeRateService;
    private Gson gson;

    @Override
    public void init() {
        exchangeRateService = (ExchangeRateService) getServletContext().getAttribute("exchangeRateService");
        gson = (Gson) getServletContext().getAttribute("gson");
    }

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

        var baseCode = Validator.validateCurrencyCode(baseCurrencyCode);
        var targetCode = Validator.validateCurrencyCode(targetCurrencyCode);
        var rate = Validator.validateRate(rateString);

        ExchangeRateRequestDto requestDto = new ExchangeRateRequestDto(baseCode, targetCode, rate);
        var exchangeRateDto = exchangeRateService
                .createExchangeRate(requestDto);
        resp.setStatus(201);
        var jsonString = gson.toJson(exchangeRateDto);
        resp.getWriter()
                .write(jsonString);
    }
}
