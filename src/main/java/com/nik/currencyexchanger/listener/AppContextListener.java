package com.nik.currencyexchanger.listener;

import com.google.gson.Gson;
import com.nik.currencyexchanger.dao.CurrencyDao;
import com.nik.currencyexchanger.dao.ExchangeRateDao;
import com.nik.currencyexchanger.service.CurrencyService;
import com.nik.currencyexchanger.service.ExchangeRateService;
import com.nik.currencyexchanger.service.ExchangeService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        CurrencyDao currencyDao = new CurrencyDao();
        ExchangeRateDao exchangeRateDao = new ExchangeRateDao();
        Gson gson = new Gson();

        CurrencyService currencyService = new CurrencyService(currencyDao);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDao, currencyService, currencyDao);
        ExchangeService exchangeService = new ExchangeService(exchangeRateDao, currencyService);
        sce.getServletContext().setAttribute("currencyService", currencyService);
        sce.getServletContext().setAttribute("exchangeRateService", exchangeRateService);
        sce.getServletContext().setAttribute("exchangeService", exchangeService);
        sce.getServletContext().setAttribute("gson", gson);
    }
}
