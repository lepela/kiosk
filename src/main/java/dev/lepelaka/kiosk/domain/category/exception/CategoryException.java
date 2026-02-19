package dev.lepelaka.kiosk.domain.category.exception;

import dev.lepelaka.kiosk.global.exception.BusinessException;
import dev.lepelaka.kiosk.global.exception.ErrorCode;

public class CategoryException extends BusinessException {
    public CategoryException(ErrorCode errorCode) {
        super(errorCode);
    }
}
