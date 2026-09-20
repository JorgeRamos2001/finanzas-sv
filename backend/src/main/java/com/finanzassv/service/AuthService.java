package com.finanzassv.service;

import com.finanzassv.dto.auth.LoginRequest;
import com.finanzassv.dto.auth.TokenResponse;
import com.finanzassv.dto.auth.UsuarioResponse;
import com.finanzassv.entity.Usuario;
import com.finanzassv.exception.RecursoNoEncontradoException;
import com.finanzassv.exception.ReglaNegocioException;
import com.finanzassv.repository.UsuarioRepository;
import com.finanzassv.security.JwtService;
import com.finanzassv.security.UsuarioPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Autenticacion: valida credenciales y emite tokens JWT.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public TokenResponse login(LoginRequest request) {
        var usuario = usuarioRepository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new BadCredentialsException("credenciales invalidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ReglaNegocioException("El usuario esta desactivado. Contacte al administrador.");
        }
        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BadCredentialsException("credenciales invalidas");
        }
        return new TokenResponse(
                jwtService.generarToken(aPrincipal(usuario)),
                "Bearer",
                expiracionMs(),
                aRespuesta(usuario));
    }

    public UsuarioResponse perfilActual(UsuarioPrincipal comoRecord) {
        var usuario = usuarioRepository.findById(comoRecord.id())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return aRespuesta(usuario);
    }

    public UsuarioResponse aRespuesta(com.finanzassv.entity.Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(),
                usuario.getNombreCompleto(), usuario.getRol(), usuario.getActivo());
    }

    public UsuarioPrincipal aPrincipal(com.finanzassv.entity.Usuario usuario) {
        return new UsuarioPrincipal(usuario.getId(), usuario.getUsername(),
                usuario.getRol().name(), usuario.getNombreCompleto());
    }

    public long expiracionMs() {
        return jwtService.expiracionMs();
    }
}
