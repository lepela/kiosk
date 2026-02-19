package dev.lepelaka.kiosk.domain.product.service;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.category.repository.CategoryRepository;
import dev.lepelaka.kiosk.domain.product.dto.ProductCreateRequest;
import dev.lepelaka.kiosk.domain.product.dto.ProductResponse;
import dev.lepelaka.kiosk.domain.product.dto.ProductUpdateRequest;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        // 테스트용 카테고리 미리 생성
        category = categoryRepository.save(Category.builder()
                .name("커피")
                .description("커피입니다")
                .displayOrder(1)
                .build());
    }

    @DisplayName("상품 생성, 조회, 수정 시 캐시 동작을 검증한다.")
    @Test
    void productLifecycleAndCache() {
        // 1. 상품 생성
        ProductCreateRequest createRequest = new ProductCreateRequest(
                "아메리카노", 5000, 100, "시원한 커피", "url", category.getId()
        );
        Long productId = productService.register(createRequest);

        // 2. 상세 조회 (1차 - DB 조회 발생)
        ProductResponse response1 = productService.detail(productId);
        assertThat(response1.name()).isEqualTo("아메리카노");

        verify(productRepository, times(1)).findById(productId);

        // 3. 상세 조회 (2차 - 캐시 적중, DB 조회 안 함)
        ProductResponse response2 = productService.detail(productId);
        assertThat(response2.name()).isEqualTo("아메리카노");

        // 호출 횟수 유지 (1회)
        verify(productRepository, times(1)).findById(productId);

        // 4. 상품 수정 (캐시 무효화 @CacheEvict)
        ProductUpdateRequest updateRequest = new ProductUpdateRequest(
                "아이스 아메리카노", 5500, 100, "더 시원한 커피", "newUrl", category.getId()
        );
        productService.modify(productId, updateRequest);

        // 5. 상세 조회 (3차 - 캐시 깨짐, DB 조회 발생)
        ProductResponse response3 = productService.detail(productId);
        assertThat(response3.name()).isEqualTo("아이스 아메리카노");
        assertThat(response3.price()).isEqualTo(5500);

        // 호출 횟수 증가 (modify 내부 조회 1회 + 조회 1회 = 총 2회 추가 -> 누적 3회)
        // modify에서 findById를 호출하므로 누적 횟수를 잘 계산해야 함.
        // 1(첫조회) + 1(modify) + 1(수정후조회) = 3
        verify(productRepository, times(3)).findById(productId);
    }

    @DisplayName("활성 상품 목록 조회 시 캐싱이 적용된다.")
    @Test
    void listOnActiveCache() {
        // given
        createProduct("P1", 1000);
        createProduct("P2", 2000);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        // 1. 첫 번째 조회
        productService.listOnActive(pageable);
        verify(productRepository, times(1)).findByActiveTrue(pageable);

        // 2. 두 번째 조회 (캐시 적중)
        productService.listOnActive(pageable);

        // then
        // 호출 횟수 유지
        verify(productRepository, times(1)).findByActiveTrue(pageable);
    }

    @DisplayName("상품 삭제 시 캐시가 무효화된다.")
    @Test
    void removeEvictsCache() {
        // given
        Long productId = productService.register(new ProductCreateRequest("삭제할상품", 1000, 10, "desc", "url", category.getId()));
        productService.detail(productId); // 캐싱 (findById 1회)

        // when
        productService.remove(productId); // 삭제 (내부 findById 1회 -> 누적 2회)

        // then
        // 다시 조회 시 DB 호출 발생해야 함 (캐시가 지워졌으므로)
        productService.detail(productId); // (findById 1회 -> 누적 3회)

        verify(productRepository, times(3)).findById(productId);
    }

    private void createProduct(String name, int price) {
        productService.register(new ProductCreateRequest(name, price, 10, "desc", "url", category.getId()));
    }
}