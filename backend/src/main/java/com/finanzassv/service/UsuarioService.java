package com.finanzassv.service;

import com.finanzassv.dto.auth.RegistroRequest;
import com.finanzassv.dto.auth.UsuarioResponse;
import com.finanzassv.entity.Usuario;
import com.finanzassv.exception.RecursoNoEncontradoException;
import com.finanzassv.exception.ReglaNegocioException;
import com.finanzassv.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion de usuarios del sistema (solo ADMIN).
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional
    public UsuarioResponse crear(RegistroRequest request) {
        if (usuarioRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ReglaNegocioException("El nombre de usuario ya existe: " + request.username());
        }
        var usuario = Usuario.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .nombreCompleto(request.nombreCompleto())
                .rol(request.rol())
                .activo(request.activo() == null || request.activo())
                .build();
        return aRespuesta(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, RegistroRequest request) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));

        if (!usuario.getUsername().equalsIgnoreCase(request.username())
                && usuarioRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ReglaNegocioException("El nombre de usuario ya existe: " + request.username());
        }
        usuario.setUsername(request.username());
        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setRol(request.rol());
        if (request.activo() != null) {
            usuario.setActivo(request.activo());
        }
        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < 6) {
                throw new ReglaNegocioException("La contrasena debe tener al menos 6 caracteres");
            }
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        return aRespuesta(usuarioRepository.save(usuario));
    }

    @Transactional
    public void desactivar(Long id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new ReglaNegocioException("El usuario ya esta desactivado");
        }
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    private UsuarioResponse aRespuesta(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(),
                usuario.getNombreCompleto(), usuario.getRol(), usuario.getActivo());
    }
}
