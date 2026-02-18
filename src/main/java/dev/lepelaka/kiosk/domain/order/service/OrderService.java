package dev.lepelaka.kiosk.domain.order.service;

import dev.lepelaka.kiosk.domain.order.component.OrderNumberGenerator;
import dev.lepelaka.kiosk.domain.order.dto.OrderCreateRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderItemRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderResponse;
import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.order.entity.OrderItem;
import dev.lepelaka.kiosk.domain.order.exception.InsufficientStockException;
import dev.lepelaka.kiosk.domain.order.exception.OrderErrorCode;
import dev.lepelaka.kiosk.domain.order.exception.OrderException;
import dev.lepelaka.kiosk.domain.order.exception.TerminalNotFoundException;
import dev.lepelaka.kiosk.domain.order.repository.OrderItemRepository;
import dev.lepelaka.kiosk.domain.order.repository.OrderRepository;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.exception.InactiveProductException;
import dev.lepelaka.kiosk.domain.product.exception.ProductNotFoundException;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.terminal.repository.TerminalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TerminalRepository terminalRepository;
    private final ProductRepository productRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    @Transactional
    public Long createOrder(OrderCreateRequest request) {
        // 1. 주문한 단말기 확인
        Long terminalId = request.terminalId();
        Terminal terminal =terminalRepository.findById(terminalId).orElseThrow(() -> new TerminalNotFoundException(terminalId));

        // 2. 정렬을 통한 데드락 방지 및 id 추출후 리스트로 전환
        List<OrderItemRequest> itemRequests = request.orderItems();
        List<Long> productIds = itemRequests.stream()
                .map(OrderItemRequest::productId)
                .distinct()
                .sorted() // 데드락방지
                .toList();

        // 3. 비관락 적용
        List<Product> products = productRepository.findAllByIdWithPessimisticLock(productIds);

        // 4. 상품확인 프로세스
        if(products.size() != itemRequests.size()) {
            Set<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            Set<Long> notFoundIds = new HashSet<>(productIds);
            notFoundIds.removeAll(foundIds);

            throw new ProductNotFoundException(notFoundIds);
        }
        // 5. 재고검증용, 수량 합치기
        Map<Long, Integer> quantityMap = itemRequests.stream().collect(Collectors.toMap(OrderItemRequest::productId, OrderItemRequest::quantity, Integer::sum));


        // 6. 상품별 재고, 비활성 체크
        for(Product product : products) {
            if(!product.isActive()) {
                throw new InactiveProductException(product.getId());
            }
            int quantity = quantityMap.get(product.getId());
            if (product.getQuantity() < quantity) {
                throw new InsufficientStockException(product.getId(), quantity, product.getQuantity());
            }
        }
        // 7. 주문번호 생성 후 주문 생성
        Order order = Order.builder()
                .terminal(terminal)
                .orderNumber(orderNumberGenerator.generate())
                .build();


        for(Product product : products) {
            int quantity = quantityMap.get(product.getId());
            // 8. 재고감소
            product.decreaseQuantity(quantity);

            // 9. 상품 스냅샷 촬영
            String productName = product.getName();
            int price = product.getPrice();

            // 10. OrderItem 생성 및 주문서에 추가
            order.addOrderItem(OrderItem.builder()
                    .quantity(quantity)
                    .productId(product.getId())
                    .price(price)
                    .productName(productName)
                    .build());
        }
        // 11. 총액 계산
        order.calculateTotalAmount();

        // 12. 주문서 저장
        orderRepository.save(order);

        return order.getId();
    }

    public OrderResponse getOrder(Long orderId) {
        return OrderResponse.from(orderRepository.findById(orderId).orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_CREATION_FAILED)));
    }
}
