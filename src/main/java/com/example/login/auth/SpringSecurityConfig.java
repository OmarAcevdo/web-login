package com.example.login.auth;

import java.util.Arrays;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.example.login.Services.RefreshTokenService;
import com.example.login.auth.filters.JwtAuthenticationFilter;
import com.example.login.auth.filters.JwtValidationFilter;
import com.example.login.repositories.UsuarioRepository;
@Configuration
public class SpringSecurityConfig {


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService, UsuarioRepository usuarioRepository) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->  auth
                    .requestMatchers(HttpMethod.GET, "/api/v1/usuarios").permitAll()
                    .requestMatchers(HttpMethod.POST, "/login").permitAll()

                    .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/{id}").hasAnyRole("USUARIO", "ADMIN")
                    
                    .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/registro").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/roles").permitAll()

                    .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/{id}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/usuarios/{id}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/{id}").hasRole("ADMIN")

                    .anyRequest().authenticated()
                )
                .addFilter(new JwtAuthenticationFilter(authenticationManager, refreshTokenService, usuarioRepository))
                .addFilter(new JwtValidationFilter(authenticationManager))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    FilterRegistrationBean<CorsFilter> corsFilter(){
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<CorsFilter>(new CorsFilter(corsConfigurationSource()));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

}