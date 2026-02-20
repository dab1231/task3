package com.nik.currencyexchanger.filter;

import com.google.gson.Gson;
import com.nik.currencyexchanger.exception.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@WebFilter("/*")
public class ErrorFilter implements Filter {

    private final Gson gson = new Gson();

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8);
        String errorJson = gson.toJson(Map.of("message", message));
        response.getWriter()
                .write(errorJson);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        var httpResp = (HttpServletResponse) servletResponse;
        try{
            filterChain.doFilter(servletRequest, servletResponse);
        }
        catch (DataBaseException e) {
            sendError( httpResp, 500, "DB error");
        }
        catch (CurrencyNotFoundException e){
            sendError(httpResp,404, "Currency not found");
        }
        catch (CurrencyAlreadyExistsException e) {
            sendError(httpResp, 409, "Currency with this code already exists");
        }
        catch (NumberFormatException e) {
            sendError(httpResp, 400, "Invalid rate format");
        }
        catch (ExchangeRateAlreadyExistsException e) {
            sendError(httpResp, 409, "Currency pair with this code already exists. ");
        }
        catch (ExchangeRateNotFoundException e){
            sendError(httpResp, 404, e.getMessage());
        }
    }
}
