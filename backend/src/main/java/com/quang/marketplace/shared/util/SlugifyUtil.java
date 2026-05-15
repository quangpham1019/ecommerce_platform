package com.quang.marketplace.shared.util;

public class SlugifyUtil {

    public static String slugify(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}