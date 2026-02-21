package dev.lepelaka.kiosk.domain.product.exception;

import dev.lepelaka.kiosk.global.exception.BusinessException;
import dev.lepelaka.kiosk.global.exception.ErrorCode;

public class ProductException extends BusinessException {
    protected ProductException(ErrorCode errorCode) {
        super(errorCode);
    }
    protected ProductException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
