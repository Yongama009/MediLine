package com.clinic.queue.service;

import com.clinic.queue.dto.PatientRequest;
import com.clinic.queue.exception.ResourceNotFoundException;
import com.clinic.queue.model.Patient;
import com.clinic.queue.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    @Value("${admin.registration.code:ADMIN_SECRET_2024}")
    private String adminRegistrationCode;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient registerPatient(PatientRequest request) {
        // Check if user wants to register as admin
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("admin")) {
            // Validate admin code
            if (request.getAdminCode() == null || request.getAdminCode().isBlank()) {
                throw new IllegalArgumentException("Admin registration code is required to register as administrator.");
            }
            if (!request.getAdminCode().equals(adminRegistrationCode)) {
                throw new IllegalArgumentException("Invalid admin registration code.");
            }
            request.setRole("ADMIN");
        } else {
            request.setRole("PATIENT");
        }
        Patient patient = buildPatient(request);
        return patientRepository.save(patient);
    }

    public Patient registerAdmin(PatientRequest request) {
        request.setRole("ADMIN");
        Patient patient = buildPatient(request);
        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public List<Patient> searchPatients(String query) {
        if (query == null || query.isBlank()) {
            return patientRepository.findAll();
        }
        return patientRepository.findByNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(query, query);
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    public Patient updatePatient(Long id, PatientRequest request) {
        Patient patient = getPatientById(id);
        applyPatientChanges(patient, request);
        return patientRepository.save(patient);
    }

    public void deletePatient(Long id) {
        Patient patient = getPatientById(id);
        patientRepository.delete(patient);
    }

    private Patient buildPatient(PatientRequest request) {
        Patient patient = new Patient();
        patient.setRole(request.getRole() == null ? "PATIENT" : request.getRole().trim().toUpperCase());
        applyPatientChanges(patient, request);
        return patient;
    }

    private void applyPatientChanges(Patient patient, PatientRequest request) {
        patient.setName(request.getName().trim());
        patient.setEmail(request.getEmail().trim());
        patient.setPhone(request.getPhone().trim());
        patient.setPassword(request.getPassword());
        patient.setNotes(request.getNotes() == null ? null : request.getNotes().trim());
    }
}
