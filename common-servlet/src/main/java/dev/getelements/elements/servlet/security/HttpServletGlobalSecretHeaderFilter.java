package dev.getelements.elements.servlet.security;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.sdk.model.Headers;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class HttpServletGlobalSecretHeaderFilter implements Filter {

    private ServletFilterProcessor<ServletRequest, ServletResponse> processor = this::proceed;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(final ServletRequest _request,
                         final ServletResponse _response,
                         final FilterChain chain) throws IOException, ServletException {
        processor.process(_request, _response, chain);
    }

    private void proceed(final ServletRequest _request,
                         final ServletResponse _response,
                         final FilterChain chain) throws IOException, ServletException {
        chain.doFilter(_request, _response);
    }

    @Override
    public void destroy() {}

    @Inject
    public void initGlobalSecret(@Named(Constants.GLOBAL_SECRET) final String globalSecret) {
        processor = globalSecret.isBlank() ? this::proceed : (_request, _response, chain) -> {

            final var request = (HttpServletRequest) _request;
            final var response = (HttpServletResponse) _response;
            final var secret= request.getHeader(Headers.GLOBAL_SECRET);

            if (secret == null || !secret.trim().equals(globalSecret)) {
                response.sendError(SC_NOT_FOUND);
            } else {
                proceed(_request, _response, chain);
            }

        };
    }

}
