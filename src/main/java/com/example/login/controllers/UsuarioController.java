package com.example.login.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.login.Services.UsuarioService;
import com.example.login.model.dto.UsuarioDto;
import com.example.login.model.request.UsuarioRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/usuarios")
@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    @GetMapping("/all")
    public List<UsuarioDto> listAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> show(@PathVariable Long id) {
        Optional<UsuarioDto> usuario = service.findById(id);
        return usuario.isPresent() ? ResponseEntity.ok(usuario.get()) : ResponseEntity.notFound().build();
    }

    @PostMapping("/registro")
    public ResponseEntity<?> create(@Valid @RequestBody UsuarioRequest usuarioRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }
        UsuarioDto usuarioCreado = service.save(usuarioRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCreado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UsuarioRequest usuarioRequest,
            BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        }

        Optional<UsuarioDto> usuarioActualizado = service.update(id, usuarioRequest, false);
        return usuarioActualizado.isPresent()
                ? ResponseEntity.ok(usuarioActualizado.get())
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateUser(@PathVariable Long id, @RequestBody UsuarioRequest usuarioRequest) {
        Optional<UsuarioDto> usuarioActualizado = service.update(id, usuarioRequest, true);
        return usuarioActualizado.isPresent()
                ? ResponseEntity.ok(usuarioActualizado.get())
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<UsuarioDto> usuario = service.findById(id);
        if (usuario.isPresent()) {
            service.remove(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public Page<UsuarioDto> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.findAll(PageRequest.of(page, size));
    }

}
