package com.utn.magtea.auth;

import com.utn.magtea.profesional.ProfesionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ProfesionalRepository repository;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        var profesional = repository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Profesional no encontrado"));
        String token = jwtUtil.generateToken(request.email(), profesional.getRole().name());
        return new LoginResponse(token, profesional.getEmail(), profesional.getRole().name());
    }
}
