package com.example.login.Services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.login.model.entities.Usuario;
import com.example.login.repositories.UsuarioRepository;

@Service
public class JpaUserDetailService implements UserDetailsService {

        
        @Autowired
        private final UsuarioRepository usuarioRepository;

        public JpaUserDetailService(UsuarioRepository usuarioRepository) {
                this.usuarioRepository = usuarioRepository;
        }

        @Override
        @Transactional(readOnly = true)
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                Usuario usuario = usuarioRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

                if (usuario.getId() == null) {
                        throw new RuntimeException("El usuario recuperado de la base de datos tiene ID nulo");
                }

                List<GrantedAuthority> authorities = usuario.getRoles()
                                .stream()
                                .map(r -> new SimpleGrantedAuthority(r.getName()))
                                .collect(Collectors.toList());

                                return new User(usuario.getUsername(), usuario.getPassword(), authorities);
        }

}
