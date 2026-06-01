package com.clinic.queue.dto;

public class LoginResponse {
    private String token;
    private String message;
    private Long patientId;
    private String name;

    public LoginResponse(String token, String message, Long patientId, String name) {
        this.token = token;
        this.message = message;
        this.patientId = patientId;
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
