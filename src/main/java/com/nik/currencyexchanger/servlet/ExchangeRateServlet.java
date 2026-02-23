package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.dto.request.ExchangeRateRequestDto;
import com.nik.currencyexchanger.exception.CurrencyNotFoundException;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ValidationException;
import com.nik.currencyexchanger.service.ExchangeRateService;
import com.nik.currencyexchanger.validation.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

        var codes = parseCurrencyPair(req);
        var exchangeRateDto = exchangeRateService.getExchangeRate(codes[0], codes[1]);
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
        var rateString = Arrays.stream(line.split("&"))
                .map(p -> p.split("=", 2))
                .filter(kv -> kv[0].equals("rate"))
                .map(kv -> URLDecoder.decode(kv[1], StandardCharsets.UTF_8))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Rate is missing"));
        var rate = Validator.validateRate(rateString);

        var requestDto = getExchangeRateRequestDto(req, rate);
        var exchangeRateDto = exchangeRateService.updateExchangeRate(requestDto);
        resp.setStatus(200);
        var jsonString = gson.toJson(exchangeRateDto);
        resp.getWriter()
                .write(jsonString);
    }

    private static String[] parseCurrencyPair(HttpServletRequest req) {
        var pathInfo = req.getPathInfo();
        var pair = Validator.validateCurrencyPair(pathInfo != null ? pathInfo.substring(1) : null);
        return new String[]{pair.substring(0, 3), pair.substring(3)};
    }

    private static ExchangeRateRequestDto getExchangeRateRequestDto(HttpServletRequest req, BigDecimal rate) {
        var codes = parseCurrencyPair(req);
        return new ExchangeRateRequestDto(codes[0], codes[1], rate);
    }
}
