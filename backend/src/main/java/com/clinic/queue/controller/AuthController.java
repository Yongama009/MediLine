package com.clinic.queue.controller;

import com.clinic.queue.dto.LoginRequest;
import com.clinic.queue.dto.LoginResponse;
import com.clinic.queue.model.Patient;
import com.clinic.queue.repository.PatientRepository;
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

    public AuthController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Optional<Patient> patientOpt = patientRepository.findByEmail(request.getEmail());

        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new LoginResponse(null, "Invalid email or password", null, null));
        }

        Patient patient = patientOpt.get();

        // Simple password comparison (in production, use BCrypt)
        if (!patient.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new LoginResponse(null, "Invalid email or password", null, null));
        }

        // Generate a simple token (in production, use JWT)
        String token = "token_" + patient.getId() + "_" + System.currentTimeMillis();

        return ResponseEntity.ok(
                new LoginResponse(token, "Login successful", patient.getId(), patient.getName()));
    }
}
