package ra.edu.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ra.edu.model.entity.Invoice;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceService {
    Invoice createInvoice(Invoice invoice);

    void updateStatus(Integer invoiceId, Invoice.Status status);

    Page<Invoice> getInvoices(String keyword, LocalDate date, Pageable pageable);

    List<Object[]> revenueByDay();
    List<Object[]> revenueByMonth();
    List<Object[]> revenueByYear();
}
