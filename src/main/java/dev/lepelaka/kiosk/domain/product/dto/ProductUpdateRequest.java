package dev.lepelaka.kiosk.domain.product.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "상품 수정 요청 DTO")
public record ProductUpdateRequest(
        @Schema(description = "상품명", example = "쟁반짜장")
        @NotBlank(message = "상품명은 필수입니다.")
        String name,
        
        @Schema(description = "가격", example = "8000")
        @Positive(message = "가격은 0보다 커야 합니다.")
        int price,
        
        @Schema(description = "재고 수량", example = "50")
        @PositiveOrZero(message = "수량은 0이상 이어야 합니다.")
        int quantity,
        
        @Schema(description = "상품 설명", example = "더 맛있는 쟁반짜장")
        String description,
        
        @Schema(description = "이미지 URL", example = "http://example.com/jjajang_new.jpg")
        String imageUrl,

        @Schema(description = "카테고리", example = "1")
        @NotBlank(message = "카테고리는 필수입니다.")
        Long categoryId
) {}
