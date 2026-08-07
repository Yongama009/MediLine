package com.clinic.queue.service;

import com.clinic.queue.dto.AppointmentRequest;
import com.clinic.queue.dto.AppointmentResponse;
import com.clinic.queue.exception.ResourceNotFoundException;
import com.clinic.queue.model.Appointment;
import com.clinic.queue.model.Patient;
import com.clinic.queue.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientService patientService, NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
        this.notificationService = notificationService;
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
        // Parse scheduled time and reject weekend dates (clinic closed on Saturday/Sunday)
        LocalDateTime scheduledAt = LocalDateTime.parse(scheduledAtStr);
        switch (scheduledAt.getDayOfWeek()) {
            case SATURDAY:
            case SUNDAY:
                throw new IllegalArgumentException("Clinic is closed on weekends. Please choose a weekday.");
            default:
                break;
        }
        appointment.setScheduledAt(scheduledAt);

        // Enforce booking frequency rules:
        // 1) No duplicate bookings for the same patient within the same week (Mon-Sun)
        LocalDateTime weekStart = scheduledAt.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        LocalDateTime weekEnd = scheduledAt.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX);
        List<Appointment> sameWeek = appointmentRepository.findByPatientIdAndScheduledAtBetween(patient.getId(), weekStart, weekEnd);
        if (!sameWeek.isEmpty()) {
            throw new IllegalArgumentException("You already have an appointment scheduled in the same week. Please choose another date.");
        }

        // 2) Allow at most one booking per calendar month
        LocalDateTime monthStart = scheduledAt.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime monthEnd = scheduledAt.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
        List<Appointment> sameMonth = appointmentRepository.findByPatientIdAndScheduledAtBetween(patient.getId(), monthStart, monthEnd);
        if (!sameMonth.isEmpty()) {
            throw new IllegalArgumentException("You can only book once per month. Please choose a different month.");
        }

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
        // notify patient that appointment is confirmed
        try {
            Patient patient = patientService.getPatientById(appointment.getPatientId());
            notificationService.notifyAppointmentConfirmed(patient, toResponse(appointment, patient.getName()));
        } catch (Exception e) {
            // swallow notification errors to avoid failing the confirm operation but log them
            log.error("Failed to send confirmation notification: {}", e.getMessage());
        }
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
        response.setStatus(appointment.getStatus() == null ? "PENDING" : appointment.getStatus().toUpperCase());
        response.setCreatedAt(appointment.getCreatedAt());
        return response;
    }
}
//yongs