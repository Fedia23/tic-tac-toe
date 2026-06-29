package com.fyre.tictactoe.session.exception;

import com.fyre.tictactoe.session.client.GameEngineClient;
import com.fyre.tictactoe.session.repository.GameSessionRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameSessionRepository sessionRepository;

    @MockBean
    private GameEngineClient gameEngineClient;

    @Test
    @DisplayName("Should handle SessionNotFoundException with 404")
    void handleSessionNotFoundException() throws Exception {
        String validUuid = "550e8400-e29b-41d4-a716-446655440000";
        mockMvc.perform(get("/sessions/" + validUuid))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value(containsString("Session not found")))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should handle IllegalStateException with 400")
    void handleIllegalStateException() throws Exception {
        Request request = Request.create(Request.HttpMethod.POST, "/games",
            Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());

        when(gameEngineClient.createGame())
            .thenThrow(new FeignException.ServiceUnavailable("Service unavailable", request, null, null));

        mockMvc.perform(post("/sessions")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().is5xxServerError())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should handle FeignException with appropriate status code")
    void handleFeignException() throws Exception {
        Request request = Request.create(Request.HttpMethod.POST, "/games",
            Collections.emptyMap(), null, StandardCharsets.UTF_8, new RequestTemplate());

        when(gameEngineClient.createGame())
            .thenThrow(new FeignException.BadRequest("Bad request", request, null, null));

        mockMvc.perform(post("/sessions")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(containsString("Error communicating with Game Engine Service")))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should include timestamp in error response")
    void errorResponseIncludesTimestamp() throws Exception {
        String validUuid = "550e8400-e29b-41d4-a716-446655440000";
        mockMvc.perform(get("/sessions/" + validUuid))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("Should include status code in error response")
    void errorResponseIncludesStatusCode() throws Exception {
        String validUuid = "550e8400-e29b-41d4-a716-446655440000";
        mockMvc.perform(get("/sessions/" + validUuid))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Should include error description in response")
    void errorResponseIncludesErrorDescription() throws Exception {
        String validUuid = "550e8400-e29b-41d4-a716-446655440000";
        mockMvc.perform(get("/sessions/" + validUuid))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"));
    }
}