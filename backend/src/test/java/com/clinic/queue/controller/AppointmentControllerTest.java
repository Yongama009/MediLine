package com.clinic.queue.controller;

import com.clinic.queue.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerTest {

    @MockBean
    private AppointmentService appointmentService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToConfirmAppointment() throws Exception {
        // Verifies that a user with the ADMIN role (ROLE_ADMIN) can access the
        // endpoint.
        mockMvc.perform(put("/api/appointments/1/confirm"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToStartProgress() throws Exception {
        // Verifies that ADMIN can set an appointment to in-progress
        mockMvc.perform(put("/api/appointments/1/in-progress"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToCompleteAppointment() throws Exception {
        // Verifies that ADMIN can set an appointment to completed
        mockMvc.perform(put("/api/appointments/1/complete"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void shouldDenyPatientToConfirmAppointment() throws Exception {
        // Verifies that a user with the PATIENT role is forbidden from accessing the
        // endpoint.
        mockMvc.perform(put("/api/appointments/1/confirm"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyAnonymousUserToConfirmAppointment() throws Exception {
        // Verifies that unauthenticated users are unauthorized.
        mockMvc.perform(put("/api/appointments/1/confirm"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnClearMessageForInvalidAdminCredentials() throws Exception {
        mockMvc.perform(put("/api/appointments/1/confirm")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjp3cm9uZ3Bhc3N3b3Jk"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Incorrect username or password"));
    }
}