package com.clinic.queue.service;

import com.clinic.queue.dto.AppointmentResponse;
import com.clinic.queue.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${twilio.account.sid:}")
    private String twilioSid;

    @Value("${twilio.auth.token:}")
    private String twilioToken;

    @Value("${twilio.from.number:}")
    private String twilioFrom;

    public NotificationService(@Nullable JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async("taskExecutor")
    public void notifyAppointmentConfirmed(Patient patient, AppointmentResponse appointment) {
        // send email (if mail sender available)
        if (mailSender == null) {
            if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
                log.info("Mail sender not configured; skipping email to {}.", patient.getEmail());
            }
        } else {
            if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
                try {
                    MimeMessage mimeMessage = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
                    Context ctx = new Context();
                    ctx.setVariable("name", patient.getName() == null ? "Patient" : patient.getName());
                    ctx.setVariable("scheduledAt", appointment.getScheduledAt().toString());
                    String htmlBody = templateEngine.process("appointment-confirmed", ctx);
                    helper.setText(htmlBody, true);
                    helper.setTo(patient.getEmail());
                    helper.setSubject("Your appointment is confirmed");
                    mailSender.send(mimeMessage);
                    log.info("Confirmation email sent to {}", patient.getEmail());
                } catch (Exception e) {
                    log.error("Failed to send confirmation email to {}: {}", patient.getEmail(), e.getMessage());
                }
            }
        }

        // send SMS if configured and patient consented
        if (patient.getPhone() != null && !patient.getPhone().isBlank() && patient instanceof Patient) {
            try {
                boolean smsOptIn = false;
                // reflection-free check: use getter method
                try {
                    java.lang.reflect.Method m = patient.getClass().getMethod("getSmsOptIn");
                    Object val = m.invoke(patient);
                    if (val instanceof Boolean) smsOptIn = (Boolean) val;
                } catch (NoSuchMethodException nsme) {
                    // older schema, default to false
                }

                if (!smsOptIn) {
                    log.info("Patient {} has not opted-in for SMS, skipping SMS.", patient.getId());
                    return;
                }

                if (twilioSid != null && !twilioSid.isBlank() && twilioToken != null && !twilioToken.isBlank()
                        && twilioFrom != null && !twilioFrom.isBlank()) {
                    // normalize phone to E.164-ish
                    String normalized = normalizePhone(patient.getPhone());
                    if (normalized == null) {
                        log.warn("Patient phone {} could not be normalized; skipping SMS.", patient.getPhone());
                        return;
                    }
                    try {
                        com.twilio.Twilio.init(twilioSid, twilioToken);
                        String smsBody = String.format("Your appointment on %s is confirmed. Please arrive 15 minutes early.", appointment.getScheduledAt().toString());
                        com.twilio.rest.api.v2010.account.Message.creator(
                                new com.twilio.type.PhoneNumber(normalized),
                                new com.twilio.type.PhoneNumber(twilioFrom),
                                smsBody
                        ).create();
                        log.info("Confirmation SMS sent to {}", normalized);
                    } catch (NoClassDefFoundError cnfe) {
                        log.warn("Twilio SDK not available at runtime; SMS not sent.");
                    } catch (Exception ex) {
                        log.error("Failed to send confirmation SMS to {}: {}", patient.getPhone(), ex.getMessage());
                    }
                } else {
                    log.info("Twilio not configured; skipping SMS.");
                }
            } catch (Exception ex) {
                log.error("Error while preparing SMS for patient {}: {}", patient.getId(), ex.getMessage());
            }
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        String raw = phone.replaceAll("[^0-9+]", "");
        // if already starts with + and digits
        if (raw.startsWith("+")) {
            String digits = raw.substring(1);
            if (digits.length() >= 7 && digits.length() <= 15) return raw;
            else return null;
        }
        // no +: try to add + if length looks like international
        String digitsOnly = raw.replaceAll("[^0-9]", "");
        if (digitsOnly.length() >= 10 && digitsOnly.length() <= 15) {
            return "+" + digitsOnly;
        }
        return null;
    }
}

