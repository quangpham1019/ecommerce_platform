package com.quang.marketplace.modules.seller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
public class SellerProfileIntegrationTest extends com.quang.marketplace.AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    public void unauthorizedUserCannotAccessSellerProfileEndpoints() throws Exception {
        mvc.perform(get("/api/seller-profiles/me"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/seller-profiles")
                .contentType("application/json")
                .content("{\"displayName\":\"My Store\",\"bio\":\"Nice shop\"}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(patch("/api/seller-profiles/me")
                .contentType("application/json")
                .content("{\"displayName\":\"Updated Store\",\"bio\":\"Updated bio\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void authorizedUserCanCreateAndUpdateSellerProfileAndDuplicateCreateIsRejected() throws Exception {
        String email = "seller" + System.currentTimeMillis() + "@example.com";
        String userBody = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email);

        mvc.perform(post("/api/register").contentType("application/json").content(userBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email));

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/login").session(session).contentType("application/json").content(userBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        String createBody = "{\"displayName\":\"My Store\",\"bio\":\"A lovely shop\"}";
        mvc.perform(post("/api/seller-profiles").session(session).contentType("application/json").content(createBody))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/seller-profiles/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("My Store"))
                .andExpect(jsonPath("$.bio").value("A lovely shop"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        String updateBody = "{\"displayName\":\"Updated Store\",\"bio\":\"Updated bio\"}";
        mvc.perform(patch("/api/seller-profiles/me").session(session).contentType("application/json").content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Updated Store"))
                .andExpect(jsonPath("$.bio").value("Updated bio"));

        mvc.perform(post("/api/seller-profiles").session(session).contentType("application/json").content(createBody))
                .andExpect(status().isConflict());
    }
}
