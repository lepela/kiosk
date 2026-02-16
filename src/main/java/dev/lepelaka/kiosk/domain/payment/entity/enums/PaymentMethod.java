package dev.lepelaka.kiosk.domain.payment.entity.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("카드"), CASH("현금"), SIMPLE("간편결제");
    private final String description;
}

