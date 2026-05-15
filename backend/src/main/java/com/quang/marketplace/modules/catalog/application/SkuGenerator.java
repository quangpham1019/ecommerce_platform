package com.quang.marketplace.modules.catalog.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.quang.marketplace.modules.catalog.api.VariantOptionRequest;

public class SkuGenerator {

    public static String generate(
            String sellerCode,
            Long productId,
            List<VariantOptionRequest> options
    ) {

        String variantCode = options.stream()
                .map(VariantOptionRequest::value)
                .map(SkuGenerator::toCode)
                .collect(Collectors.joining("-"));

        return sellerCode
                + "-" + productId
                + "-" + variantCode;
    }

    private static String toCode(String value) {

        String normalized = value
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "");

        if (normalized.length() <= 3) {
            return normalized;
        }

        return normalized.substring(0, 3);
    }
}