package dev.lepelaka.kiosk.domain.order.service;

import dev.lepelaka.kiosk.domain.order.component.OrderNumberGenerator;
import dev.lepelaka.kiosk.domain.order.dto.OrderCreateRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderItemRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderResponse;
import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.order.entity.OrderItem;
import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import dev.lepelaka.kiosk.domain.order.exception.*;
import dev.lepelaka.kiosk.domain.order.repository.OrderItemRepository;
import dev.lepelaka.kiosk.domain.order.repository.OrderRepository;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.exception.InactiveProductException;
import dev.lepelaka.kiosk.domain.product.exception.ProductNotFoundException;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.terminal.repository.TerminalRepository;
import dev.lepelaka.kiosk.global.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        if(products.size() != productIds.size()) {
            Set<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            Set<Long> notFoundIds = new HashSet<>(productIds);
            notFoundIds.removeAll(foundIds);

            throw new ProductNotFoundException(notFoundIds);
        }
        // 5. 재고검증용, 수량 합치기
        Map<Long, Integer> quantityMap = itemRequests.stream().collect(Collectors.toMap(OrderItemRequest::productId, OrderItemRequest::quantity, Integer::sum));


        // 7. 주문번호 생성 후 주문 생성
        Order order = Order.builder()
                .terminal(terminal)
                .orderNumber(orderNumberGenerator.generate())
                .status(OrderStatus.PENDING)
                .build();

        for(Product product : products) {
            int quantity = quantityMap.get(product.getId());
            // 8. 재고감소
//            product.decreaseQuantity(quantity);
            product.order(quantity);

            // 9. 상품 스냅샷 촬영
            String productName = product.getName();
            int price = product.getPrice();

            // 10. OrderItem 생성 및 주문서에 추가
            order.addOrderItem(OrderItem.builder()
                    .quantity(quantity)
                    .productId(product.getId())
                    .price(price)
                    .productName(productName)
                    .order(order)
                    .build());
        }
        // 11. 총액 계산
        order.calculateTotalAmount();

        // 12. 주문서 저장
        orderRepository.save(order);

        return order.getId();
    }


    // 조회
    public OrderResponse getOrder(Long orderId) {
        return OrderResponse.from(orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId)));
    }

    public OrderResponse getOrder(String orderNumber) {
        return OrderResponse.from(orderRepository.findByOrderNumber(orderNumber).orElseThrow(()->new OrderNotFoundException(orderNumber)));
    }

    // 상태 변경
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.confirm();
    }

    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        order.complete();
    }

    // 주문 취소 (결제 전만 가능)
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

        if(!order.isCancellable()) {
            throw new CannotCancelOrderException(orderId, order.getStatus());
        }

        // 재고 복구
        for(OrderItem orderItem : order.getOrderItems()) {
            Product product = productRepository.findById(orderItem.getProductId()).orElseThrow(() -> new ProductNotFoundException(orderItem.getProductId()));
            product.increaseQuantity(orderItem.getQuantity());
        }
        order.cancel();
    }

    // 이하 관리자용 구현

    // 전체 주문목록
    public PageResponse<OrderResponse> getAllOrders(Pageable pageable) {
        return PageResponse.from(orderRepository.findAll(pageable).map(OrderResponse::from));
    }
    // 상태별 주문목록
    public PageResponse<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return PageResponse.from(orderRepository.findByStatus(status, pageable).map(OrderResponse::from));
    }
    // 터미널별 주문목록
    public PageResponse<OrderResponse> getOrdersByTerminal(Long terminalId, Pageable pageable) {
        return PageResponse.from(orderRepository.findByTerminalId(terminalId, pageable).map(OrderResponse::from));
    }
    // 기간별 주문목록
    public PageResponse<OrderResponse> getOrdersByPeriod(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return PageResponse.from(orderRepository.findByCreatedAtBetween(startDate, endDate, pageable).map(OrderResponse::from));
    }
    // 통계 >> DTO 정의 후 구현예정
    // 일일매출
    // 기간매출



}
