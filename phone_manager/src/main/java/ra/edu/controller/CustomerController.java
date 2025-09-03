package ra.edu.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ra.edu.model.entity.Customer;
import ra.edu.service.CustomerService;
import ra.edu.service.imp.CloudinaryService;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Customer> customers = customerService.getAll(PageRequest.of(page, 5));
        model.addAttribute("customers", customers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customers.getTotalPages());
        return "customer/list";
    }


    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/add";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("customer") Customer customer,
                       BindingResult bindingResult,
                       @RequestParam("imageFile") MultipartFile file,
                       Model model) {
        // Validate file ảnh
        if (file == null || file.isEmpty()) {
            bindingResult.rejectValue("image", "NotNull", "Ảnh khách hàng không được để trống");
        }

        // Validate name unique
        if (customerService.existsByName(customer.getName())) {
            bindingResult.rejectValue("name", "Unique", "Tên khách hàng đã tồn tại");
        }

        // Validate phone unique
        if (customerService.existsByPhone(customer.getPhone())) {
            bindingResult.rejectValue("phone", "Unique", "Số điện thoại đã tồn tại");
        }


        if (customerService.existsByEmail(customer.getEmail())) {
            bindingResult.rejectValue("email", "Unique", "Email đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            return "customer/add";
        }

        if (!setImage(customer, file, model, true)) {
            return "customer/add";
        }

        customerService.save(customer);

        return "redirect:/customers";
    }




    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Customer customer = customerService.findById(id).orElseThrow();
        model.addAttribute("customer", customer);
        return "customer/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCustomer(@PathVariable Integer id,
                                 @Valid @ModelAttribute("customer") Customer customer,
                                 BindingResult bindingResult,
                                 @RequestParam("imageFile") MultipartFile file,
                                 @RequestParam(defaultValue = "0") int page,  // nhận page từ hidden input
                                 Model model) {
        customer.setId(id);

        if (customerService.existsByName(customer.getName())) {
            Customer existing = customerService.findByName(customer.getName()).get();
            if (!existing.getId().equals(id)) {
                bindingResult.rejectValue("name", "Unique", "Tên khách hàng đã tồn tại");
            }
        }

        if (customerService.existsByPhone(customer.getPhone())) {
            Customer existing = customerService.findByPhone(customer.getPhone()).get();
            if (!existing.getId().equals(id)) {
                bindingResult.rejectValue("phone", "Unique", "Số điện thoại đã tồn tại");
            }
        }

        if (bindingResult.hasErrors()) {
            return "customer/edit";
        }

        if (!setImage(customer, file, model, false)) {
            return "customer/edit";
        }

        customerService.save(customer);

        return "redirect:/customers?page=" + page;
    }


    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page) {
        customerService.deleteById(id);
        return "redirect:/customers?page=" + page;
    }

    private boolean setImage(Customer customer, MultipartFile file, Model model, boolean isNew) {
        try {
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(file);
                customer.setImage(imageUrl);
            } else if (!isNew) {
                Customer existing = customerService.findById(customer.getId()).orElse(null);
                if (existing != null) {
                    customer.setImage(existing.getImage());
                }
            }
            return true;
        } catch (IOException e) {
            model.addAttribute("error", "Có lỗi khi upload file: " + e.getMessage());
            return false;
        }
    }
}
