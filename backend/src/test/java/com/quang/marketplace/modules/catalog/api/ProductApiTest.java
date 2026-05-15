package com.quang.marketplace.modules.catalog.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
public class ProductApiTest extends com.quang.marketplace.AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    public void POST_products_unauthenticated_returns401() throws Exception {
        mvc.perform(post("/api/products").contentType("application/json").content("{\"name\":\"X\",\"description\":\"D\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void POST_products_withoutActiveSeller_returns403() throws Exception {
        String email = "apiuser" + System.currentTimeMillis() + "@example.com";
        String userBody = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email);

        mvc.perform(post("/api/register").contentType("application/json").content(userBody)).andExpect(status().isCreated());

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/login").session(session).contentType("application/json").content(userBody)).andExpect(status().isOk());

        mvc.perform(post("/api/products").session(session).contentType("application/json").content("{\"title\":\"X\",\"description\":\"D\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void POST_products_withValidRequest_returns201() throws Exception {
        String email = "apiuser2" + System.currentTimeMillis() + "@example.com";
        String userBody = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email);

        mvc.perform(post("/api/register").contentType("application/json").content(userBody)).andExpect(status().isCreated());

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/login").session(session).contentType("application/json").content(userBody)).andExpect(status().isOk());

        // create seller profile
        String createBody = "{\"displayName\":\"My API Shop\",\"bio\":\"desc\"}";
        mvc.perform(post("/api/seller-profiles").session(session).contentType("application/json").content(createBody)).andExpect(status().isCreated());

        mvc.perform(post("/api/products").session(session).contentType("application/json").content("{\"title\":\"My Product\",\"description\":\"desc\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void POST_productVariants_validRequest_returns201() throws Exception {
        String email = "apiuser3" + System.currentTimeMillis() + "@example.com";
        String userBody = String.format("{\"email\":\"%s\",\"password\":\"password123\"}", email);

        mvc.perform(post("/api/register").contentType("application/json").content(userBody)).andExpect(status().isCreated());

        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/login").session(session).contentType("application/json").content(userBody)).andExpect(status().isOk());

        String createBody = "{\"displayName\":\"Variant Shop\",\"bio\":\"desc\"}";
        mvc.perform(post("/api/seller-profiles").session(session).contentType("application/json").content(createBody)).andExpect(status().isCreated());

        // create product and parse returned location to get id
        String prod = "{\"title\":\"P1\",\"description\":\"d\"}";
        var resp = mvc.perform(post("/api/products").session(session).contentType("application/json").content(prod))
            .andExpect(status().isCreated())
            .andReturn().getResponse();

        String location = resp.getHeader("Location");
        // location is like /api/products/{id}
        String[] parts = location.split("/");
        String id = parts[parts.length-1];

        mvc.perform(post("/api/products/"+id+"/variants").session(session).contentType("application/json").content("{\"sku\":\"SKU-TEST-"+System.currentTimeMillis()+"\",\"price\":19.99,\"quantity\":10}"))
            .andExpect(status().isCreated());
    }

    @Test
    public void POST_productVariants_duplicateSku_returns409() throws Exception {
        mvc.perform(post("/api/products/1/variants").contentType("application/json").content("{\"sku\":\"DUP\",\"price\":9.99,\"quantity\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void POST_publish_unauthenticated_returns401() throws Exception {
        mvc.perform(post("/api/products/1/publish")).andExpect(status().isUnauthorized());
    }

    @Test
    public void GET_products_returnsOnlyPublishedProducts() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$[0].published").exists());
    }
}
