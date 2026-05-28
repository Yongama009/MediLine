package com.clinic.queue.service;

import com.clinic.queue.dto.PatientRequest;
import com.clinic.queue.exception.ResourceNotFoundException;
import com.clinic.queue.model.Patient;
import com.clinic.queue.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient registerPatient(PatientRequest request) {
        Patient patient = buildPatient(request);
        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
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
        applyPatientChanges(patient, request);
        return patient;
    }

    private void applyPatientChanges(Patient patient, PatientRequest request) {
        patient.setName(request.getName().trim());
        patient.setPhone(request.getPhone().trim());
        patient.setNotes(request.getNotes() == null ? null : request.getNotes().trim());
    }
}
