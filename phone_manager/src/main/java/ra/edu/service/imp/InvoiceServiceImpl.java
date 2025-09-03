package ra.edu.service.imp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.model.entity.Invoice;
import ra.edu.model.entity.InvoiceDetail;
import ra.edu.model.entity.Product;
import ra.edu.repo.InvoiceRepository;
import ra.edu.repo.ProductRepository;
import ra.edu.service.InvoiceService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;



    @Override
    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        if (invoice.getInvoiceDetails() != null) {
            for (InvoiceDetail detail : invoice.getInvoiceDetails()) {
                Product product = productRepository.findById(detail.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

                if (product.getStock() < detail.getQuantity()) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ tồn kho!");
                }

                product.setStock(product.getStock() - detail.getQuantity());
                productRepository.save(product);

                detail.setProduct(product);
                detail.setInvoice(invoice);

                detail.setUnitPrice(product.getPrice());
            }

            invoice.calculateTotalAmount();
        }
        return invoiceRepository.save(invoice);
    }



    @Override
    public void updateStatus(Integer invoiceId, Invoice.Status newStatus) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        Invoice.Status currentStatus = invoice.getStatus();


        if (currentStatus == Invoice.Status.CONFIRMED && newStatus == Invoice.Status.PENDING) {
            throw new RuntimeException("Đơn hàng đã xác nhận, không thể quay lại trạng thái chờ duyệt.");
        }
        if (currentStatus == Invoice.Status.SHIPING &&
                (newStatus == Invoice.Status.CONFIRMED || newStatus == Invoice.Status.PENDING)) {
            throw new RuntimeException("Đơn hàng đang giao, không thể chuyển về trạng thái chờ duyệt hoặc đã xác nhận.");
        }
        if (currentStatus == Invoice.Status.COMPLETED &&
                (newStatus == Invoice.Status.CONFIRMED || newStatus == Invoice.Status.PENDING || newStatus == Invoice.Status.SHIPING)) {
            throw new RuntimeException("Đơn hàng đã hoàn tất, không thể thay đổi sang trạng thái khác.");
        }
        if (currentStatus == Invoice.Status.CANCELED) {
            throw new RuntimeException("Đơn hàng đã bị hủy, không thể cập nhật trạng thái.");
        }

        if (newStatus == Invoice.Status.CANCELED) {
            invoice.getInvoiceDetails().forEach(detail -> {
                Product product = detail.getProduct();
                product.setStock(product.getStock() + detail.getQuantity());
                productRepository.save(product);
            });
        }


        invoice.setStatus(newStatus);
        invoiceRepository.save(invoice);
    }




    @Override
    public Page<Invoice> getInvoices(String keyword, LocalDate date, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty() && date != null) {
            return invoiceRepository.searchByCustomerNameAndDate(keyword, date, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            return invoiceRepository.searchByCustomerName(keyword, pageable);
        } else if (date != null) {
            return invoiceRepository.searchByDate(date, pageable);
        }
        return invoiceRepository.findAll(pageable);
    }

    @Override
    public List<Object[]> revenueByDay() {
        return invoiceRepository.getRevenueByDay();
    }

    @Override
    public List<Object[]> revenueByMonth() {
        return invoiceRepository.getRevenueByMonth();
    }

    @Override
    public List<Object[]> revenueByYear() {
        return invoiceRepository.getRevenueByYear();
    }


}
