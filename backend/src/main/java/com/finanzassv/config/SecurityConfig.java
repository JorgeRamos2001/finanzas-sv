package com.finanzassv.config;

import com.finanzassv.exception.ErrorResponse;
import com.finanzassv.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Configuracion de seguridad: API stateless con JWT y roles
 * ADMIN, CONTADOR y CONSULTA.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Respuesta JSON 401 cuando no se envia token valido. */
    @Bean
    public AuthenticationEntryPoint puntoEntradaNoAutenticado() {
        return (request, response, ex) -> escribirError(response, 401,
                "No autorizado", "Se requiere un token JWT valido");
    }

    /** Respuesta JSON 403 cuando el rol no tiene permisos. */
    @Bean
    public AccessDeniedHandler manejadorAccesoDenegado() {
        return (request, response, ex) -> escribirError(response, 403,
                "Acceso denegado", "No tiene permisos para realizar esta operacion");
    }

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(origenesPermitidos()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Autenticacion
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // Usuarios: solo ADMIN
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        // Cuentas: lectura para todos los roles, escritura solo ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/cuentas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/cuentas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/cuentas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/cuentas/**").hasRole("ADMIN")
                        // Asientos: registro/anulacion para ADMIN y CONTADOR, lectura todos
                        .requestMatchers(HttpMethod.POST, "/api/asientos/**")
                        .hasAnyRole("ADMIN", "CONTADOR")
                        // Reportes: lectura para todos los roles
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(puntoEntradaNoAutenticado())
                        .accessDeniedHandler(manejadorAccesoDenegado()))
                .addFilterBefore(jwtAuthenticationFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** CORS para el frontend Vue en desarrollo (Vite 5173). */
    @Bean
    public CorsConfigurationSource origenesPermitidos() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:4173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);
        return fuente;
    }

    private void escribirError(HttpServletResponse response, int status, String error, String mensaje)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                ErrorResponse.of(status, error, mensaje));
    }
}
