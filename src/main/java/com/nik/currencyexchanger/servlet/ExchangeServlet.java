package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.dto.request.ExchangeRequestDto;
import com.nik.currencyexchanger.exception.CurrencyNotFoundException;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ExchangeRateNotFoundException;
import com.nik.currencyexchanger.exception.ValidationException;
import com.nik.currencyexchanger.service.ExchangeService;
import com.nik.currencyexchanger.validation.Validator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {

    private ExchangeService exchangeService;
    private Gson gson;

    @Override
    public void init() {
        exchangeService = (ExchangeService) getServletContext().getAttribute("exchangeService");
        gson = (Gson) getServletContext().getAttribute("gson");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, NumberFormatException, DataBaseException, CurrencyNotFoundException, ExchangeRateNotFoundException, ValidationException {

        var baseCode = req.getParameter("from");
        var targetCode = req.getParameter("to");
        var amountString = req.getParameter("amount");

        var validateBaseCode = Validator.validateCurrencyCode(baseCode);
        var validateTargetCode = Validator.validateCurrencyCode(targetCode);
        var validateAmount = Validator.validateAmount(amountString);

        ExchangeRequestDto requestDto = new ExchangeRequestDto(validateBaseCode, validateTargetCode, validateAmount);
        var exchangeDto = exchangeService.calculateExchange(requestDto);
        resp.setStatus(200);
        var jsonString = gson.toJson(exchangeDto);
        resp.getWriter()
                .write(jsonString);
    }
}
