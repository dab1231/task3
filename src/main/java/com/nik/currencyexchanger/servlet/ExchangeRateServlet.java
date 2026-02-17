package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.exception.ExchangeRateNotFoundException;
import com.nik.currencyexchanger.service.ExchangeRateService;
import com.nik.currencyexchanger.util.ErrorSetter;
import com.nik.currencyexchanger.util.StatusSetter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var pathInfo = req.getPathInfo();

            if(pathInfo == null){
                ErrorSetter.setError(resp, 400, "Currency pair is missing");
                return;
            }
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

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (req.getMethod().equalsIgnoreCase("PATCH")) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var reader = req.getReader();
            var line = reader.readLine();
            var rateString = line.substring(5);
            if(rateString.isBlank()){
                ErrorSetter.setError(resp, 400, "The required form field is missing.");
                return;
            }
            BigDecimal rate = new BigDecimal(rateString);


            var pathInfo = req.getPathInfo();
            if(pathInfo == null){
                ErrorSetter.setError(resp, 400, "Currency pair is missing");
                return;
            }
            var baseAndTargetCode = pathInfo.substring(1);

            if(baseAndTargetCode.length() != 6){
                ErrorSetter.setError(resp, 400, "Currency codes for the pair are missing from the address");
                return;
            }
            var targetCode = baseAndTargetCode.substring(3);
            var baseCode = baseAndTargetCode.substring(0, 3);

            var exchangeRateDto = exchangeRateService.updateExchangeRate(baseCode, targetCode, rate);
            StatusSetter.setHeadersAndStatus(resp, 200);
            var jsonString = gson.toJson(exchangeRateDto);
            resp.getWriter()
                    .write(jsonString);
        } catch (NumberFormatException e) {
            ErrorSetter.setError(resp, 400, "Invalid rate format");
        } catch (ExchangeRateNotFoundException e){
            ErrorSetter.setError(resp, 404, "Currency pair not found in database");
        } catch (DataBaseException e){
            ErrorSetter.setError(resp, 500, "DB error");
        }
    }
}
