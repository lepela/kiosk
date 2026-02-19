package dev.lepelaka.kiosk.domain.order.service;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.category.repository.CategoryRepository;
import dev.lepelaka.kiosk.domain.order.component.OrderNumberGenerator;
import dev.lepelaka.kiosk.domain.order.dto.OrderCreateRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderItemRequest;
import dev.lepelaka.kiosk.domain.order.repository.OrderRepository;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.terminal.entity.enums.TerminalStatus;
import dev.lepelaka.kiosk.domain.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
class OrderServiceConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private OrderNumberGenerator orderNumberGenerator;

    private Terminal terminal;
    private Product product;

    @BeforeEach
    void setUp() {
        // 멀티스레드 환경에서 접근해야 하므로 @Transactional 없이 실제 DB에 저장
        terminal = terminalRepository.save(Terminal.builder().name("동시성 테스트 키오스크").status(TerminalStatus.ACTIVE).build());
        Category category = categoryRepository.save(Category.builder().name("테스트 카테고리").displayOrder(1).build());
        
        product = productRepository.save(Product.builder()
                .name("인기 상품")
                .price(1000)
                .quantity(100) // 재고 100개
                .category(category)
                .build());
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        terminalRepository.deleteAll();
    }

    @DisplayName("동시에 100개의 주문 요청이 들어오면 재고가 정확히 감소해야 한다.")
    @Test
    void concurrentOrderCreation() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger orderSeq = new AtomicInteger(0);

        // 고유한 주문 번호 생성 (중복 에러 방지)
        given(orderNumberGenerator.generate()).willAnswer(invocation -> "ORD-" + System.nanoTime() + "-" + orderSeq.incrementAndGet());

        OrderCreateRequest request = new OrderCreateRequest(List.of(new OrderItemRequest(product.getId(), 1)), terminal.getId());

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.createOrder(request);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 종료 대기

        // then
        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(0);
    }
}