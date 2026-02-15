package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.dto.CurrencyDto;
import com.nik.currencyexchanger.exception.CurrencyNotFoundException;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.service.CurrencyService;
import com.nik.currencyexchanger.util.ErrorSetter;
import com.nik.currencyexchanger.util.StatusSetter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;


@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final Gson gson = new Gson();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var pathInfo = req.getPathInfo();

            if(pathInfo == null || pathInfo.length() <= 1){
                StatusSetter.setHeadersAndStatus(resp, 400);
                var errorJson = gson.toJson(Map.of("message", "Currency code is missing from the address"));
                resp.getWriter()
                        .write(errorJson);
                return;
            }

            var code = pathInfo.substring(1);
            var currencyDto = currencyService.getCurrencyByCode(code);

            StatusSetter.setHeadersAndStatus(resp, 200);
            var jsonString = gson.toJson(currencyDto);
            resp.getWriter()
                    .write(jsonString);
        }
        catch (DataBaseException e) {
            ErrorSetter.setError(resp, 500, "DB error");
        }
        catch (CurrencyNotFoundException e){
            ErrorSetter.setError(resp,404, "Currency not found");
        }
    }
}
