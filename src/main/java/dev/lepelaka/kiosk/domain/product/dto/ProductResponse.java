package dev.lepelaka.kiosk.domain.product.dto;

import dev.lepelaka.kiosk.domain.product.entity.Product;

public record ProductResponse (
        Long id,
        String name,
        int price,
        int quantity,
        String description,
        String imageUrl,
        String category
) {
    public static ProductResponse fromEntity (Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getQuantity(), product.getDescription(), product.getImageUrl(), product.getCategory());
    }
}
