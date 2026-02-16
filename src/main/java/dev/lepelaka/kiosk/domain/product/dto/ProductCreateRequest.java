package dev.lepelaka.kiosk.domain.product.dto;


import dev.lepelaka.kiosk.domain.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "상품 등록 요청 DTO")
public record ProductCreateRequest(
        @Schema(description = "상품명", example = "짜장면")
        @NotBlank(message = "상품명은 필수입니다.")
        String name,
        
        @Schema(description = "가격", example = "7000")
        @Positive(message = "가격은 0보다 커야 합니다.")
        int price,
        
        @Schema(description = "재고 수량", example = "100")
        @PositiveOrZero(message = "수량은 0이상 이어야 합니다.")
        int quantity,
        
        @Schema(description = "상품 설명", example = "맛있는 짜장면")
        String description,
        
        @Schema(description = "이미지 URL", example = "http://example.com/jjajang.jpg")
        String imageUrl,

        @Schema(description = "카테고리", example = "메인")
        @NotBlank(message = "카테고리는 필수입니다.")
        String category
) {
    public Product toEntity() {
        return Product.builder()
                .name(name)
                .price(price)
                .quantity(quantity)
                .description(description)
                .imageUrl(imageUrl)
                .category(category)
                .build();
    }
}
