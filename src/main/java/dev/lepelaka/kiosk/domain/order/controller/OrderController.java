package dev.lepelaka.kiosk.domain.order.controller;

import dev.lepelaka.kiosk.domain.order.dto.OrderCreateRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderResponse;
import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import dev.lepelaka.kiosk.domain.order.service.OrderService;
import dev.lepelaka.kiosk.global.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Long orderId = orderService.createOrder(request);
        return ResponseEntity.created(URI.create("/api/v1/order/" + orderId))
                .body(orderService.getOrder(orderId));
    }
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrder(orderNumber));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(@RequestParam(required = false) OrderStatus status, Pageable pageable) {
        PageResponse<OrderResponse> orders = status != null
                ? orderService.getOrdersByStatus(status, pageable)
                : orderService.getAllOrders(pageable);

        return ResponseEntity.ok(orders);
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirmOrder(@PathVariable Long orderId) {
        orderService.confirmOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/complete")
    public ResponseEntity<Void> completeOrder(@PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
