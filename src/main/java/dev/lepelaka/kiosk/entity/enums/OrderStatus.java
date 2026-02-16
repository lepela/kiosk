package dev.lepelaka.kiosk.entity.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("대기"), PROCESSING("조리중"), COMPLETED("완료"), CANCELLED("취소");
    private final String description;
}
