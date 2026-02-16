package dev.lepelaka.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "orderItems")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Product extends BaseEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int price;

    private int quantity;  // 재고 (나중에 추가)

    @Column(length = 500)
    private String description;

    private String imageUrl;

    @Column(nullable = false, length = 50)
    private String category;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Product(String name, int price, int quantity,
                   String description, String imageUrl, String category) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
    }
}