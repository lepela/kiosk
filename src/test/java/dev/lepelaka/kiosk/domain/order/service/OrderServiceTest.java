package dev.lepelaka.kiosk.domain.order.service;

import dev.lepelaka.kiosk.domain.order.component.OrderNumberGenerator;
import dev.lepelaka.kiosk.domain.order.dto.OrderCreateRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderItemRequest;
import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.order.entity.OrderItem;
import dev.lepelaka.kiosk.domain.order.entity.enums.OrderStatus;
import dev.lepelaka.kiosk.domain.order.exception.InsufficientStockException;
import dev.lepelaka.kiosk.domain.order.repository.OrderRepository;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.exception.ProductNotFoundException;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TerminalRepository terminalRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    @DisplayName("주문 생성 시 재고가 감소하고 주문이 저장된다.")
    @Test
    void createOrder() {
        // given
        Long terminalId = 1L;
        Long productId = 100L;
        int requestQuantity = 2;
        int initialStock = 10;

        OrderCreateRequest request = new OrderCreateRequest(
                                List.of(new OrderItemRequest(productId, requestQuantity)), terminalId
        );

        Terminal terminal = mock(Terminal.class);
        Product product = mock(Product.class);

        given(terminalRepository.findById(terminalId)).willReturn(Optional.of(terminal));
        given(productRepository.findAllByIdWithPessimisticLock(anyList())).willReturn(List.of(product));
        given(orderNumberGenerator.generate()).willReturn("20231010-0001");

        // Product Mock 설정
        given(product.getId()).willReturn(productId);
        given(product.isActive()).willReturn(true);
        given(product.getQuantity()).willReturn(initialStock);
        given(product.getPrice()).willReturn(5000);
        given(product.getName()).willReturn("아메리카노");

        // when
        orderService.createOrder(request);

        // then
        // 1. 재고 감소 메서드가 호출되었는지 검증
        verify(product, times(1)).decreaseQuantity(requestQuantity);
        
        // 2. 주문 저장 메서드가 호출되었는지 검증
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @DisplayName("주문 생성 시 존재하지 않는 상품이 포함되어 있으면 예외가 발생한다.")
    @Test
    void createOrderWithInvalidProduct() {
        // given
        Long terminalId = 1L;
        Long validProductId = 100L;
        Long invalidProductId = 999L;

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(
                        new OrderItemRequest(validProductId, 1),
                        new OrderItemRequest(invalidProductId, 1)
                ),terminalId
        );

        Terminal terminal = mock(Terminal.class);
        Product product = mock(Product.class);

        given(terminalRepository.findById(terminalId)).willReturn(Optional.of(terminal));
        // 유효한 상품 1개만 리턴됨 (요청은 2개)
        given(productRepository.findAllByIdWithPessimisticLock(anyList())).willReturn(List.of(product));
        given(product.getId()).willReturn(validProductId);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @DisplayName("주문 생성 시 재고보다 많은 수량을 요청하면 예외가 발생한다.")
    @Test
    void createOrderWithInsufficientStock() {
        // given
        Long terminalId = 1L;
        Long productId = 100L;
        int requestQuantity = 5;
        int currentStock = 3;

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(productId, requestQuantity)),
                terminalId
        );

        Terminal terminal = mock(Terminal.class);
        Product product = mock(Product.class);

        given(terminalRepository.findById(terminalId)).willReturn(Optional.of(terminal));
        given(productRepository.findAllByIdWithPessimisticLock(anyList())).willReturn(List.of(product));

        given(product.getId()).willReturn(productId);
        given(product.isActive()).willReturn(true);
        given(product.getQuantity()).willReturn(currentStock); // 재고 부족 설정

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining(String.valueOf(productId));
    }

    @DisplayName("주문 취소 시 상품 재고가 복구되고 주문이 취소 상태로 변경된다.")
    @Test
    void cancelOrder() {
        // given
        Long orderId = 1L;
        Long productId = 100L;
        int quantity = 2;

        Order order = mock(Order.class);
        OrderItem orderItem = mock(OrderItem.class);
        Product product = mock(Product.class);

        // Order Mock 설정
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(order.isCancellable()).willReturn(true);
        given(order.getOrderItems()).willReturn(List.of(orderItem));

        // OrderItem Mock 설정
        given(orderItem.getProductId()).willReturn(productId);
        given(orderItem.getQuantity()).willReturn(quantity);

        // Product Mock 설정
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        orderService.cancelOrder(orderId);

        // then
        // 1. 재고 증가(복구) 호출 검증
        verify(product, times(1)).increaseQuantity(quantity);
        
        // 2. 주문 취소 처리 호출 검증
        verify(order, times(1)).cancel();
    }
}