package dev.lepelaka.kiosk.domain.category.dto;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CategoryCreateRequest(
        @NotBlank @Size(max = 50)
        String name,
        @Size(max = 200)
        String description,
        @Min(0)
        int displayOrder
) {
    public Category toEntity() {
        return Category.builder()
                .name(name)
                .description(description)
                .displayOrder(displayOrder)
                .build();
    }
}
