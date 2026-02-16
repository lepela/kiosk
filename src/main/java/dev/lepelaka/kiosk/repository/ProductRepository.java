package dev.lepelaka.kiosk.repository;

import dev.lepelaka.kiosk.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    List<Product> findByActiveTrue();

    List<Product> findByCategoryAndActiveTrue(String category);
}
