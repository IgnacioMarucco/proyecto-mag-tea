package com.utn.magtea.auth;

import com.utn.magtea.profesional.ProfesionalResponseDTO;
import com.utn.magtea.profesional.ProfesionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ProfesionalService profesionalService;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        ProfesionalResponseDTO profesional = profesionalService.findByEmail(request.email());
        String token = jwtUtil.generateToken(request.email(), profesional.role().name());
        return new LoginResponse(token, profesional.email(), profesional.role().name());
    }
}
