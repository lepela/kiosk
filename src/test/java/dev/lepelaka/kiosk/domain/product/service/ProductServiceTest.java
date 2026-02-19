package dev.lepelaka.kiosk.domain.product.service;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.category.exception.CategoryNotFoundException;
import dev.lepelaka.kiosk.domain.category.repository.CategoryRepository;
import dev.lepelaka.kiosk.domain.product.dto.ProductCreateRequest;
import dev.lepelaka.kiosk.domain.product.dto.ProductResponse;
import dev.lepelaka.kiosk.domain.product.dto.ProductUpdateRequest;
import dev.lepelaka.kiosk.domain.product.entity.Product;
import dev.lepelaka.kiosk.domain.product.exception.DuplicateProductException;
import dev.lepelaka.kiosk.domain.product.exception.ProductNotFoundException;
import dev.lepelaka.kiosk.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @DisplayName("상품을 등록하면 저장된 ID를 반환한다.")
    @Test
    void register() {
        // given
        ProductCreateRequest request = new ProductCreateRequest("아메리카노", 5000, 100, "설명", "url", 1L);

        Product product = Product.builder().name("아메리카노").build();
        ReflectionTestUtils.setField(product, "id", 1L);
        
        Category category = Category.builder().name("커피").build();
        ReflectionTestUtils.setField(category, "id", 1L);

        given(productRepository.existsByName(request.name())).willReturn(false);

        given(categoryRepository.findById(request.categoryId())).willReturn(Optional.of(category));

        given(productRepository.save(any(Product.class))).willReturn(product);

        // when
        Long savedId = productService.register(request);

        // then
        assertThat(savedId).isEqualTo(1L);
        verify(productRepository).save(any(Product.class));
    }

    @DisplayName("이미 존재하는 상품명으로 등록하면 예외가 발생한다.")
    @Test
    void register_Duplicate() {
        // given
        ProductCreateRequest request = new ProductCreateRequest("아메리카노", 5000, 100, "설명", "url", 1L);
        given(productRepository.existsByName(request.name())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> productService.register(request))
                .isInstanceOf(DuplicateProductException.class);
    }

    @DisplayName("상품 정보를 수정한다.")
    @Test
    void modify() {
        // given
        Long productId = 1L;
        Long categoryId = 2L;
        ProductUpdateRequest request = new ProductUpdateRequest("라떼", 5500, 50, "우유 듬뿍", "newUrl", categoryId);

        Product product = Product.builder().name("아메리카노").price(5000).build();
        Category category = Category.builder().name("커피").build();
        ReflectionTestUtils.setField(category, "id", categoryId);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        productService.modify(productId, request);

        // then
        assertThat(product.getName()).isEqualTo("라떼");
        assertThat(product.getPrice()).isEqualTo(5500);
        assertThat(product.getCategory()).isEqualTo(category);
    }

    @DisplayName("존재하지 않는 상품을 수정하려 하면 예외가 발생한다.")
    @Test
    void modify_ProductNotFound() {
        // given
        Long productId = 999L;
        ProductUpdateRequest request = new ProductUpdateRequest("라떼", 5500, 50, "desc", "url", 1L);
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.modify(productId, request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @DisplayName("수정 시 존재하지 않는 카테고리를 지정하면 예외가 발생한다.")
    @Test
    void modify_CategoryNotFound() {
        // given
        Long productId = 1L;
        Long categoryId = 999L;
        ProductUpdateRequest request = new ProductUpdateRequest("라떼", 5500, 50, "desc", "url", categoryId);
        Product product = mock(Product.class);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.modify(productId, request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @DisplayName("상품 상세 정보를 조회한다.")
    @Test
    void detail() {
        // given
        Long productId = 1L;
        Product product = Product.builder().name("아메리카노").price(5000).build();
        ReflectionTestUtils.setField(product, "id", productId);
        
        // ProductResponse.fromEntity 내부에서 category 접근 시 NPE 방지용
        Category category = Category.builder().name("커피").build();
        ReflectionTestUtils.setField(product, "category", category);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        ProductResponse response = productService.detail(productId);

        // then
        assertThat(response.name()).isEqualTo("아메리카노");
        assertThat(response.price()).isEqualTo(5000);
    }

    @DisplayName("상품을 삭제(비활성화)한다.")
    @Test
    void remove() {
        // given
        Long productId = 1L;
        Product product = mock(Product.class); // 행위 검증을 위해 Mock 사용
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        productService.remove(productId);

        // then
        verify(product).deactivate(); // Product 엔티티의 deactivate 메서드 호출 검증
    }
}