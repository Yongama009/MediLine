package com.clinic.queue.service;

import com.clinic.queue.dto.AppointmentRequest;
import com.clinic.queue.dto.AppointmentResponse;
import com.clinic.queue.exception.ResourceNotFoundException;
import com.clinic.queue.model.Appointment;
import com.clinic.queue.model.Patient;
import com.clinic.queue.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientService patientService) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
    }

    public AppointmentResponse scheduleAppointment(AppointmentRequest request) {
        Patient patient = patientService.getPatientById(request.getPatientId());

        Appointment appointment = new Appointment();
        appointment.setPatientId(patient.getId());
        appointment.setReason(request.getReason() == null ? null : request.getReason().trim());
        appointment.setStatus("PENDING");

        String scheduledAtStr = request.getScheduledAt();
        if (scheduledAtStr != null && scheduledAtStr.length() == 16) {
            scheduledAtStr += ":00";
        }
        appointment.setScheduledAt(LocalDateTime.parse(scheduledAtStr));

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return toResponse(savedAppointment, patient.getName());
    }

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAllByOrderByScheduledAtAsc().stream()
                .map(appointment -> {
                    String patientName = "Unknown Patient";
                    try {
                        Patient patient = patientService.getPatientById(appointment.getPatientId());
                        patientName = patient.getName();
                    } catch (ResourceNotFoundException e) {
                        // Patient record might have been removed
                    }
                    return toResponse(appointment, patientName);
                })
                .collect(Collectors.toList());
    }

    public void confirmAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        appointment.setStatus("CONFIRMED");
        appointmentRepository.save(appointment);
    }

    public void startProgress(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        appointment.setStatus("IN_PROGRESS");
        appointmentRepository.save(appointment);
    }

    public void completeAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        appointment.setStatus("COMPLETED");
        appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        appointmentRepository.delete(appointment);
    }

    private AppointmentResponse toResponse(Appointment appointment, String patientName) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setPatientId(appointment.getPatientId());
        response.setPatientName(patientName);
        response.setReason(appointment.getReason());
        response.setScheduledAt(appointment.getScheduledAt());
        response.setStatus(appointment.getStatus() == null ? "PENDING" : appointment.getStatus());
        response.setCreatedAt(appointment.getCreatedAt());
        return response;
    }
}
