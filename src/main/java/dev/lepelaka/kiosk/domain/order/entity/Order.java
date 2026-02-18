package dev.lepelaka.kiosk.domain.order.entity;

import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import dev.lepelaka.kiosk.domain.order.exception.InvalidOrderStatusException;
import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_number", columnList = "orderNumber"),
        @Index(name = "idx_terminal_status", columnList = "terminal_id, status"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@ToString(exclude = {"terminal", "orderItems"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;
    @Column(nullable = false)
    private int totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false)
    private Terminal terminal;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();


    public void validateStatusTransition(OrderStatus newStatus) {
        if(!status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusException(status, newStatus);
        }
    }

    public boolean isCancellable() {
        return status == OrderStatus.PENDING;
    }

    public void confirm() {
        validateStatusTransition(OrderStatus.CONFIRMED);
        status = OrderStatus.CONFIRMED;
    }

    public void complete() {
        validateStatusTransition(OrderStatus.COMPLETED);
        status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        validateStatusTransition(OrderStatus.CANCELED);
        status = OrderStatus.CANCELED;
    }


    @Builder
    public Order(String orderNumber, int totalAmount, OrderStatus status, Terminal terminal) {
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.status = status;
        setTerminal(terminal);
    }

    private void setTerminal(Terminal terminal) {
        this.terminal = terminal;
        if (terminal != null) {
            terminal.getOrders().add(this);
        }
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
    }

    public void calculateTotalAmount() {
        totalAmount = orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }



}

