package com.nik.currencyexchanger.filter;

import com.nik.currencyexchanger.exception.*;
import com.nik.currencyexchanger.util.ErrorSetter;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class ErrorFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try{
            filterChain.doFilter(servletRequest, servletResponse);
        }
        catch (DataBaseException e) {
            ErrorSetter.setError(((HttpServletResponse) servletResponse), 500, "DB error");
        }
        catch (CurrencyNotFoundException e){
            ErrorSetter.setError(((HttpServletResponse) servletResponse),404, "Currency not found");
        }
        catch (CurrencyAlreadyExistsException e) {
            ErrorSetter.setError(((HttpServletResponse) servletResponse), 409, "Currency with this code already exists");
        }
        catch (NumberFormatException e) {
            ErrorSetter.setError(((HttpServletResponse) servletResponse), 400, "Invalid rate format");
        }
        catch (ExchangeRateAlreadyExistsException e) {
            ErrorSetter.setError(((HttpServletResponse) servletResponse), 409, "Currency pair with this code already exists. ");
        }
        catch (ExchangeRateNotFoundException e){
            ErrorSetter.setError(((HttpServletResponse) servletResponse), 404, e.getMessage());
        }
    }
}
