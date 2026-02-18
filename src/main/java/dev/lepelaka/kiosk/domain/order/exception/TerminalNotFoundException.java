package dev.lepelaka.kiosk.domain.order.exception;

public class TerminalNotFoundException extends OrderException {

    private final Long terminalId;

    public TerminalNotFoundException(Long terminalId) {
        super(OrderErrorCode.TERMINAL_NOT_FOUND);
        this.terminalId = terminalId;
    }

    @Override
    public String getMessage() {
        return String.format("%s (터미널 ID : %d", super.getMessage(), terminalId);
    }
}
