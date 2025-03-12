package com.example.login.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.login.model.dto.UsuarioDto;
import com.example.login.model.request.UsuarioRequest;

public interface UsuarioService {
    
    List<UsuarioDto> findAll();
    
    Optional<UsuarioDto> findById(Long id);

    UsuarioDto save(UsuarioRequest usuarioRequest);

    Optional<UsuarioDto> update(Long id, UsuarioRequest usuarioRequest, boolean isPatch);

    void remove(Long id);

    Page<UsuarioDto> findAll(Pageable pageable);
    

}
