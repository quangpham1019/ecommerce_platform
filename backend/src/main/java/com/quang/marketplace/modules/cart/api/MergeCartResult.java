package com.quang.marketplace.modules.cart.api;

import java.util.List;

public record MergeCartResult(
    boolean merged,
    List<String> warnings
) {
    
}
