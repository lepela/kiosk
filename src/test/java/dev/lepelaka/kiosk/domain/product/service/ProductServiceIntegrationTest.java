package dev.lepelaka.kiosk.domain.product.service;

import dev.lepelaka.kiosk.domain.product.dto.ProductCreateRequest;
import dev.lepelaka.kiosk.domain.product.dto.ProductResponse;
import dev.lepelaka.kiosk.domain.product.dto.ProductUpdateRequest;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.global.common.dto.PageResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional // 테스트 종료 후 롤백 (데이터 초기화)
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        productRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("상품을 등록하고 조회한다.")
    void register_and_find() {
        // given
        ProductCreateRequest request = new ProductCreateRequest(
                "짜장면", 7000, 100, "맛있는 짜장면", "url", "메인"
        );

        // when
        Long savedId = productService.register(request);

        // then
        assertThat(savedId).isNotNull();

        ProductResponse response = productService.detail(savedId);
        assertThat(response.name()).isEqualTo("짜장면");
        assertThat(response.price()).isEqualTo(7000);
        assertThat(response.category()).isEqualTo("메인");
    }

    @Test
    @DisplayName("상품 정보를 수정한다.")
    void modify() {
        // given
        Product product = Product.builder()
                .name("짜장면")
                .price(7000)
                .quantity(100)
                .description("맛있는 짜장면")
                .imageUrl("url")
                .category("메인")
                .build();
        productRepository.save(product);

        ProductUpdateRequest request = new ProductUpdateRequest(
                "쟁반짜장", 8000, 50, "더 맛있는 짜장", "new_url", "메인"
        );

        // when
        productService.modify(product.getId(), request);

        // then
        ProductResponse response = productService.detail(product.getId());
        assertThat(response.name()).isEqualTo("쟁반짜장");
        assertThat(response.price()).isEqualTo(8000);
        assertThat(response.quantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("상품 정보를 수정한다. DynamicUpdate 적용")
    void modify_on_dirty() {
        // given
        Product product = Product.builder()
                .name("짜장면")
                .price(7000)
                .quantity(100)
                .description("맛있는 짜장면")
                .imageUrl("url")
                .category("메인")
                .build();
        productRepository.save(product);

        ProductUpdateRequest request = new ProductUpdateRequest(
                "쟁반짜장",
                7000, // 미수정
                50,
                null, // null
                "new_url",
                "메인"
        );

        // when
        productService.modify(product.getId(), request);

        // then
        ProductResponse response = productService.detail(product.getId());
        assertThat(response.name()).isEqualTo("쟁반짜장");
        assertThat(response.price()).isEqualTo(7000);
        assertThat(response.quantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("존재하지 않는 상품 수정 시 예외가 발생한다.")
    void modify_fail() {
        // given
        ProductUpdateRequest request = new ProductUpdateRequest(
                "쟁반짜장", 8000, 50, "더 맛있는 짜장", "new_url", "메인"
        );

        // when & then
        assertThatThrownBy(() -> productService.modify(999L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("상품이 존재하지 않습니다");
    }

    @Test
    @DisplayName("상품을 삭제(Soft Delete)한다.")
    void delete() {
        // given
        Product product = Product.builder()
                .name("짜장면")
                .price(7000)
                .quantity(100)
                .description("맛있는 짜장면")
                .imageUrl("url")
                .category("메인")
                .build();
        productRepository.save(product);

        // when
        productService.remove(product.getId());

        // then
        Product deletedProduct = productRepository.findById(product.getId()).orElseThrow();
        assertThat(deletedProduct.isActive()).isFalse();
        
        // listOnActive() 조회 시 제외되는지 확인
        PageResponse<ProductResponse> activeProducts = productService.listOnActive(PageRequest.of(0, 10));
        assertThat(activeProducts.content()).isEmpty();
    }
}