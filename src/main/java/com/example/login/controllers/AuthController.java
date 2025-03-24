package com.example.login.controllers;

import com.example.login.Services.RefreshTokenService;
import com.example.login.auth.TokenJwtConfig;
import com.example.login.model.entities.RefreshToken;
import com.example.login.model.entities.Usuario;
import com.example.login.repositories.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request, Authentication authResult) {
        String refreshToken = request.get("refreshToken");

        String username = ((User) authResult.getPrincipal()).getUsername();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();
        boolean isAdmin = roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        return refreshTokenService.findByToken(refreshToken)
                .map(token -> {
                    // Verifica si el Refresh Token ha expirado
                    if (token.getExpiryDate().isBefore(new Date().toInstant())) {
                        return ResponseEntity.badRequest().body(Map.of("message", "Refresh token expirado"));
                    }

                    // Crear un nuevo Access Token
                    String newAccessToken = Jwts.builder()
                            .claim("username", username)
                            .claim("isAdmin", isAdmin)
                            .subject(usuario.getId().toString())
                            .claim("isAdmin", isAdmin)
                            .signWith(TokenJwtConfig.SECRET_KEY)
                            .issuedAt(new Date())
                            .expiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 minutos de
                                                                                               // expiración
                            .compact();

                    // Crear un nuevo Refresh Token como JWT (lo mismo que en la creación del access
                    // token)
                    String newRefreshToken = refreshTokenService.generateRefreshToken(usuario);

                    return ResponseEntity.ok(Map.of(
                            "accessToken", newAccessToken,
                            "refreshToken", newRefreshToken));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of("message", "Refresh token inválido")));
    }
}
