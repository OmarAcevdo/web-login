package com.example.login.auth.filters;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.login.Services.RefreshTokenService;
import com.example.login.auth.TokenJwtConfig;
import com.example.login.model.entities.RefreshToken;
import com.example.login.model.entities.Usuario;
import com.example.login.repositories.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService,
            UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            Usuario usuario = new ObjectMapper().readValue(request.getInputStream(), Usuario.class);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    usuario.getUsername(), usuario.getPassword());
            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo la solicitud", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        String username = ((User) authResult.getPrincipal()).getUsername();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();
        boolean isAdmin = roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));


        String token = Jwts.builder()
                .claim("isAdmin", isAdmin)
                .claim("username", username)

                .subject(usuario.getId().toString())
                .signWith(TokenJwtConfig.SECRET_KEY)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // Expira en 15 min
                .compact();

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario.getId());

        response.addHeader(TokenJwtConfig.HEADER_AUTHORIZATION, TokenJwtConfig.PREFIX_TOKEN + token);

        Map<String, Object> body = new HashMap<>();
        body.put("message", String.format("Bienvenido %s, has iniciado sesión con éxito!", username));
        body.put("accessToken", token);
        body.put("refreshToken", refreshToken.getToken());
        body.put("expiresIn", 900); // 900 segundos = 15 min

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setStatus(200);
        response.setContentType("application/json");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        Map<String, Object> body = new HashMap<>();
        body.put("message", "Credenciales incorrectas. Por favor, verifica e intenta de nuevo.");
        body.put("error", "Autenticación fallida");

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setStatus(401);
        response.setContentType("application/json");
    }

}
