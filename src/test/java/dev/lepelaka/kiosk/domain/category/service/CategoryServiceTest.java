package dev.lepelaka.kiosk.domain.category.service;

import dev.lepelaka.kiosk.domain.category.dto.CategoryCreateRequest;
import dev.lepelaka.kiosk.domain.category.dto.CategoryResponse;
import dev.lepelaka.kiosk.domain.category.dto.CategoryUpdateRequest;
import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.category.exception.CategoryNotFoundException;
import dev.lepelaka.kiosk.domain.category.repository.CategoryRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @DisplayName("카테고리를 생성하면 저장된 ID를 반환한다.")
    @Test
    void create() {
        // given
        CategoryCreateRequest request = new CategoryCreateRequest("커피", "맛있는 커피", 1);
        Category category = request.toEntity();
        ReflectionTestUtils.setField(category, "id", 1L);

        given(categoryRepository.save(any(Category.class))).willReturn(category);

        // when
        Long savedId = categoryService.create(request);

        // then
        assertThat(savedId).isEqualTo(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @DisplayName("존재하는 카테고리 정보를 수정한다.")
    @Test
    void modify() {
        // given
        Long categoryId = 1L;
        CategoryUpdateRequest request = new CategoryUpdateRequest("라떼", "우유가 들어간 커피", 2);
        Category category = Category.builder().name("커피").description("설명").displayOrder(1).build();
        ReflectionTestUtils.setField(category, "id", categoryId);

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        categoryService.modify(categoryId, request);

        // then
        assertThat(category.getName()).isEqualTo("라떼");
        assertThat(category.getDescription()).isEqualTo("우유가 들어간 커피");
        assertThat(category.getDisplayOrder()).isEqualTo(2);
    }

    @DisplayName("존재하지 않는 카테고리를 수정하려 하면 예외가 발생한다.")
    @Test
    void modify_NotFound() {
        // given
        Long categoryId = 999L;
        CategoryUpdateRequest request = new CategoryUpdateRequest("라떼", "설명", 1);

        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.modify(categoryId, request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @DisplayName("카테고리를 비활성화한다.")
    @Test
    void deactivate() {
        // given
        Long categoryId = 1L;
        Category category = Category.builder().build();
        ReflectionTestUtils.setField(category, "id", categoryId);

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        categoryService.deactivate(categoryId);

        // then
        assertThat(category.isActive()).isFalse();
    }

    @DisplayName("카테고리를 삭제한다.")
    @Test
    void remove() {
        // given
        Long categoryId = 1L;
        Category category = Category.builder().build();
        ReflectionTestUtils.setField(category, "id", categoryId);

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        categoryService.remove(categoryId);

        // then
        verify(categoryRepository).delete(category);
    }

    @DisplayName("활성화된 카테고리 목록을 페이징하여 조회한다.")
    @Test
    void listActive() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        Category c1 = Category.builder().name("커피").build();
        ReflectionTestUtils.setField(c1, "id", 1L);
        
        Category c2 = Category.builder().name("디저트").build();
        ReflectionTestUtils.setField(c2, "id", 2L);

        List<Category> categories = List.of(c1, c2);
        Page<Category> page = new PageImpl<>(categories, pageable, categories.size());

        given(categoryRepository.findByActiveTrueOrderByDisplayOrderAsc(pageable)).willReturn(page);

        // when
        PageResponse<CategoryResponse> response = categoryService.listActive(pageable);

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.content()).extracting("name").containsExactly("커피", "디저트");
        assertThat(response.totalElements()).isEqualTo(2);
    }
}