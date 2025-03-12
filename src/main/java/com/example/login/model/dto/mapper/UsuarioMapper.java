package com.example.login.model.dto.mapper;

import org.springframework.stereotype.Component;

import com.example.login.model.dto.UsuarioDto;
import com.example.login.model.entities.Usuario;
import com.example.login.model.request.UsuarioRequest;

@Component
public class UsuarioMapper {
    public static UsuarioDto toDto(Usuario usuario) {
        return new UsuarioDto(usuario.getId(), usuario.getUsername(), usuario.getEmail(), 
                              usuario.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName())));
    }

    public static Usuario toEntity(UsuarioRequest usuarioRequest) {
        Usuario usuario = new Usuario();
        usuario.setUsername(usuarioRequest.getUsername());
        usuario.setEmail(usuarioRequest.getEmail());
        usuario.setPassword(usuarioRequest.getPassword());
        return usuario;
    }
}
