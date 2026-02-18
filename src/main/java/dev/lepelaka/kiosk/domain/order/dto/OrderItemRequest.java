package dev.lepelaka.kiosk.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest (
        @NotBlank
        Long productId,
        @Positive
        int quantity
){

}
