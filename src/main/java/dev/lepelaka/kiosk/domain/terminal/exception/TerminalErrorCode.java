package dev.lepelaka.kiosk.domain.terminal.exception;

import dev.lepelaka.kiosk.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TerminalErrorCode implements ErrorCode {
    TERMINAL_AUTH_REQUIRED("TERMINAL-001", "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
    TERMINAL_AUTH_INVALID("TERMINAL-002", "잘못된 인증입니다", HttpStatus.UNAUTHORIZED),
    TERMINAL_KEY_MISMATCH("TERMINAL-003", "키가 일치하지 않습니다", HttpStatus.UNAUTHORIZED),
    TERMINAL_INACTIVE("TERMINAL-004", "비활성화된 단말기입니다", HttpStatus.FORBIDDEN),
    TERMINAL_MAINTENANCE("TERMINAL-005", "정비중인 단말기입니다", HttpStatus.FORBIDDEN)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
