package ra.edu.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ra.edu.dto.InvoiceForm;
import ra.edu.model.entity.Customer;
import ra.edu.model.entity.Invoice;
import ra.edu.model.entity.InvoiceDetail;
import ra.edu.model.entity.Product;
import ra.edu.repo.ProductRepository;
import ra.edu.service.CustomerService;
import ra.edu.service.InvoiceService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final CustomerService customerService;
    private final ProductRepository productRepository;

    // ==================== LIST ====================
    @GetMapping
    public String listInvoices(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String date,
                               Model model) {
        LocalDate searchDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : null;
        Page<Invoice> invoices = invoiceService.getInvoices(keyword, searchDate, PageRequest.of(page, 10));
        model.addAttribute("invoices", invoices);
        model.addAttribute("keyword", keyword);
        model.addAttribute("date", date);
        return "invoice/list";
    }

    // ==================== ADD FORM ====================
    @GetMapping("/add")
    public String addInvoice(Model model) {
        model.addAttribute("invoiceForm", new InvoiceForm());
        model.addAttribute("customers", customerService.getAll(PageRequest.of(0, 100)).getContent());
        model.addAttribute("products", productRepository.findAll());
        return "invoice/add";
    }

    // ==================== SAVE ====================
    @PostMapping("/save")
    public String saveInvoice(@ModelAttribute InvoiceForm invoiceForm,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            // Tìm khách hàng
            Customer customer = customerService.findById(invoiceForm.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

            // Kiểm tra sản phẩm đã chọn
            if (invoiceForm.getSelectedProductIds() == null || invoiceForm.getSelectedProductIds().isEmpty()) {
                throw new RuntimeException("Vui lòng chọn ít nhất 1 sản phẩm");
            }

            Invoice invoice = new Invoice();
            invoice.setCustomer(customer);
            List<InvoiceDetail> details = new ArrayList<>();
            Map<Integer, Integer> quantities = invoiceForm.getQuantities();

            for (Integer productId : invoiceForm.getSelectedProductIds()) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

                int qty = 1;
                if (quantities != null && quantities.get(productId) != null) {
                    qty = quantities.get(productId);
                }

                if (product.getStock() < qty) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ tồn kho!");
                }

                InvoiceDetail detail = new InvoiceDetail();
                detail.setProduct(product);
                detail.setQuantity(qty);
                detail.setUnitPrice(product.getPrice());
                detail.setInvoice(invoice);

                details.add(detail);
            }

            invoice.setInvoiceDetails(details);
            invoiceService.createInvoice(invoice);

            redirectAttributes.addFlashAttribute("successMessage", "Tạo hóa đơn thành công!");
            return "redirect:/invoices";

        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("invoiceForm", invoiceForm);
            model.addAttribute("customers", customerService.getAll(PageRequest.of(0, 100)).getContent());
            model.addAttribute("products", productRepository.findAll());
            return "invoice/add";
        }
    }

    // ==================== UPDATE STATUS ====================
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Integer id,
                               @RequestParam Invoice.Status status,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String date,
                               RedirectAttributes redirectAttributes) {
        try {
            invoiceService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        String redirectUrl = "/invoices?page=" + page;
        if (keyword != null && !keyword.isEmpty()) redirectUrl += "&keyword=" + keyword;
        if (date != null && !date.isEmpty()) redirectUrl += "&date=" + date;

        return "redirect:" + redirectUrl;
    }

    // ==================== REVENUE ====================
    @GetMapping("/revenue")
    public String revenue(Model model) {
        model.addAttribute("dayRevenue", invoiceService.revenueByDay());
        model.addAttribute("monthRevenue", invoiceService.revenueByMonth());
        model.addAttribute("yearRevenue", invoiceService.revenueByYear());
        return "invoice/revenue";
    }
}
