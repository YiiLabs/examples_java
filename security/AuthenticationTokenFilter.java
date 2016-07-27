package com.yiilab.inside.security;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AuthenticationTokenFilter extends GenericFilterBean {

    private AuthenticationManager authenticationManager;

    public AuthenticationTokenFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        final String token = httpRequest.getHeader("X-Token");
        if (token != null) {
            final Authentication resultOfAuthentication = new PreAuthenticatedAuthenticationToken(token, null);
            final Authentication responseAuthentication = authenticationManager.authenticate(resultOfAuthentication);
            logger.info("AuthenticationTokenFilter ====== " + token);
            if (responseAuthentication == null || !responseAuthentication.isAuthenticated()) {
                throw new InternalAuthenticationServiceException("Unable to authenticate Domain User for provided credentials");
            }
            logger.debug("User successfully authenticated");
            SecurityContextHolder.getContext().setAuthentication(responseAuthentication);
        }

        chain.doFilter(request, response);
    }
}
