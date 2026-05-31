package com.utn.magtea.auth;

import com.utn.magtea.profesional.ProfesionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ProfesionalRepository repository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var profesional = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Profesional no encontrado: " + email));

        return User.builder()
                .username(profesional.getEmail())
                .password(profesional.getPassword())
                .roles(profesional.getRole().name())
                .disabled(!profesional.isActivo())
                .build();
    }
}
