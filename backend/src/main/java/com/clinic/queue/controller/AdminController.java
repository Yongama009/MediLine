package com.clinic.queue.controller;

import com.clinic.queue.dto.PatientRequest;
import com.clinic.queue.model.Patient;
import com.clinic.queue.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin(origins = "*")
public class AdminController {

    private final PatientService patientService;

    public AdminController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody PatientRequest request) {
        try {
            Patient admin = patientService.registerAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(admin);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
