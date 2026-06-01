package com.clinic.queue.service;

import com.clinic.queue.dto.AppointmentRequest;
import com.clinic.queue.dto.AppointmentResponse;
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
        appointment.setScheduledAt(LocalDateTime.parse(request.getScheduledAt()));

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return toResponse(savedAppointment, patient.getName());
    }

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAllByOrderByScheduledAtAsc().stream()
                .map(appointment -> {
                    Patient patient = patientService.getPatientById(appointment.getPatientId());
                    return toResponse(appointment, patient.getName());
                })
                .collect(Collectors.toList());
    }

    private AppointmentResponse toResponse(Appointment appointment, String patientName) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setPatientId(appointment.getPatientId());
        response.setPatientName(patientName);
        response.setReason(appointment.getReason());
        response.setScheduledAt(appointment.getScheduledAt());
        response.setCreatedAt(appointment.getCreatedAt());
        return response;
    }
}
