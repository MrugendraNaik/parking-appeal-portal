package com.vit.parkingappeal.web;

import com.vit.parkingappeal.TestData;
import com.vit.parkingappeal.model.Violation;
import com.vit.parkingappeal.repository.ViolationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ViolationRepository violations;

    @Test
    void healthEndpointIsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void homePageRenders() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"));
    }

    @Test
    void submitThenTrackThenDecideViaApi() throws Exception {
        Violation v = TestData.freshViolation(violations, 1);
        String body = mvc.perform(multipart("/api/appeals").file(TestData.pdf())
                        .param("ticketNumber", v.getTicketNumber())
                        .param("vehicleNumber", v.getVehicleNumber())
                        .param("ownerName", "Rahul Desai")
                        .param("email", "rahul@example.com")
                        .param("phone", "9123456780")
                        .param("reason", TestData.VALID_REASON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andReturn().getResponse().getContentAsString();
        String ref = body.replaceAll(".*\"appealRef\":\"([^\"]+)\".*", "$1");

        mvc.perform(get("/api/appeals/" + ref).param("email", "rahul@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketNumber").value(v.getTicketNumber()));

        mvc.perform(post("/api/appeals/" + ref + "/decision").header("X-Reviewer-Key", "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"APPROVE\",\"remarks\":\"Evidence accepted\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void invalidSubmissionReturnsFieldErrors() throws Exception {
        mvc.perform(multipart("/api/appeals").param("ticketNumber", "bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ticketNumber").exists())
                .andExpect(jsonPath("$.errors.evidence").exists());
    }

    @Test
    void reviewerEndpointsNeedTheKey() throws Exception {
        mvc.perform(get("/api/appeals")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/appeals").header("X-Reviewer-Key", "test-key")).andExpect(status().isOk());
    }

    @Test
    void reviewerPagesRedirectToLogin() throws Exception {
        mvc.perform(get("/reviewer")).andExpect(status().is3xxRedirection());
    }
}
