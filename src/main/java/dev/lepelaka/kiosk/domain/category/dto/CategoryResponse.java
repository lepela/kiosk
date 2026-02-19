package dev.lepelaka.kiosk.domain.category.dto;

import dev.lepelaka.kiosk.domain.category.entity.Category;

import java.time.LocalDateTime;

public record CategoryResponse (
        Long id,
        String name,
        String description,
        int displayOrder,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getDisplayOrder(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
