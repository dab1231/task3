package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.exception.CurrencyNotFoundException;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ExchangeRateAlreadyExistsException;
import com.nik.currencyexchanger.service.ExchangeRateService;
import com.nik.currencyexchanger.util.ErrorSetter;
import com.nik.currencyexchanger.util.StatusSetter;
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
        StatusSetter.setHeadersAndStatus(resp, 200);
        var jsonString = gson.toJson(exchangeRatesDto);
        resp.getWriter()
                .write(jsonString);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ExchangeRateAlreadyExistsException, CurrencyNotFoundException, DataBaseException, NumberFormatException {

        var baseCurrencyCode = req.getParameter("baseCurrencyCode");
        var targetCurrencyCode = req.getParameter("targetCurrencyCode");
        var rateString = req.getParameter("rate");

        if (baseCurrencyCode == null || targetCurrencyCode == null || rateString == null
                || baseCurrencyCode.isBlank() || targetCurrencyCode.isBlank() || rateString.isBlank()) {
            ErrorSetter.setError(resp, 400, "The required form field is missing");
            return;
        }

        BigDecimal rate = new BigDecimal(rateString);
        var exchangeRateDto = exchangeRateService
                .createExchangeRate(baseCurrencyCode, targetCurrencyCode, rate);
        StatusSetter.setHeadersAndStatus(resp, 201);
        var jsonString = gson.toJson(exchangeRateDto);
        resp.getWriter()
                .write(jsonString);
    }
}
