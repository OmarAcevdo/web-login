package com.example.login.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UsuarioDto {

    private Long id;
    private String username;
    private String email;
    private boolean admin;

}
