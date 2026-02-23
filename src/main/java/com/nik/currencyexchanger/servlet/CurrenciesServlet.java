package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.dto.request.CurrencyRequestDto;
import com.nik.currencyexchanger.exception.CurrencyAlreadyExistsException;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ValidationException;
import com.nik.currencyexchanger.service.CurrencyService;
import com.nik.currencyexchanger.validation.Validator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private CurrencyService currencyService;
    private Gson gson;

    @Override
    public void init() {
        currencyService = (CurrencyService) getServletContext().getAttribute("currencyService");
        gson = (Gson) getServletContext().getAttribute("gson");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, DataBaseException {
        var currenciesDto = currencyService.getAllCurrencies();
        String jsonString = gson.toJson(currenciesDto);

        resp.setStatus(200);
        resp.getWriter()
                .write(jsonString);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, CurrencyAlreadyExistsException, DataBaseException, ValidationException {
        var name = req.getParameter("name");
        var code = req.getParameter("code");
        var sign = req.getParameter("sign");

        var validateName = Validator.validateName(name);
        var validateCurrencyCode = Validator.validateCurrencyCode(code);
        var validateSign = Validator.validateSign(sign);

        CurrencyRequestDto requestDto = new CurrencyRequestDto(validateCurrencyCode, validateName, validateSign);
        var currency = currencyService.createCurrency(requestDto);
        resp.setStatus(201);
        var jsonString = gson.toJson(currency);
        resp.getWriter()
                .write(jsonString);

    }
}
