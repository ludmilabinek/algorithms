package org.example.algorithms.controller;

import org.example.algorithms.exception.ValidationException;
import org.example.algorithms.service.AlgorithmsService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlgorithmsController.class)
class AlgorithmsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlgorithmsService service;

    @Test
    void returnsCorrectFactorial() throws Exception {
        when(service.factorial(5)).thenReturn("120");

        mockMvc.perform(post("/api/algorithms/numbers/factorial")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"n\":5}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result").value("120"));
    }

    @Test
    void returnsHttp400WhenServiceThrowsValidationException() throws Exception {
        when(service.factorial(null))
                .thenThrow(new ValidationException("n must not be null"));

        mockMvc.perform(post("/api/algorithms/numbers/factorial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("n must not be null"));
    }

    @Test
    void returnsHttp400WhenIntegerFieldGetsFloatValue() throws Exception {
        mockMvc.perform(post("/api/algorithms/numbers/factorial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\":3.14}"))
                .andExpect(status().isBadRequest());
    }
}
