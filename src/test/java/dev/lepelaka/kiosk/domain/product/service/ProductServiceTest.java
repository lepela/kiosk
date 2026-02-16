package dev.lepelaka.kiosk.domain.product.service;

import dev.lepelaka.kiosk.domain.product.dto.ProductCreateRequest;
import dev.lepelaka.kiosk.domain.product.dto.ProductResponse;
import dev.lepelaka.kiosk.domain.product.dto.ProductUpdateRequest;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import dev.lepelaka.kiosk.global.common.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Test
    @DisplayName("상품 등록 성공")
    void register() {
        // given
        ProductCreateRequest request = new ProductCreateRequest(
                "짜장면", 7000, 100, "맛있는 짜장면", "url", "메인"
        );
        Product product = request.toEntity();
        
        // Mocking: save 호출 시 product 반환
        given(productRepository.save(any(Product.class))).willReturn(product);

        // when
        Long savedId = productService.register(request);

        // then
        assertThat(savedId).isEqualTo(product.getId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 수정 성공")
    void modify_success() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .name("짜장면")
                .price(7000)
                .quantity(100)
                .description("맛있는 짜장면")
                .imageUrl("url")
                .category("메인")
                .build();

        // Mocking: findById 호출 시 product 반환
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        ProductUpdateRequest request = new ProductUpdateRequest(
                "쟁반짜장", 8000, 50, "더 맛있는 짜장", "new_url", "메인"
        );

        // when
        productService.modify(productId, request);

        // then
        assertThat(product.getName()).isEqualTo("쟁반짜장");
        assertThat(product.getPrice()).isEqualTo(8000);
        assertThat(product.getQuantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("상품 수정 실패 - 존재하지 않는 상품")
    void modify_fail_not_found() {
        // given
        Long productId = 999L;
        ProductUpdateRequest request = new ProductUpdateRequest(
                "쟁반짜장", 8000, 50, "더 맛있는 짜장", "new_url", "메인"
        );

        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.modify(productId, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("상품이 존재하지 않습니다");
    }

    @Test
    @DisplayName("상품 삭제 성공 (Soft Delete)")
    void delete_success() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .name("짜장면")
                .price(7000)
                .quantity(100)
                .description("맛있는 짜장면")
                .imageUrl("url")
                .category("메인")
                .build();

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        productService.remove(productId);

        // then
        assertThat(product.isActive()).isFalse(); // BaseEntity의 active 필드 확인
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    void detail_success() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
                .name("짜장면")
                .price(7000)
                .quantity(100)
                .description("맛있는 짜장면")
                .imageUrl("url")
                .category("메인")
                .build();

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        ProductResponse response = productService.detail(productId);

        // then
        assertThat(response.name()).isEqualTo("짜장면");
        assertThat(response.price()).isEqualTo(7000);
    }

    @Test
    @DisplayName("상품 목록 조회 (페이징)")
    void list() {
        // given
        Product p1 = Product.builder().name("짜장면").price(7000).quantity(100).description("맛있는 짜장면").imageUrl("url").category("메인").build();
        Product p2 = Product.builder().name("짬뽕").price(8000).quantity(100).description("맛있는 짬뽕").imageUrl("url").category("메인").build();
        
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(p1, p2), pageRequest, 2);

        given(productRepository.findAll(any(Pageable.class))).willReturn(productPage);

        // when
        PageResponse<ProductResponse> response = productService.list(pageRequest);

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.content()).extracting("name").containsExactly("짜장면", "짬뽕");
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.totalPages()).isEqualTo(1);
    }
}