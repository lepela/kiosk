package dev.lepelaka.kiosk.domain.order.repository;

import dev.lepelaka.kiosk.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByProductId(Long productId);

    List<OrderItem> findByOrderId(Long orderId);
}
