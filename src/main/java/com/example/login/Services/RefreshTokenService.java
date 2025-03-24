package com.example.login.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.login.model.entities.RefreshToken;
import com.example.login.model.entities.Usuario;
import com.example.login.repositories.RefreshTokenRepository;
import com.example.login.repositories.UsuarioRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

@Service
public class RefreshTokenService {

    public final static SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();
    private final static long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 días

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método para generar un Refresh Token como JWT
    public String generateRefreshToken(Usuario usuario) {
        return Jwts.builder()
                .subject(usuario.getId().toString())
                .signWith(SECRET_KEY) // Cambia esto por tu clave secreta
                .issuedAt(new Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .compact();
    }

    public RefreshToken createRefreshToken(Long id) {
        String username = usuarioRepository.findById(id).orElseThrow().getUsername();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        String refreshToken = generateRefreshToken(usuario);

        RefreshToken token = new RefreshToken();
        token.setUsuario(usuarioRepository.findById(id).orElseThrow());
        token.setToken(refreshToken); // Asignamos el JWT generado
        token.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION));

        return refreshTokenRepository.save(token);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUsuarioId(userId);
    }
}
