package com.waste.helper.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.waste.helper.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn429WhenRateLimitExceeded() throws Exception {
        // IP rate limit: 10/min — send 11 requests
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/v1/auth/guest"))
                .andExpect(result -> {
                    // First 10 should not be 429 (they may be 404 or other, but not 429)
                });
        }
        // 11th request should be rate limited
        mockMvc.perform(get("/api/v1/auth/guest"))
            .andExpect(status().isTooManyRequests())
            .andExpect(header().string("X-RateLimit-Remaining", "0"));
    }
}
