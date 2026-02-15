package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ExchangeRateNotFoundException;
import com.nik.currencyexchanger.service.ExchangeRateService;
import com.nik.currencyexchanger.util.ErrorSetter;
import com.nik.currencyexchanger.util.StatusSetter;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ExchangeRateServlet extends HttpServlet {

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var pathInfo = req.getPathInfo();
            var baseAndTargetCode = pathInfo.substring(1);

            if(baseAndTargetCode.length() != 6){
                ErrorSetter.setError(resp, 400, "Currency codes for the pair are missing from the address ");
            }

            var targetCode = baseAndTargetCode.substring(3);
            var baseCode = baseAndTargetCode.substring(0, 3);
            var exchangeRateDto = exchangeRateService.getExchangeRate(baseCode, targetCode);
            StatusSetter.setHeadersAndStatus(resp, 200);
            var jsonString = gson.toJson(exchangeRateDto);
            resp.getWriter()
                    .write(jsonString);
        }
        catch (ExchangeRateNotFoundException e) {
            ErrorSetter.setError(resp, 404, "Exchange rate for the pair not found");
        }
        catch (DataBaseException e){
            ErrorSetter.setError(resp, 500, "DB error");
        }
    }
}
