package dev.lepelaka.kiosk.domain.order.service;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.category.repository.CategoryRepository;
import dev.lepelaka.kiosk.domain.order.component.OrderNumberGenerator;
import dev.lepelaka.kiosk.domain.order.dto.OrderCreateRequest;
import dev.lepelaka.kiosk.domain.order.dto.OrderItemRequest;
import dev.lepelaka.kiosk.domain.order.entity.Order;
import dev.lepelaka.kiosk.domain.order.exception.InsufficientStockException;
import dev.lepelaka.kiosk.domain.order.repository.OrderRepository;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.domain.terminal.entity.Terminal;
import dev.lepelaka.kiosk.domain.terminal.entity.enums.TerminalStatus;
import dev.lepelaka.kiosk.domain.terminal.repository.TerminalRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Testcontainers
public class OrderServiceConcurrencyByMysqlAndRedisTest {
    private static final int CONCURRENT_REQUEST_COUNT = 100;
    private static final int INITIAL_STOCK_QUANTITY = 10;

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);
        r.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.show-sql", () -> "true");
        r.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQLDialect");

        r.add("spring.data.redis.host", redis::getHost);
        r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        r.add("spring.data.redis.password", () -> ""); // 컨테이너는 기본적으로 비밀번호가 없으므로 설정 덮어쓰기
    }

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

    @Autowired
    private OrderNumberGenerator orderNumberGenerator;

    private Terminal terminal;
    private Product product;

    @BeforeEach
    void setUp() {
        terminal = terminalRepository.save(Terminal.builder().name("MySQL 테스트 키오스크").build());
        Category category = categoryRepository.save(Category.builder().name("테스트 카테고리").displayOrder(1).build());

        // 재고 10개 설정
        product = productRepository.save(Product.builder()
                .name("한정판 상품")
                .price(10000)
                .quantity(INITIAL_STOCK_QUANTITY)
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

    @DisplayName("재고보다 많은 주문이 동시에 들어오면, 재고만큼만 성공하고 나머지는 품절 예외가 발생해야 한다.")
    @Test
    void checkOverselling() throws InterruptedException {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUEST_COUNT);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        OrderCreateRequest request = new OrderCreateRequest(List.of(new OrderItemRequest(product.getId(), 1)), terminal.getId());

        // when
        try {
            for (int i = 0; i < CONCURRENT_REQUEST_COUNT; i++) {
                executorService.submit(() -> {
                    try {
                        orderService.createOrder(request);
                        successCount.incrementAndGet();
                    } catch (InsufficientStockException e) {
                        failCount.incrementAndGet(); // 재고 부족 예외 발생 시 카운트
                    } catch (Exception e) {
                        e.printStackTrace(); // 예상치 못한 예외는 로그 출력 (카운트 집계 제외)
                    } finally {
                        latch.countDown();
                    }
                });
            }
            // 타임아웃 설정: 10초 내에 끝나지 않으면 데드락 가능성 있음
            boolean finished = latch.await(10, TimeUnit.SECONDS);
            assertThat(finished).isTrue().as("테스트가 시간 내에 완료되지 않았습니다.");
        } finally {
            executorService.shutdown();
        }

        // then
        assertThat(successCount.get()).isEqualTo(INITIAL_STOCK_QUANTITY); // 성공은 딱 재고만큼
        assertThat(failCount.get()).isEqualTo(CONCURRENT_REQUEST_COUNT - INITIAL_STOCK_QUANTITY); // 나머지는 실패

        Product finalProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(finalProduct.getQuantity()).isEqualTo(0); // 재고는 0이어야 함 (음수 불가)

        // DB 데이터 정합성 검증: 실제 저장된 주문 개수 확인
        List<Order> orders = orderRepository.findAll();
        log.info("=============== 실제 처리된 주문 ===============");
        orders.forEach(order -> log.info("{}", order));
        assertThat(orders).hasSize(INITIAL_STOCK_QUANTITY);
    }
}
