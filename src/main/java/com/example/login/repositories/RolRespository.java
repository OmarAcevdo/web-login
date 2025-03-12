package com.example.login.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.login.model.entities.Rol;

public interface RolRespository extends JpaRepository<Rol, Long>{
    Optional<Rol> findByName(String name);
}
