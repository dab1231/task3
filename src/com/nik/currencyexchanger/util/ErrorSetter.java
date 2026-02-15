package com.nik.currencyexchanger.util;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.Map;

@UtilityClass
public class ErrorSetter {
    private final Gson gson = new Gson();

    public void setError(HttpServletResponse resp, int status, String messageValue) throws IOException {
        StatusSetter.setHeadersAndStatus(resp, status);
        String errorJson = gson.toJson(Map.of("message", messageValue));
        resp.getWriter()
                .write(errorJson);
    }
}
