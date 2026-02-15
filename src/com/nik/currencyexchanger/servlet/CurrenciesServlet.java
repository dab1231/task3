package com.nik.currencyexchanger.servlet;

import com.google.gson.Gson;
import com.nik.currencyexchanger.exception.DataBaseException;
import com.nik.currencyexchanger.service.CurrencyService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;


@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {

    private final CurrenciesServlet INSTANCE = new CurrenciesServlet();
    private final CurrencyService currencyService = CurrencyService.getInstance();
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var currenciesDto = currencyService.getAllCurrencies();
            String jsonString = gson.toJson(currenciesDto);
            resp.setStatus(200);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter()
                .write(jsonString);

        } catch (DataBaseException e) {
            resp.setStatus(500);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            String errorJson = gson.toJson(Map.of("message", "DB error"));
            resp.getWriter()
                    .write(errorJson);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
