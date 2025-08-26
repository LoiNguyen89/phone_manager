package ra.edu.service.imp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ra.edu.model.entity.Product;
import ra.edu.repo.ProductRepository;
import ra.edu.service.ProductService;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product update(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void delete(Integer id) {
        productRepository.deleteById(id);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> searchByBrand(String brand, Pageable pageable) {
        return productRepository.findByBrandContainingIgnoreCase(brand, pageable);
    }

    @Override
    public Page<Product> searchByPriceRange(BigDecimal min, BigDecimal max, Pageable pageable) {
        return productRepository.findByPriceBetween(min, max, pageable);
    }

    @Override
    public Page<Product> searchByStock(Integer stock, Pageable pageable) {
        return productRepository.findByStockGreaterThanEqual(stock, pageable);
    }

    @Override
    public Product findById(Integer id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
    }

    @Override
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }
}
