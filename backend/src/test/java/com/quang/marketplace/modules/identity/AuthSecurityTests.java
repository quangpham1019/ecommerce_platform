package com.quang.marketplace.modules.identity;

import com.quang.marketplace.AbstractIntegrationTest;
import com.quang.marketplace.modules.identity.infrastructure.UserRepository;
import com.quang.marketplace.modules.identity.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthSecurityTests extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void login_returns_set_cookie_header() throws Exception {
        String email = "tuser" + System.currentTimeMillis() + "@example.com";
        String body = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email);

        mvc.perform(post("/api/register").contentType("application/json").content(body))
                .andExpect(status().isCreated());

        var resp = mvc.perform(post("/api/login").contentType("application/json").content(body)).andReturn().getResponse();
        String setCookie = resp.getHeader("Set-Cookie");
        assertNotNull(setCookie, "Expected Set-Cookie header on login");
        assertTrue(setCookie.contains("JSESSIONID"));
        assertTrue(setCookie.toLowerCase().contains("httponly"));
    }

    @Test
    public void malformed_json_or_invalid_content_type_returns_400() throws Exception {
        // malformed JSON
        mvc.perform(post("/api/login").contentType("application/json").content("{not-json"))
                .andExpect(status().isBadRequest());

        // invalid content-type
        mvc.perform(post("/api/login").contentType("text/plain").content("plain text"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void sql_injection_like_input_does_not_login() throws Exception {
        // register a normal user
        String email = "safe" + System.currentTimeMillis() + "@example.com";
        String body = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email);
        mvc.perform(post("/api/register").contentType("application/json").content(body))
                .andExpect(status().isCreated());

        // attempt login with SQL-injection looking email
        String inj = "{\"email\":\"' OR '1'='1\",\"password\":\"whatever\"}";
        mvc.perform(post("/api/login").contentType("application/json").content(inj))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void failed_login_then_access_protected_endpoint_is_denied() throws Exception {
        String email = "flow" + System.currentTimeMillis() + "@example.com";
        String body = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email);
        mvc.perform(post("/api/register").contentType("application/json").content(body))
                .andExpect(status().isCreated());

        MockHttpSession session = new MockHttpSession();

        // wrong password
        String wrong = String.format("{\"email\":\"%s\",\"password\":\"badpass\"}", email);
        mvc.perform(post("/api/login").session(session).contentType("application/json").content(wrong))
                .andExpect(status().is4xxClientError());

        String prod = "{\"title\":\"T\",\"description\":\"D\"}";
        mvc.perform(post("/api/products").session(session).contentType("application/json").content(prod))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void race_login_while_password_changed_old_password_fails() throws Exception {
        String email = "race" + System.currentTimeMillis() + "@example.com";
        String body = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email);
        mvc.perform(post("/api/register").contentType("application/json").content(body))
                .andExpect(status().isCreated());

        // load user and change its password in DB to simulate concurrent password change
        var userOpt = userRepository.findByEmail(email.toLowerCase());
        assertThat(userOpt).isPresent();
        User u = userOpt.get();
        u = userRepository.save(new User(u.getEmail(), passwordEncoder.encode("new-secret")));

        // attempt login with old password should fail
        mvc.perform(post("/api/login").contentType("application/json").content(body))
                .andExpect(status().is4xxClientError());
    }
}
