package com.clinic.queue.security;

import com.clinic.queue.model.Patient;
import com.clinic.queue.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PatientAuthenticationProvider implements AuthenticationProvider {

    private final PatientRepository patientRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public PatientAuthenticationProvider(PatientRepository patientRepository, BCryptPasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String rawPassword = (authentication.getCredentials() == null) ? null : authentication.getCredentials().toString();

        if (username == null) {
            throw new BadCredentialsException("Invalid email or password");
        }

        Patient patient = patientRepository.findByEmailIgnoreCase(username.trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        String storedPassword = patient.getPassword();
        boolean matches = false;

        if (storedPassword != null && storedPassword.startsWith("$2")) {
            // BCrypt hashed
            matches = passwordEncoder.matches(rawPassword, storedPassword);
        } else {
            // Legacy plain-text password: compare directly and migrate to BCrypt on successful match
            if (storedPassword != null && storedPassword.equals(rawPassword)) {
                matches = true;
                try {
                    patient.setPassword(passwordEncoder.encode(rawPassword));
                    patientRepository.save(patient);
                } catch (Exception ex) {
                    // Log if desired; migration shouldn't block authentication
                }
            }
        }

        if (!matches) {
            throw new BadCredentialsException("Invalid email or password");
        }

        UserDetails user = User.withUsername(patient.getEmail())
                .password(patient.getPassword())
                .roles(patient.getRole() == null ? "PATIENT" : patient.getRole())
                .build();

        return new UsernamePasswordAuthenticationToken(user, patient.getPassword(), user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

