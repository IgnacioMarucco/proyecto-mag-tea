package com.utn.magtea.config;

import com.utn.magtea.profesional.Profesional;
import com.utn.magtea.profesional.ProfesionalRepository;
import com.utn.magtea.profesional.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final ProfesionalRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin-email:admin@magtea.com}")
    private String adminEmail;

    @Value("${app.init.admin-password:magtea2026}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (repository.findByEmail(adminEmail).isEmpty()) {
            var admin = new Profesional();
            admin.setNombre("Admin");
            admin.setApellido("Sistema");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.INVESTIGADOR_PRINCIPAL);
            repository.save(admin);
        }
    }
}
