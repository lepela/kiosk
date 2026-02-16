package dev.lepelaka.kiosk.domain.product.dto;


import dev.lepelaka.kiosk.domain.product.entity.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductUpdateRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        String name,
        @Positive(message = "가격은 0보다 커야 합니다.")
        int price,
        @PositiveOrZero(message = "수량은 0이상 이어야 합니다.")
        int quantity,
        String description,
        String imageUrl,

        @NotBlank(message = "카테고리는 필수입니다.")
        String category
) {}
