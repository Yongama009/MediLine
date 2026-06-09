package com.clinic.queue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public class PatientRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email should be valid") // Removed NotBlank to allow partial updates
    private String email;

    private String phone;
    private String password;

    private String role;
    private String notes;
    private String adminCode; // Required only when registering as ADMIN
    private Boolean smsOptIn;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public Boolean getSmsOptIn() {
        return smsOptIn;
    }

    public void setSmsOptIn(Boolean smsOptIn) {
        this.smsOptIn = smsOptIn;
    }
}
