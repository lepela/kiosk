package dev.lepelaka.kiosk.repository;

import dev.lepelaka.kiosk.entity.Kiosk;
import dev.lepelaka.kiosk.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByProductId(Long productId);

    List<OrderItem> findByOrderId(Long orderId);
}
