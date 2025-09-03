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

    private Pageable buildPageable(int page, int size, String sortDir, String sortField) {
        if ("asc".equalsIgnoreCase(sortDir)) {
            return PageRequest.of(page, size, Sort.by(sortField).ascending());
        } else if ("desc".equalsIgnoreCase(sortDir)) {
            return PageRequest.of(page, size, Sort.by(sortField).descending());
        } else {
            return PageRequest.of(page, size, Sort.by("id").ascending());
        }
    }

//    @GetMapping
//    public String listProducts(@RequestParam(defaultValue = "0") int page,
//                               @RequestParam(defaultValue = "5") int size,
//                               @RequestParam(required = false) String sortDir,
//                               Model model) {
//
//        Pageable pageable = buildPageable(page, size, sortDir, "price");
//
//        Page<Product> products = productService.findAll(pageable);
//        model.addAttribute("products", products);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", products.getTotalPages());
//        model.addAttribute("sortDir", sortDir == null ? "none" : sortDir);
//        return "product/list";
//    }


    @GetMapping
    public String listProducts(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(required = false) String sortDir,
                               Model model) {

        Pageable pageable = buildPageable(page, size, sortDir, "price");

        Page<Product> products = productService.findAll(pageable);
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("sortDir", sortDir == null ? "none" : sortDir);
        model.addAttribute("searchType", null); // không có filter
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
                                @Valid @ModelAttribute("product") Product product,
                                BindingResult bindingResult,
                                @RequestParam("imageFile") MultipartFile file,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(required = false, defaultValue = "asc") String sortDir,
                                @RequestParam(required = false) String searchType,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Integer stock,
                                @RequestParam(required = false) BigDecimal min,
                                @RequestParam(required = false) BigDecimal max,
                                Model model) {

        product.setId(id);

        if (bindingResult.hasErrors() || !setImage(product, file, model, false)) {
            return "product/edit";
        }

        productService.update(product);

        String redirectUrl;

        if ("brand".equals(searchType) && keyword != null && !keyword.isEmpty()) {
            redirectUrl = "/products/search/brand?brand=" + keyword
                    + "&page=" + page
                    + "&sortDir=" + sortDir;
        } else if ("stock".equals(searchType) && stock != null) {
            redirectUrl = "/products/search/stock?stock=" + stock
                    + "&page=" + page
                    + "&sortDir=" + sortDir;
        } else if ("price".equals(searchType) && min != null && max != null) {
            redirectUrl = "/products/search/price?min=" + min
                    + "&max=" + max
                    + "&page=" + page
                    + "&sortDir=" + sortDir;
        } else {
            redirectUrl = "/products?page=" + page + "&sortDir=" + sortDir;
        }

        return "redirect:" + redirectUrl;
    }





    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String sortDir,
                         @RequestParam(required = false) String searchType) {
        productService.delete(id);
        String redirectUrl = "/products?page=" + page + "&sortDir=" + (sortDir != null ? sortDir : "none");
        if (searchType != null) redirectUrl += "&searchType=" + searchType;
        return "redirect:" + redirectUrl;
    }


    @GetMapping("/search/brand")
    public String searchByBrand(@RequestParam String brand,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size,
                                @RequestParam(required = false) String sortDir,
                                Model model) {

        Pageable pageable = buildPageable(page, size, sortDir, "price");

        Page<Product> products = productService.searchByBrand(brand, pageable);
        model.addAttribute("products", products);
        model.addAttribute("searchType", "brand");
        model.addAttribute("keyword", brand);
        model.addAttribute("sortDir", sortDir == null ? "none" : sortDir);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        return "product/list";
    }

    @GetMapping("/search/stock")
    public String searchByStock(@RequestParam(required = false) Integer stock,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size,
                                @RequestParam(required = false) String sortDir,
                                Model model) {

        Pageable pageable = buildPageable(page, size, sortDir, "price");

        Page<Product> products;
        if (stock == null) {
            products = productService.findAll(pageable);
        } else if (stock < 0) {
            model.addAttribute("error", "Hàng tồn không được nhỏ hơn 0");
            model.addAttribute("products", Page.empty());
            return "product/list";

        } else {
            products = productService.searchByStock(stock, pageable);
            model.addAttribute("stock", stock);
            model.addAttribute("searchType", "stock");
        }

        model.addAttribute("products", products);
        model.addAttribute("sortDir", sortDir == null ? "none" : sortDir);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());

        return "product/list";
    }

    @GetMapping("/search/price")
    public String searchByPrice(@RequestParam(required = false) BigDecimal min,
                                @RequestParam(required = false) BigDecimal max,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size,
                                @RequestParam(required = false) String sortDir,
                                Model model) {
        if ((min != null && min.compareTo(BigDecimal.ZERO) < 0) ||
                (max != null && max.compareTo(BigDecimal.ZERO) < 0)) {
            model.addAttribute("error", "Giá không được nhỏ hơn 0");
            model.addAttribute("products", Page.empty());
            return "product/list";
        }

        if (min == null) {
            min = BigDecimal.ZERO;
        }
        if (max == null) {
            max = BigDecimal.valueOf(Double.MAX_VALUE);
        }

        Pageable pageable = buildPageable(page, size, sortDir, "price");

        Page<Product> products = productService.searchByPriceRange(min, max, pageable);

        model.addAttribute("products", products);
        model.addAttribute("searchType", "price");
        model.addAttribute("min", min);
        model.addAttribute("max", (max.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) == 0) ? "" : max);
        model.addAttribute("sortDir", sortDir == null ? "none" : sortDir);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());

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
