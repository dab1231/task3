package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.dto.request.CurrencyRequestDto;
import com.nik.currencyexchanger.exception.CurrencyAlreadyExistsException;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ValidationException;
import com.nik.currencyexchanger.service.CurrencyService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private CurrencyService currencyService;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        currencyService = (CurrencyService) getServletContext().getAttribute("currencyService");
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

        if (name == null || code == null || sign == null
                || name.isBlank() || code.isBlank() || sign.isBlank() || sign.length() > 3) {
            throw new ValidationException("The required form field is missing.");
        }
        CurrencyRequestDto requestDto = new CurrencyRequestDto(code,name, sign);
        var currency = currencyService.createCurrency(requestDto); // TODO: разобраться с reqDTO и respDTO
        resp.setStatus(201);
        var jsonString = gson.toJson(currency);
        resp.getWriter()
                .write(jsonString);

    }
}
