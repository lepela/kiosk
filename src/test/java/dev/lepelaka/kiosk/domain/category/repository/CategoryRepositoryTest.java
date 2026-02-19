package dev.lepelaka.kiosk.domain.category.repository;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Slf4j
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리 저장 및 조회")
    void save_and_find() {
        // given
        Category category = Category.builder()
                .name("메인")
                .description("메인 메뉴입니다.")
                .displayOrder(1)
                .build();

        // when
        Category saved = categoryRepository.save(category);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("메인");
        assertThat(saved.getDescription()).isEqualTo("메인 메뉴입니다.");
        assertThat(saved.getDisplayOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("활성 카테고리 조회")
    void findByActiveTrue() {
        // given
        Category activeCategory = Category.builder()
                .name("메인")
                .displayOrder(1)
                .build();
        
        Category inactiveCategory = Category.builder()
                .name("비활성")
                .displayOrder(2)
                .build();
        inactiveCategory.deactivate(); // 비활성화

        Pageable pageable = PageRequest.of(0, 10);

        categoryRepository.saveAll(List.of(activeCategory, inactiveCategory));

        // when
        Page<Category> activeCategories = categoryRepository.findByActiveTrueOrderByDisplayOrderAsc(pageable);

        activeCategories.forEach(c -> log.info("{}", c));
        // then
        assertThat(activeCategories).hasSize(1);
        assertThat(activeCategories).extracting("name").contains("메인");
        assertThat(activeCategories).extracting("name").doesNotContain("비활성");
    }

    @Test
    @DisplayName("활성 카테고리만 displayOrder 오름차순으로 페이징 조회된다")
    void findActiveCategories_sortedAndPaged() {
        // given
        Category active1 = Category.builder().name("메인").displayOrder(2).build();
        Category active2 = Category.builder().name("사이드").displayOrder(1).build();
        Category inactive = Category.builder().name("비활성").displayOrder(3).build();
        inactive.deactivate();

        categoryRepository.saveAll(List.of(active1, active2, inactive));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Category> result = categoryRepository.findByActiveTrueOrderByDisplayOrderAsc(pageable);

        // then
        // page meta
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();

        // content
        List<Category> content = result.getContent();
        assertThat(content)
                .extracting(Category::getName)
                .containsExactly("사이드", "메인"); // 정렬까지 검증

        assertThat(content)
                .allMatch(Category::isActive); // active=true 조건 직접 검증
    }

    @Test
    @DisplayName("카테고리 순서대로 조회")
    void findAllByOrderByDisplayOrderAsc() {
        // given
        Category category1 = Category.builder().name("사이드").displayOrder(2).build();
        Category category2 = Category.builder().name("메인").displayOrder(1).build();
        Category category3 = Category.builder().name("음료").displayOrder(3).build();
        Pageable pageable = PageRequest.of(0, 10);

        categoryRepository.saveAll(List.of(category1, category2, category3));

        // when
        Page<Category> categories = categoryRepository.findAllByOrderByDisplayOrderAsc(pageable);

        // then
        assertThat(categories).hasSize(3);
        assertThat(categories).extracting("name")
                .containsExactly("메인", "사이드", "음료");
    }
}