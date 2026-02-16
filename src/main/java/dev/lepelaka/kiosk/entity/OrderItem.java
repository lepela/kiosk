package dev.lepelaka.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@ToString(exclude = {"order", "product"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class OrderItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int price;

    @Builder
    public OrderItem(Order order, Product product, int quantity, int price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public int getTotalPrice() {
        return quantity * price;
    }
}
