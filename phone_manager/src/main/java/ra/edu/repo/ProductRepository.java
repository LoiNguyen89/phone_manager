package ra.edu.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ra.edu.model.entity.Product;

import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    boolean existsByName(String name);


    Page<Product> findByBrandContainingIgnoreCase(String brand, Pageable pageable);


    Page<Product> findByPriceBetween(BigDecimal min, BigDecimal max, Pageable pageable);


    Page<Product> findByStockGreaterThanEqual(Integer stock, Pageable pageable);


}
