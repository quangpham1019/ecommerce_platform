package com.quang.marketplace.modules.catalog.api;

public record ProductView(
    Long id, String title, String description, boolean published) {

}
