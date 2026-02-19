package dev.lepelaka.kiosk.domain.category.dto;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest (
        @NotBlank @Size(max = 50)
        String name,

        @Size(max = 200)
        String description,

        @Min(0)
        int displayOrder
) {

}
