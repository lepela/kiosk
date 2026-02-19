package dev.lepelaka.kiosk.domain.category.exception;

import dev.lepelaka.kiosk.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements ErrorCode {
    CATEGORY_NOT_FOUND("CATEGORY-001", "카테고리를 찾을 수 없습니다", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;


}
