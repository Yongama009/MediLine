package com.clinic.queue.controller;

import com.clinic.queue.dto.PatientRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String createPatientPayload() throws Exception {
        PatientRequest request = new PatientRequest();
        request.setName("John Doe");
        request.setPhone("555-1234");
        request.setNotes("Walk-in patient");
        return objectMapper.writeValueAsString(request);
    }

    @Test
    void shouldRegisterPatient() throws Exception {
        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPatientPayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("555-1234"))
                .andExpect(jsonPath("$.notes").value("Walk-in patient"));
    }

    @Test
    void shouldGetAllPatients() throws Exception {
        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPatientPayload()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    void shouldGetPatientById() throws Exception {
        String response = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPatientPayload()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long patientId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/patients/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void shouldUpdatePatient() throws Exception {
        String response = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPatientPayload()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long patientId = objectMapper.readTree(response).get("id").asLong();

        PatientRequest updateRequest = new PatientRequest();
        updateRequest.setName("Jane Doe");
        updateRequest.setPhone("555-9876");
        updateRequest.setNotes("Updated notes");

        mockMvc.perform(put("/api/patients/{id}", patientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.phone").value("555-9876"))
                .andExpect(jsonPath("$.notes").value("Updated notes"));
    }

    @Test
    void shouldDeletePatient() throws Exception {
        String response = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createPatientPayload()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long patientId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/patients/{id}", patientId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/patients/{id}", patientId))
                .andExpect(status().isNotFound());
    }
}
