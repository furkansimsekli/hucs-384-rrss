package com.fosskeeters.rrss.controllers;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.thymeleaf.exceptions.TemplateEngineException;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@ControllerAdvice
public class ErrorController implements Filter {
    public static String BOLD = "\033[1m";
    public static String RED = "\033[31m";
    public static String RESET = "\033[0m";

    @Bean
    public FilterRegistrationBean<ErrorController> addFilter() {
        FilterRegistrationBean<ErrorController> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(this);
        registrationBean.addUrlPatterns("*");

        return registrationBean;
    }

    public static void printException(Throwable ex) {
        System.out.println(BOLD + RED + "[ERROR] " + RESET + ex.getClass().getName() + ": " + BOLD
                           + ex.getMessage() + RESET);

        for (StackTraceElement ste : ex.getStackTrace()) {
            if (ste.getClassName().contains("fosskeeters")
                && !ste.getClassName().contains("ErrorController")) {
                System.out.println("    " + BOLD + ste.getFileName() + ":" + ste.getLineNumber()
                                   + RESET + " - " + ste.getClassName() + ":"
                                   + ste.getMethodName());
            }
        }

        System.out.println();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleExceptions(Exception ex) throws Exception {
        if (ex instanceof NoResourceFoundException
            || ex instanceof HttpRequestMethodNotSupportedException
            || ex instanceof ResponseStatusException) {
            throw ex;
        }

        printException(ex);
        return "error/5xx";
    }

    // A filter is required to catch Thymeleaf errors as @ExceptionHandler only catches exceptions
    // from controllers.
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            if (ex.getCause() instanceof TemplateEngineException) {
                printException(ex.getCause().getCause() != null ? ex.getCause().getCause()
                                                                : ex.getCause());
            }

            RequestDispatcher error = request.getRequestDispatcher("error");
            error.forward(request, response);
        }
    }
}
