package ra.edu.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ra.edu.model.entity.Product;

import java.math.BigDecimal;

public interface ProductService {
    Product save(Product product);
    Product update(Product product);
    void delete(Integer id);
    Page<Product> findAll(Pageable pageable);
    Page<Product> searchByBrand(String brand, Pageable pageable);
    Page<Product> searchByPriceRange(BigDecimal min, BigDecimal max, Pageable pageable);
    Page<Product> searchByStock(Integer stock, Pageable pageable);
    Product findById(Integer id);
    boolean existsByName(String name);
}
