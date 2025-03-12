package com.example.login.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import com.example.login.model.entities.Rol;
import com.example.login.repositories.RolRespository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initRoles(RolRespository rolRepository) {
        return args -> {
            createRoleIfNotExists(rolRepository, "ROLE_ADMIN");
            createRoleIfNotExists(rolRepository, "ROLE_USUARIO");
        };
    }

    @Transactional
    private void createRoleIfNotExists(RolRespository rolRepository, String roleName) {
        if (rolRepository.findByName(roleName).isEmpty()) {
            Rol role = new Rol();
            role.setName(roleName);
            rolRepository.save(role);
            System.out.println("Rol creado: " + roleName);
        }
    }
}
