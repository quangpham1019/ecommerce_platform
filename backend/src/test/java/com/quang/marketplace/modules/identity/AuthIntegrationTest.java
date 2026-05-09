package com.quang.marketplace.modules.identity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.quang.marketplace.shared.error.InvalidCredentialsException;

import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest extends com.quang.marketplace.AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    public void registerLoginLogoutFlow() throws Exception {
        String email = "tuser" + System.currentTimeMillis() + "@example.com";
        String body = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email);

        // register
        mvc.perform(post("/api/register").contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email));

        MockHttpSession session = new MockHttpSession();

        // login (attach session so cookie flows)
        mvc.perform(post("/api/login").session(session).contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        // logout
        mvc.perform(post("/api/logout").session(session))
                .andExpect(status().isOk());
    }

    
    @Test
    public void duplicateEmailRegistrationFails() throws Exception {
        String email1 = "dup2" + System.currentTimeMillis() + "@example.com";
        String body = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email1);

        mvc.perform(post("/api/register")
                .contentType("application/json")
                .content(body))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/register")
                .contentType("application/json")
                .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    public void login_creates_security_context_in_session_and_logout_invalidates() throws Exception {

        String email2 = "flow" + System.currentTimeMillis() + "@example.com";
        String body = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email2);

        // register
        mvc.perform(post("/api/register").contentType("application/json").content(body))
                .andExpect(status().isCreated());

        MockHttpSession session = new MockHttpSession();

        // login
        mvc.perform(post("/api/login").session(session).contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email2));

        Object ctx = session.getAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertThat(ctx).isNotNull();

        // logout
        mvc.perform(post("/api/logout").session(session))
                .andExpect(status().isOk());

        // After logout, further authenticated requests with same session should be unauthorized
        String prod = "{\"title\":\"T\",\"description\":\"D\"}";
        mvc.perform(post("/api/products").session(session).contentType("application/json").content(prod))
                .andExpect(status().isUnauthorized());
    }
}
