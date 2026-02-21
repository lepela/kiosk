package dev.lepelaka.kiosk.domain.category.exception;

import dev.lepelaka.kiosk.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class CategoryNotFoundException extends CategoryException {
    private final Long id;

    public CategoryNotFoundException(Long categoryId) {
        super(ErrorCode.CATEGORY_NOT_FOUND);
        this.id = categoryId;
    }
}
