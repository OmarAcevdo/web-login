package com.example.login.model.request;

import com.example.login.model.IUsuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequest implements IUsuario {

    @Size(min = 4, max = 10)
    private String username;

    @Email
    private String email;

    @Size(min = 8, max = 14)
    private String password;

    private Boolean admin; // Cambio a Boolean (permite null)
    
    @Override
    public boolean isAdmin() {
        return Boolean.TRUE.equals(admin); // Evita NullPointerException
    }
}
