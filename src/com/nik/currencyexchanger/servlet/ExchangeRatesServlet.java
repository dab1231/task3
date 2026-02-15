package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.service.ExchangeRateService;
import com.nik.currencyexchanger.util.ErrorSetter;
import com.nik.currencyexchanger.util.StatusSetter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ExchangeRatesServlet extends HttpServlet {

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var exchangeRatesDto = exchangeRateService.getAllExchangeRates();
            StatusSetter.setHeadersAndStatus(resp, 200);
            var jsonString = gson.toJson(exchangeRatesDto);
            resp.getWriter()
                    .write(jsonString);
        } catch (DataBaseException e) {
            ErrorSetter.setError(resp, 500, "DB error");
        }
    }


}
