package com.github.sibmaks.springdocsrender.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Order(value = Ordered.HIGHEST_PRECEDENCE)
@Component
@ConditionalOnProperty(value = "app.enable.incoming.log", havingValue = "true")
@WebFilter(filterName = "RequestCachingFilter", urlPatterns = "/*")
public class RequestCachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        long timing = System.currentTimeMillis() - startTime;
        logger.info(
                String.format("Request [%s] to '%s' executed for %d ms",
                        request.getMethod(),
                        request.getRequestURL(),
                        timing
                )
        );
    }
}