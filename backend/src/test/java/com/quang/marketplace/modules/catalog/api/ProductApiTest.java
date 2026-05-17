package com.quang.marketplace.modules.catalog.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;
import com.quang.marketplace.AbstractIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductApiTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void POST_products_unauthenticated_returns401() throws Exception {
        mvc.perform(post("/api/products").contentType("application/json").content("{\"name\":\"X\",\"description\":\"D\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void POST_products_malformedJson_returns400() throws Exception {
        MockHttpSession session = loginSeller("Blank Title Shop");

        mvc.perform(
            post("/api/products")
                .session(session)
                .contentType("application/json")
                .content("""
                    {
                        name": "X",
                        "description": "D"
                    } 
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_products_blankTitle_returns400() throws Exception {
        MockHttpSession session = loginSeller("Blank Title Shop");

        mvc.perform(post("/api/products")
                .session(session)
                .contentType("application/json")
                .content("""
                    {
                        "title": "",
                        "description": "Description"
                    }
                """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void POST_products_missingDescription_returns400() throws Exception {
        MockHttpSession session = loginSeller("Missing Description Shop");

        mvc.perform(post("/api/products")
                .session(session)
                .contentType("application/json")
                .content("""
                    {
                        "title": "Product"
                    }
                """))
            .andExpect(status().isBadRequest());
    }
    
    @Test
    void POST_products_withoutActiveSeller_returns403() throws Exception {
        MockHttpSession session = loginUser("noseller" + System.currentTimeMillis() + "@example.com");

        mvc.perform(post("/api/products").session(session).contentType("application/json").content("{\"title\":\"X\",\"description\":\"D\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void POST_products_withValidRequest_returns201() throws Exception {
        MockHttpSession session = loginSeller("Valid Product Shop");

        mvc.perform(post("/api/products").session(session).contentType("application/json").content("{\"title\":\"My Product\",\"description\":\"desc\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void POST_productVariants_unauthenticated_returns401() throws Exception {
        mvc.perform(
            post("/api/products/1/variants")
                .contentType("application/json")
                .content("{\"sku\":\"SKU\",\"price\":9.99,\"quantity\":1}"))
                .andExpect(status().isUnauthorized()
            );
    }

    @Test
    void POST_productVariants_wrongSeller_returns403() throws Exception {
        MockHttpSession session = loginSeller("Other Seller Shop");

        mvc.perform(
            post("/api/products/1/variants")
                .session(session)
                .contentType("application/json")
                .content("{\"sku\":\"SKU\",\"price\":9.99,\"quantity\":1}"))
                .andExpect(status().isForbidden()
            );
    }

    @Test
    void POST_productVariants_duplicateSku_returns409() throws Exception {
        MockHttpSession session = loginSeller("Variant Validation Shop");

        Long productId = createProduct(session);

        mvc.perform(
            post("/api/products/" + productId + "/variants")
                .session(session)
                .contentType("application/json")
                .content("{\"sku\":\"DUP\",\"price\":9.99,\"quantity\":1}"))
                .andExpect(status().isCreated());
        mvc.perform(
            post("/api/products/" + productId + "/variants")
                .session(session)
                .contentType("application/json")
                .content("{\"sku\":\"DUP\",\"price\":9.99,\"quantity\":1}"))
                .andExpect(status().isConflict());
    }

    @Test
    void POST_productVariants_negativePrice_returns400() throws Exception {
        MockHttpSession session = loginSeller("Variant Validation Shop");

        Long productId = createProduct(session);

        mvc.perform(
            post("/api/products/" + productId + "/variants")
                .session(session)
                .contentType("application/json")
                .content("""
                    {
                        "sku": "SKU",
                        "price": -5,
                        "quantity": 1
                    }
                """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void POST_productVariants_missingFields_returns400() throws Exception {
        MockHttpSession session = loginSeller("Variant Validation Shop");

        Long productId = createProduct(session);

        mvc.perform(
            post("/api/products/" + productId + "/variants")
                .session(session)
                .contentType("application/json")
                .content("""
                    {
                        "sku": "SKU",
                        "price": 5.00
                    }
                """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void POST_productVariants_validRequest_returns201() throws Exception {
        MockHttpSession session = loginSeller("Variant Product Shop");

        Long productId = createProduct(session);

        addVariant(session, productId, new AddVariantRequest("SKU-TEST", new BigDecimal("19.99"), 10, null));

    }
    
    @Test
    void POST_publish_unauthenticated_returns401() throws Exception {
        mvc.perform(post("/api/products/1/publish")).andExpect(status().isUnauthorized());
    }

    @Test
    void POST_publish_wrongSeller_returns403() throws Exception {
        MockHttpSession session = loginSeller("Publish Validation Shop");

        publishProduct(session, 1L, status().isForbidden());
    }

    @Test
    void POST_publish_noVariants_returns409() throws Exception {
        MockHttpSession session = loginSeller("Publish Validation Shop");

        Long productId = createProduct(session);

        publishProduct(session, productId, status().isConflict());
    }

    
    @Test
    void POST_publish_variantWithZeroInventory_returns409() throws Exception {
        MockHttpSession session = loginSeller("Publish Validation Shop");

        Long productId = createProduct(session);

        publishProduct(session, productId, status().isConflict());
    }

    @Test
    void POST_publish_validRequest_returns200() throws Exception {
        MockHttpSession session = loginSeller("Publish Product Shop");

        Long productId = createProduct(session);

        addVariant(session, productId, new AddVariantRequest("SKU-TEST", new BigDecimal("19.99"), 10, null));

        publishProduct(session, productId, status().isOk());
    }

    @Test
    void GET_products_excludesDraftProducts() throws Exception {
        MockHttpSession session = loginSeller("Published Product Shop");
        String productTitle = "Draft Product" + System.nanoTime();
        createProduct(session, productTitle);
        
        mvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].title")
            .value(everyItem(not(productTitle))));
    }

    @Test
    void GET_products_includesPublishedProducts() throws Exception {
        MockHttpSession session = loginSeller("Published Product Shop");
        String productTitle = "Published Product" + System.nanoTime();
        Long productId = createProduct(session, productTitle);
        
        addVariant(session, productId, new AddVariantRequest("SKU-TEST", new BigDecimal("19.99"), 10, null));

        publishProduct(session, productId, status().isOk());

        mvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].title")
            .value(hasItem(productTitle)));
    }


    private MockHttpSession loginUser(String email) throws Exception {
        String body = String.format(
            "{\"email\":\"%s\",\"password\":\"password123\"}",
            email
        );

        mvc.perform(post("/api/register")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isCreated());

        MockHttpSession session = new MockHttpSession();

        mvc.perform(post("/api/login")
                .session(session)
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk());

        return session;
    }

    private MockHttpSession loginUser() throws Exception {
        return loginUser("apiuser-" + System.nanoTime() + "@example.com");
    }

    private MockHttpSession loginSeller(String shopName) throws Exception {
        MockHttpSession session = loginUser();

        mvc.perform(post("/api/seller-profiles")
                .session(session)
                .contentType("application/json")
                .content(String.format(
                    "{\"displayName\":\"%s\",\"bio\":\"desc\"}",
                    shopName
                )))
            .andExpect(status().isCreated());

        return session;
    }

    private Long createProduct(MockHttpSession session) throws Exception {
        return createProduct(session, "Test Product");
    }

    private Long createProduct(MockHttpSession session, String title) throws Exception {
        String body = String.format("""
            {
                "title": "%s",
                "description": "Description"
            }
            """, title);

        String location = mvc.perform(
                post("/api/products")
                    .session(session)
                    .contentType("application/json")
                    .content(body)
            )
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getHeader("Location");

        String[] parts = location.split("/");

        return Long.valueOf(parts[parts.length - 1]);
    }

    private void addVariant(MockHttpSession session, Long productId, AddVariantRequest request) throws Exception {
        String body = String.format("""
            {
                "sku": "%s",
                "price": %f,
                "quantity": %d
            }
            """, request.sku(), request.price(), request.quantity());

        mvc.perform(
            post("/api/products/" + productId + "/variants")
                .session(session)
                .contentType("application/json")
                .content(body)
        )
        .andExpect(status().isCreated());
    }

    private void publishProduct(MockHttpSession session, Long productId, ResultMatcher resultMatcher) throws Exception {
        mvc.perform(post("/api/products/" + productId + "/publish").session(session))
            .andExpect(resultMatcher);
    }
}
