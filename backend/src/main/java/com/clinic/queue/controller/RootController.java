package com.clinic.queue.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(
                Map.of(
                        "status", "MediLine backend is running",
                        "patients", "/api/patients",
                        "patientById", "/api/patients/{id}"));
    }
}
