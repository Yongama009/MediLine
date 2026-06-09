package com.clinic.queue.controller;

import com.clinic.queue.dto.PatientRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
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

        private String createPatientPayload(String email) throws Exception {
                PatientRequest request = new PatientRequest();
                request.setName("John Doe");
                request.setEmail(email);
                request.setPhone("555-1234");
                request.setPassword("password123");
                request.setNotes("Walk-in patient");
                return objectMapper.writeValueAsString(request);
        }

        @Test
        void shouldRegisterPatient() throws Exception {
                String email = "reg-test@example.com";
                mockMvc.perform(post("/api/patients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPatientPayload(email)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("John Doe"))
                                .andExpect(jsonPath("$.email").value(email))
                                .andExpect(jsonPath("$.phone").value("555-1234"))
                                .andExpect(jsonPath("$.notes").value("Walk-in patient"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldGetAllPatients() throws Exception {
                String email = "all-test@example.com";
                mockMvc.perform(post("/api/patients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPatientPayload(email)))
                                .andExpect(status().isCreated());

                mockMvc.perform(get("/api/patients"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("John Doe"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldGetPatientById() throws Exception {
                String email = "id-test@example.com";
                String response = mockMvc.perform(post("/api/patients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPatientPayload(email)))
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
        @WithMockUser(roles = "ADMIN")
        void shouldUpdatePatient() throws Exception {
                String email = "up1-test@example.com";
                String response = mockMvc.perform(post("/api/patients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPatientPayload(email)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                Long patientId = objectMapper.readTree(response).get("id").asLong();

                PatientRequest updateRequest = new PatientRequest();
                updateRequest.setName("Jane Doe");
                updateRequest.setEmail("jane.doe@example.com");
                updateRequest.setPhone("555-9876");
                updateRequest.setPassword("newpassword");
                updateRequest.setNotes("Updated notes");

                mockMvc.perform(put("/api/patients/{id}", patientId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(patientId))
                                .andExpect(jsonPath("$.name").value("Jane Doe"))
                                .andExpect(jsonPath("$.email").value("jane.doe@example.com"))
                                .andExpect(jsonPath("$.phone").value("555-9876"))
                                .andExpect(jsonPath("$.notes").value("Updated notes"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldDeletePatient() throws Exception {
                String email = "del-test@example.com";
                String response = mockMvc.perform(post("/api/patients")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPatientPayload(email)))
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
