package com.example.login.Services.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.login.Services.UsuarioService;
import com.example.login.model.dto.UsuarioDto;
import com.example.login.model.dto.mapper.DtoMapperUsuario;
import com.example.login.model.dto.mapper.UsuarioMapper;
import com.example.login.model.entities.Rol;
import com.example.login.model.entities.Usuario;
import com.example.login.model.request.UsuarioRequest;
import com.example.login.repositories.RolRespository;
import com.example.login.repositories.UsuarioRepository;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private RolRespository rolRespository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDto> findAll() {
        List<Usuario> usuarios = repository.findAll();
        return usuarios.stream()
                .map(u -> DtoMapperUsuario.builder().setUser(u).build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioDto> findById(Long id) {
        return repository.findById(id)
                .map(u -> DtoMapperUsuario.builder().setUser(u).build());
    }

    @Override
    @Transactional
    public UsuarioDto save(UsuarioRequest usuarioRequest) {
        if (repository.findByUsername(usuarioRequest.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }
        if (repository.findByEmail(usuarioRequest.getEmail()).isPresent()) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }

        Usuario usuario = UsuarioMapper.toEntity(usuarioRequest);
        usuario.setUsername(usuarioRequest.getUsername());
        usuario.setEmail(usuarioRequest.getEmail());
        usuario.setPassword(passwordEncoder.encode(usuarioRequest.getPassword())); // Ahora sí toma la contraseña real
        usuario.setRoles(getRoles(usuarioRequest.isAdmin()));

        Usuario usuarioGuardado = repository.save(usuario);
        return DtoMapperUsuario.builder().setUser(usuarioGuardado).build();
    }

    @Override
    @Transactional
    public Optional<UsuarioDto> update(Long id, UsuarioRequest usuarioRequest, boolean isPatch) {
        Optional<Usuario> usuarioOpt = repository.findById(id);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            if (!isPatch) {
                if (usuarioRequest.getUsername() == null || usuarioRequest.getEmail() == null
                        || usuarioRequest.getPassword() == null) {
                    throw new IllegalArgumentException(
                            "Todos los campos son obligatorios para la actualización completa (PUT).");
                }
            }

            if (usuarioRequest.getUsername() != null) {
                usuario.setUsername(usuarioRequest.getUsername());
            }
            if (usuarioRequest.getEmail() != null) {
                usuario.setEmail(usuarioRequest.getEmail());
            }
            if (usuarioRequest.getPassword() != null && !usuarioRequest.getPassword().isEmpty()) {
                if (!usuarioRequest.getPassword().startsWith("$2a$")) { // Evitar doble hash si ya está encriptada
                    usuario.setPassword(passwordEncoder.encode(usuarioRequest.getPassword()));
                }
            }

            if (usuarioRequest.getAdmin() != null) {
                usuario.setRoles(getRoles(usuarioRequest.getAdmin()));
            }

            Usuario usuarioActualizado = repository.save(usuario);
            return Optional.of(DtoMapperUsuario.builder().setUser(usuarioActualizado).build());
        }
        return Optional.empty();
    }

    @Override
    public void remove(Long id) {
        repository.deleteById(id);
    }

    private List<Rol> getRoles(boolean isAdmin) {
        List<Rol> roles = new ArrayList<>();
        Optional<Rol> userRole = rolRespository.findByName("ROLE_USUARIO");
        userRole.ifPresent(roles::add);

        if (isAdmin) {
            Optional<Rol> adminRole = rolRespository.findByName("ROLE_ADMIN");
            adminRole.ifPresent(roles::add);
        }
        return roles;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(u -> DtoMapperUsuario.builder().setUser(u).build());
    }

}
