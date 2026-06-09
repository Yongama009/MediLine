package com.clinic.queue.repository;

import com.clinic.queue.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAllByOrderByScheduledAtAsc();
    List<Appointment> findByPatientIdAndScheduledAtBetween(Long patientId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
