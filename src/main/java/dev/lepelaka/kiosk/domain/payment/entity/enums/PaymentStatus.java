package dev.lepelaka.kiosk.domain.payment.entity.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("대기"), COMPLETED("완료"), FAILED("실패");
    private final String description;
}

