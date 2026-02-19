package dev.lepelaka.kiosk.domain.category.service;

import dev.lepelaka.kiosk.domain.category.dto.CategoryCreateRequest;
import dev.lepelaka.kiosk.domain.category.dto.CategoryResponse;
import dev.lepelaka.kiosk.domain.category.dto.CategoryUpdateRequest;
import dev.lepelaka.kiosk.domain.category.entity.Category;
import dev.lepelaka.kiosk.domain.category.exception.CategoryNotFoundException;
import dev.lepelaka.kiosk.domain.category.repository.CategoryRepository;
import dev.lepelaka.kiosk.global.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public Long create(CategoryCreateRequest request) {
        return categoryRepository.save(request.toEntity()).getId();
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void modify(Long id, CategoryUpdateRequest request) {
        Category category = getCategory(id);
        category.update(request.name(), request.description(), request.displayOrder());
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deactivate(Long id) {
        getCategory(id).deactivate();
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void activate(Long id) {
        getCategory(id).activate();
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void remove(Long id) {
        categoryRepository.delete(getCategory(id));
    }

    @Cacheable(value = "categories", key = "#id")
    public CategoryResponse get(Long id) {
        return CategoryResponse.from(getCategory(id));
    }


    @Cacheable(value = "categories", key = "':active:page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public PageResponse<CategoryResponse> listActive(Pageable pageable) {
        return PageResponse.from(categoryRepository.findByActiveTrueOrderByDisplayOrderAsc(pageable).map(CategoryResponse::from));
    }

    @Cacheable(value = "categories", key = "':all:displayorder:page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public PageResponse<CategoryResponse> listByDisplayOrder(Pageable pageable) {
        return PageResponse.from(categoryRepository.findAllByOrderByDisplayOrderAsc(pageable).map(CategoryResponse::from));
    }

    // 헬퍼 정의
    private Category getCategory(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }


}
