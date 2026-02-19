package dev.lepelaka.kiosk.domain.product.entity;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.order.exception.InsufficientStockException;
import dev.lepelaka.kiosk.domain.product.exception.InactiveProductException;
import dev.lepelaka.kiosk.domain.product.exception.InvalidQuantityException;
import dev.lepelaka.kiosk.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "category")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@DynamicUpdate
public class Product extends BaseEntity {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int price;

    private int quantity;

    @Column(length = 500)
    private String description;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Version
    private Long version; // 낙관락 적용 목적

    @Builder
    public Product(String name, int price, int quantity,
                   String description, String imageUrl, Category category) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public void update(String name, int price, int quantity, String description, String imageUrl, Category category ) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
    }
    void increaseQuantity(int requestedQuantity) {
        if(requestedQuantity <= 0) throw new InvalidQuantityException(id, requestedQuantity);
        this.quantity += requestedQuantity;
    }

    void decreaseQuantity(int requestedQuantity) {
        if(requestedQuantity <= 0) throw new InvalidQuantityException(id, requestedQuantity);
        if (this.quantity < requestedQuantity) {
            throw new InsufficientStockException(id, requestedQuantity, this.quantity);
        }
        this.quantity -= requestedQuantity;
    }

    public void validateActive() {
        if(!isActive()) {
            throw new InactiveProductException(id);
        }
    }
    /**
     * 주문 도메인 행위.
     * - 활성 상품인지 검증
     * - 재고 충분성 검증
     * - 재고 차감 수행
     */
    public void order(int requestedQuantity) {
        validateActive();
        decreaseQuantity(requestedQuantity);
    }

    public void restore(int requestedQuantity) {
        validateActive();
        increaseQuantity(requestedQuantity);
    }

}