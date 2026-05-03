package com.quang.marketplace.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
public class SecurityApiTest extends com.quang.marketplace.AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void unauthenticated_post_products_returns_401() throws Exception {
        String body = "{\"title\":\"T\",\"description\":\"D\"}";

        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticated_post_products_and_logout_flow() throws Exception {
        String email = "secuser" + System.currentTimeMillis() + "@example.com";
        String reg = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email);
        String prod = "{\"title\":\"T\",\"description\":\"D\"}";

        // register
        mvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON).content(reg))
                .andExpect(status().isCreated());

        MockHttpSession session = new MockHttpSession();

        // login
        mvc.perform(post("/api/login").session(session).contentType(MediaType.APPLICATION_JSON).content(reg))
                .andExpect(status().isOk());

        // authenticated request should succeed for creating a seller profile
        String sellerReq = "{\"displayName\":\"Shop\"}";
        mvc.perform(post("/api/seller-profiles").session(session).contentType(MediaType.APPLICATION_JSON).content(sellerReq))
                .andExpect(status().isCreated());

        // logout
        mvc.perform(post("/api/logout").session(session))
                .andExpect(status().isOk());

        // after logout, same session should no longer authenticate
        mvc.perform(post("/api/products").session(session).contentType(MediaType.APPLICATION_JSON).content(prod))
                .andExpect(status().isUnauthorized());
    }
}
