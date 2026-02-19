package dev.lepelaka.kiosk.domain.category.repository;

import dev.lepelaka.kiosk.domain.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Page<Category> findAllByOrderByDisplayOrderAsc(Pageable pageable);
    Page<Category> findByActiveTrueOrderByDisplayOrderAsc(Pageable pageable);
}