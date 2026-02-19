package dev.lepelaka.kiosk.domain.product.dto;

import dev.lepelaka.kiosk.domain.category.dto.CategoryResponse;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 응답 DTO")
public record ProductResponse (
        @Schema(description = "상품 ID", example = "1")
        Long id,
        
        @Schema(description = "상품명", example = "짜장면")
        String name,
        
        @Schema(description = "가격", example = "7000")
        int price,
        
        @Schema(description = "재고 수량", example = "100")
        int quantity,
        
        @Schema(description = "상품 설명", example = "맛있는 짜장면")
        String description,
        
        @Schema(description = "이미지 URL", example = "http://example.com/jjajang.jpg")
        String imageUrl,
        
        @Schema(description = "카테고리", example = "메인")
        CategoryResponse categoryResponse
) {
    public static ProductResponse fromEntity (Product product) {
        return new ProductResponse(
                product.getId(), 
                product.getName(), 
                product.getPrice(), 
                product.getQuantity(), 
                product.getDescription(), 
                product.getImageUrl(),
                CategoryResponse.from(product.getCategory())
        );
    }
}
