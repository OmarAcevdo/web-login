package com.example.login.auth.filters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.example.login.auth.SimpleGrantedAuthorityJsonCreator;
import com.example.login.auth.TokenJwtConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtValidationFilter extends BasicAuthenticationFilter{
    public JwtValidationFilter(AuthenticationManager autenticacionManager){
        super(autenticacionManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException{
        String header = request.getHeader(TokenJwtConfig.HEADER_AUTHORIZATION);

        if (header == null || !header.startsWith(TokenJwtConfig.PREFIX_TOKEN)) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.replace(TokenJwtConfig.PREFIX_TOKEN, "");

        try {
            Claims claims = Jwts.parser()
                            .verifyWith(TokenJwtConfig.SECRET_KEY)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
            boolean isAdmin = Optional.ofNullable(claims.get("isAdmin", Boolean.class)).orElse(false);
            String username = claims.get("username", String.class);
            String userId = claims.getSubject(); // ID del usuario

            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(isAdmin ? "ROLE_ADMIN" : "ROLE_USER"));

            Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);


        } catch (Exception e) {
            Map<String, String> body  = new HashMap<>();
            body.put("error", e.getMessage());
            body.put("message", "El token JWT no es valido");
            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            response.setStatus(401);
            response.setContentType("application/json");
        }
    }
}
