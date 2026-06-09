package com.clinic.queue.controller;

import com.clinic.queue.dto.LoginRequest;
import com.clinic.queue.dto.LoginResponse;
import com.clinic.queue.model.Patient;
import com.clinic.queue.repository.PatientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(PatientRepository patientRepository, PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail() == null ? null : request.getEmail().trim();
        String rawPassword = request.getPassword();
        Optional<Patient> patientOpt = patientRepository.findByEmailIgnoreCase(email);

        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new LoginResponse(null, "Invalid email or password", null, null));
        }

        Patient patient = patientOpt.get();

        String storedPassword = patient.getPassword();
        boolean matches = false;

        if (storedPassword != null && storedPassword.startsWith("$2")) {
            matches = passwordEncoder.matches(rawPassword, storedPassword);
        } else {
            // legacy plain-text password
            if (storedPassword != null && storedPassword.equals(rawPassword)) {
                matches = true;
                try {
                    patient.setPassword(passwordEncoder.encode(rawPassword));
                    patientRepository.save(patient);
                } catch (Exception ex) {
                    // migration failure shouldn't block login
                }
            }
        }

        if (!matches) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new LoginResponse(null, "Invalid email or password", null, null));
        }

        // Generate a simple token (in production, use JWT)
        String token = "token_" + patient.getId() + "_" + System.currentTimeMillis();

        return ResponseEntity.ok(
                new LoginResponse(token, "Login successful", patient.getId(), patient.getName()));
    }
}
