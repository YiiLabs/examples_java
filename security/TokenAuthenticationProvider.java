package com.yiilab.inside.security;

import com.yiilab.inside.dao.user.TokenRepository;
import com.yiilab.inside.model.user.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class TokenAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private TokenRepository tokenRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String token = (String) authentication.getPrincipal();

        if (token == null) {
            throw new BadCredentialsException("Invalid token");
        }
        Token t = tokenRepository.findByToken(token);
        if (t == null) {
            throw new BadCredentialsException("Invalid token or token expired");
        }
        return new PreAuthenticatedAuthenticationToken(token, t, t.getUser().getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
