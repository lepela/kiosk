package dev.lepelaka.kiosk.domain.order.controller;

import dev.lepelaka.kiosk.domain.order.dto.OrderCreateRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderResponse;
import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import dev.lepelaka.kiosk.domain.order.service.OrderService;
import dev.lepelaka.kiosk.global.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "주문 API", description = "주문 생성, 조회, 취소 및 상태 변경 API")
@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Long orderId = orderService.createOrder(request);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderId)
                .toUri();
        
        return ResponseEntity.created(location)
                .body(orderService.getOrder(orderId));
    }
    
    @Operation(summary = "주문 조회", description = "주문 번호로 주문 정보를 조회합니다.")
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrder(orderNumber));
    }

    @Operation(summary = "주문 취소", description = "주문을 취소합니다. (결제 전 상태만 가능)")
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "주문 목록 조회 (관리자용)", description = "전체 주문 또는 상태별 주문 목록을 조회합니다.")
    @GetMapping("/admin")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @Parameter(description = "주문 상태") @RequestParam(required = false) OrderStatus status,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        
        PageResponse<OrderResponse> orders = status != null
                ? orderService.getOrdersByStatus(status, pageable)
                : orderService.getAllOrders(pageable);

        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "주문 확정 (결제 완료)", description = "주문을 결제 완료 상태로 변경합니다.")
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable Long orderId) {
        orderService.confirmOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "주문 완료 (제조 완료)", description = "주문을 제조 완료 상태로 변경합니다.")
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
