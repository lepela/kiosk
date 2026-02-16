package dev.lepelaka.kiosk.entity;

import dev.lepelaka.kiosk.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_number", columnList = "orderNumber"),
        @Index(name = "idx_kiosk_status", columnList = "kiosk_id, status"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@ToString(exclude = {"kiosk", "orderItems"})
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
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kiosk_id", nullable = false)
    private Kiosk kiosk;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public void complete() {
        if (this.status != OrderStatus.PROCESSING) {
            throw new IllegalStateException("조리중인 주문만 완료 가능");
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if (this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("완료된 주문은 취소 불가");
        }
        this.status = OrderStatus.CANCELLED;
    }


    @Builder
    public Order(String orderNumber, int totalAmount, OrderStatus status, Kiosk kiosk) {
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.status = status;
        setKiosk(kiosk);
    }

    private void setKiosk(Kiosk kiosk) {
        this.kiosk = kiosk;
        if (kiosk != null) {
            kiosk.getOrders().add(this);
        }
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
    }

    public void calculateTotalAmount() {
        totalAmount = orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }

}

