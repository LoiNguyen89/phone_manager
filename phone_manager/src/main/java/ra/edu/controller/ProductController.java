package ra.edu.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ra.edu.model.entity.Product;
import ra.edu.service.ProductService;
import ra.edu.service.imp.CloudinaryService;

import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final CloudinaryService cloudinaryService;
    private final ProductService productService;

    @GetMapping
    public String listProducts(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(required = false) String sortDir,
                               Model model) {

        Pageable pageable;

        if ("asc".equalsIgnoreCase(sortDir)) {
            pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        } else if ("desc".equalsIgnoreCase(sortDir)) {
            pageable = PageRequest.of(page, size, Sort.by("price").descending());
        } else {
            pageable = PageRequest.of(page, size, Sort.by("id").ascending()); // Bỏ sắp xếp -> sort id tăng dần
            sortDir = "none"; // để dùng highlight trên view
        }

        Page<Product> products = productService.findAll(pageable);
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("sortDir", sortDir);
        return "product/list";
    }


    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        return "product/add";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("product") Product product,
                       BindingResult bindingResult,
                       @RequestParam("imageFile") MultipartFile file,
                       Model model) {

        if (file == null || file.isEmpty()) {
            bindingResult.rejectValue("image", "NotNull", "Ảnh sản phẩm không được để trống");
        }
        if (productService.existsByName(product.getName())) {
            bindingResult.rejectValue("name", "Duplicate", "Tên sản phẩm đã tồn tại");
        }
        if (bindingResult.hasErrors()) {
            return "product/add";
        }
        try {
            String imageUrl = cloudinaryService.uploadFile(file);
            product.setImage(imageUrl);
            productService.save(product);
        } catch (Exception e) {
            model.addAttribute("uploadError", "Upload ảnh thất bại: " + e.getMessage());
            return "product/add";
        }
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "product/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Integer id,
                                @Valid @ModelAttribute Product product,
                                BindingResult bindingResult,
                                @RequestParam("imageFile") MultipartFile file,
                                Model model) {
        product.setId(id);

        if (bindingResult.hasErrors()) {
            return "product/edit";
        }
        if (!setImage(product, file, model, false)) {
            return "product/edit";
        }
        productService.update(product);
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        productService.delete(id);
        return "redirect:/products";
    }

    // Tìm kiếm theo brand
    @GetMapping("/search/brand")
    public String searchByBrand(@RequestParam String brand,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size,
                                @RequestParam(defaultValue = "asc") String sortDir,
                                Model model) {
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("asc") ? Sort.by("price").ascending() : Sort.by("price").descending());
        Page<Product> products = productService.searchByBrand(brand, pageable);
        model.addAttribute("products", products);
        model.addAttribute("searchType", "brand");
        model.addAttribute("keyword", brand);
        model.addAttribute("sortDir", sortDir);
        return "product/list";
    }

    // Tìm kiếm theo price range
    @GetMapping("/search/price")
    public String searchByPrice(@RequestParam BigDecimal min,
                                @RequestParam BigDecimal max,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size,
                                @RequestParam(defaultValue = "asc") String sortDir,
                                Model model) {
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("asc") ? Sort.by("price").ascending() : Sort.by("price").descending());
        Page<Product> products = productService.searchByPriceRange(min, max, pageable);
        model.addAttribute("products", products);
        model.addAttribute("searchType", "price");
        model.addAttribute("min", min);
        model.addAttribute("max", max);
        model.addAttribute("sortDir", sortDir);
        return "product/list";
    }

    // Tìm kiếm theo stock
    @GetMapping("/search/stock")
    public String searchByStock(@RequestParam Integer stock,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size,
                                @RequestParam(defaultValue = "asc") String sortDir,
                                Model model) {
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("asc") ? Sort.by("price").ascending() : Sort.by("price").descending());
        Page<Product> products = productService.searchByStock(stock, pageable);
        model.addAttribute("products", products);
        model.addAttribute("searchType", "stock");
        model.addAttribute("stock", stock);
        model.addAttribute("sortDir", sortDir);
        return "product/list";
    }

    private boolean setImage(Product product, MultipartFile file, Model model, boolean isNew) {
        try {
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(file);
                product.setImage(imageUrl);
            } else if (!isNew) {
                Product existing = productService.findById(product.getId());
                product.setImage(existing.getImage());
            }
            return true;
        } catch (IOException e) {
            model.addAttribute("error", "Có lỗi khi upload file: " + e.getMessage());
            return false;
        }
    }
}
