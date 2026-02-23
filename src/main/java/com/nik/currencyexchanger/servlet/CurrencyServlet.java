package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.exception.CurrencyNotFoundException;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ValidationException;
import com.nik.currencyexchanger.service.CurrencyService;
import com.nik.currencyexchanger.validation.Validator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private CurrencyService currencyService;
    private Gson gson;

    @Override
    public void init() {
        currencyService = (CurrencyService) getServletContext().getAttribute("currencyService");
        gson = (Gson) getServletContext().getAttribute("gson");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, DataBaseException, CurrencyNotFoundException, ValidationException {
        var pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.length() != 4) {
            throw new ValidationException("Currency code is missing");
        }

        var code = pathInfo.substring(1);

        var validateCurrencyCode = Validator.validateCurrencyCode(code);
        var currencyDto = currencyService.getCurrencyByCode(validateCurrencyCode);

        resp.setStatus(200);
        var jsonString = gson.toJson(currencyDto);
        resp.getWriter()
                .write(jsonString);
    }
}
