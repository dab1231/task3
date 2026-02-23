package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.dto.request.ExchangeRateRequestDto;
import com.nik.currencyexchanger.exception.CurrencyNotFoundException;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ValidationException;
import com.nik.currencyexchanger.service.ExchangeRateService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private ExchangeRateService exchangeRateService;
    private Gson gson;

    @Override
    public void init() {
        exchangeRateService = (ExchangeRateService) getServletContext().getAttribute("exchangeRateService");
        gson = (Gson) getServletContext().getAttribute("gson");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, CurrencyNotFoundException, DataBaseException, ValidationException {

        var pathInfo = req.getPathInfo();

        if (pathInfo == null) {
            throw new ValidationException("Currency pair is missing");
        }
        var baseAndTargetCode = pathInfo.substring(1);

        if (baseAndTargetCode.length() != 6) {
            throw new ValidationException("Currency codes for the pair are missing from the address");
        }

        var targetCode = baseAndTargetCode.substring(3);
        var baseCode = baseAndTargetCode.substring(0, 3);
        var exchangeRateDto = exchangeRateService.getExchangeRate(baseCode, targetCode);
        resp.setStatus(200);
        var jsonString = gson.toJson(exchangeRateDto);
        resp.getWriter()
                .write(jsonString);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (req.getMethod().equalsIgnoreCase("PATCH")) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, NumberFormatException, CurrencyNotFoundException, DataBaseException, ValidationException {

        var reader = req.getReader();
        var line = reader.readLine();
        if (line == null) {
            throw new ValidationException("The required form field is missing.");
        }
        var rateLine = line.split("=");
        var rateString = rateLine[1];
        if(new BigDecimal(rateString).compareTo(BigDecimal.ZERO) <= 0){
            throw new ValidationException("Error, rate must be positive");
        }

        var requestDto = getExchangeRateRequestDto(req, rateString);
        var exchangeRateDto = exchangeRateService.updateExchangeRate(requestDto);
        resp.setStatus(200);
        var jsonString = gson.toJson(exchangeRateDto);
        resp.getWriter()
                .write(jsonString);
    }

    private static ExchangeRateRequestDto getExchangeRateRequestDto(HttpServletRequest req, String rateString) {
        BigDecimal rate = new BigDecimal(rateString);


        var pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            throw new ValidationException("Currency pair is missing");
        }
        var baseAndTargetCode = pathInfo.substring(1);

        if (baseAndTargetCode.length() != 6) {
            throw new ValidationException("Currency codes for the pair are missing from the address.");
        }
        var targetCode = baseAndTargetCode.substring(3);
        var baseCode = baseAndTargetCode.substring(0, 3);

        return new ExchangeRateRequestDto(baseCode, targetCode, rate);
    }
}
