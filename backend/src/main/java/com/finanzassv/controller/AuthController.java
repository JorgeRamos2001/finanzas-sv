package com.finanzassv.controller;

import com.finanzassv.dto.auth.LoginRequest;
import com.finanzassv.dto.auth.TokenResponse;
import com.finanzassv.dto.auth.UsuarioResponse;
import com.finanzassv.security.UsuarioPrincipal;
import com.finanzassv.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Autenticacion de usuarios (login y perfil).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UsuarioResponse perfil(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        return authService.perfilActual(usuario);
    }
}
