package dev.lepelaka.kiosk.domain.category.service;

import dev.lepelaka.kiosk.domain.category.dto.CategoryCreateRequest;
import dev.lepelaka.kiosk.domain.category.dto.CategoryResponse;
import dev.lepelaka.kiosk.domain.category.dto.CategoryUpdateRequest;
import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.category.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoSpyBean
    private CategoryRepository categoryRepository;

    @DisplayName("카테고리 생성, 조회, 수정 사이클 및 캐시 동작을 검증한다.")
    @Test
    void categoryLifecycleAndCache() {
        // 1. 생성
        CategoryCreateRequest createRequest = new CategoryCreateRequest("커피", "원두 커피", 1);
        Long savedId = categoryService.create(createRequest);

        assertThat(savedId).isNotNull();

        // 2. 조회 (1차 - DB 조회 발생)
        CategoryResponse response1 = categoryService.get(savedId);
        assertThat(response1.name()).isEqualTo("커피");

        verify(categoryRepository, times(1)).findById(savedId);

        // 3. 조회 (2차 - 캐시 적중, DB 조회 안 함)
        CategoryResponse response2 = categoryService.get(savedId);
        assertThat(response2.name()).isEqualTo("커피");

        // 호출 횟수가 여전히 1이어야 함
        verify(categoryRepository, times(1)).findById(savedId);

        // 4. 수정 (캐시 무효화 발생 @CacheEvict)
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest("디카페인", "카페인 없는 커피", 2);
        categoryService.modify(savedId, updateRequest);

        // 5. 조회 (3차 - 캐시가 깨졌으므로 DB 조회 발생)
        CategoryResponse response3 = categoryService.get(savedId);
        assertThat(response3.name()).isEqualTo("디카페인");
        assertThat(response3.displayOrder()).isEqualTo(2);

        // 호출 횟수가 3으로 증가해야 함
        // 1. 첫 조회(Miss) + 2. modify 내부 조회 + 3. 수정 후 조회(Miss)
        verify(categoryRepository, times(3)).findById(savedId);
    }

    @DisplayName("목록 조회 시 캐싱이 적용된다.")
    @Test
    void listCaching() {
        // given
        categoryService.create(new CategoryCreateRequest("A", "Desc A", 1));
        categoryService.create(new CategoryCreateRequest("B", "Desc B", 2));
        
        Pageable pageable = PageRequest.of(0, 10);

        // when
        // 1. 첫 번째 조회
        categoryService.listActive(pageable);
        verify(categoryRepository, times(1)).findByActiveTrueOrderByDisplayOrderAsc(pageable);

        // 2. 두 번째 조회 (캐시 적중)
        categoryService.listActive(pageable);
        
        // then
        // 리포지토리 호출 횟수가 증가하지 않아야 함
        verify(categoryRepository, times(1)).findByActiveTrueOrderByDisplayOrderAsc(pageable);
    }

    @DisplayName("카테고리 비활성화 시 상태가 변경되고 캐시가 정리된다.")
    @Test
    void deactivate() {
        // given
        Long id = categoryService.create(new CategoryCreateRequest("테스트", "설명", 1));
        
        // 캐시 워밍 (조회하여 캐시에 적재)
        categoryService.get(id);

        // when
        categoryService.deactivate(id);

        // then
        Category category = categoryRepository.findById(id).orElseThrow();
        assertThat(category.isActive()).isFalse();

        // 캐시가 지워졌는지 확인하기 위해 다시 조회 시 리포지토리 호출 확인
        // (create 1회 + get 1회 + deactivate(내부조회) 1회 + 검증용 findById 1회 = 총 4회 호출 상태)
        // 여기서 다시 get을 호출하면 5회가 되어야 함 (캐시가 살아있다면 안 늘어남)
        
        // Mockito count reset for clarity
        org.mockito.Mockito.clearInvocations(categoryRepository);
        
        categoryService.get(id);
        
        // deactivate에 @CacheEvict가 있으므로 다시 DB를 조회해야 함
        verify(categoryRepository, times(1)).findById(id);
    }
}