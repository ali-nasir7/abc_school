package com.backend.Abroad_School.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class SimpleAuthInterceptor implements HandlerInterceptor {

    private final String USERNAME;
    private final String PASSWORD;

    public SimpleAuthInterceptor(
            @org.springframework.beans.factory.annotation.Value("${app.security.username}") String username,
            @org.springframework.beans.factory.annotation.Value("${app.security.password}") String password
    ) {
        this.USERNAME = username;
        this.PASSWORD = password;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String auth = request.getHeader("Authorization");

        if (auth == null || !auth.startsWith("Basic ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing Authorization");
            return false;
        }

        String base64 = auth.substring(6);
        String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);

        String[] parts = decoded.split(":", 2);
        if (parts.length != 2) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Authorization");
            return false;
        }

        String username = parts[0];
        String password = parts[1];

        if (!USERNAME.equals(username) || !PASSWORD.equals(password)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Credentials");
            return false;
        }

        return true; 
    }
}
