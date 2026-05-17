package com.quang.marketplace.modules.catalog.api;

import jakarta.validation.constraints.NotNull;

public record CreateProductRequest(

    @NotNull
    String title, 

    @NotNull
    String description) {
    
}
