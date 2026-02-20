package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.exception.CurrencyNotFoundException;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ExchangeRateNotFoundException;
import com.nik.currencyexchanger.service.ExchangeRateService;
import com.nik.currencyexchanger.util.ErrorSetter;
import com.nik.currencyexchanger.util.StatusSetter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;


@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, NumberFormatException, DataBaseException, CurrencyNotFoundException, ExchangeRateNotFoundException {

        var baseCode = req.getParameter("from");
        var targetCode = req.getParameter("to");
        var amountString = req.getParameter("amount");
        if (baseCode == null || targetCode == null || amountString == null
                || baseCode.isBlank() || targetCode.isBlank() || amountString.isBlank()) {
            ErrorSetter.setError(resp, 400, "Required parameter is missing");
            return;
        }

        BigDecimal amount = new BigDecimal(amountString);
        var exchangeDto = exchangeRateService.calculateExchange(baseCode, targetCode, amount);
        StatusSetter.setHeadersAndStatus(resp, 200);
        var jsonString = gson.toJson(exchangeDto);
        resp.getWriter()
                .write(jsonString);
    }
}
