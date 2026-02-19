package dev.lepelaka.kiosk.domain.category.exception;

import dev.lepelaka.kiosk.global.exception.ErrorCode;

public class CategoryNotFoundException extends CategoryException{
    private Long id;
    public CategoryNotFoundException(Long categoryId) {
        super(CategoryErrorCode.CATEGORY_NOT_FOUND);
        this.id = categoryId;

    }
}
