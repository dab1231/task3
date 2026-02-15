package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.service.CurrencyService;
import com.nik.currencyexchanger.util.ErrorSetter;
import com.nik.currencyexchanger.util.StatusSetter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var currenciesDto = currencyService.getAllCurrencies();
            String jsonString = gson.toJson(currenciesDto);

            StatusSetter.setHeadersAndStatus(resp, 200);
            resp.getWriter()
                .write(jsonString);

        } catch (DataBaseException e) {
            ErrorSetter.setError(resp, 500, "DB error");
        }
    }

}
