package ra.edu.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ra.edu.model.entity.Invoice;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    @Query("SELECT DATE(i.createdAt), SUM(i.totalAmount) " +
            "FROM Invoice i WHERE i.status = 'COMPLETED' GROUP BY DATE(i.createdAt)")
    List<Object[]> getRevenueByDay();

    @Query("SELECT MONTH(i.createdAt), SUM(i.totalAmount) " +
            "FROM Invoice i WHERE i.status = 'COMPLETED' GROUP BY MONTH(i.createdAt)")
    List<Object[]> getRevenueByMonth();

    @Query("SELECT YEAR(i.createdAt), SUM(i.totalAmount) " +
            "FROM Invoice i WHERE i.status = 'COMPLETED' GROUP BY YEAR(i.createdAt)")
    List<Object[]> getRevenueByYear();


    @Query("SELECT i FROM Invoice i WHERE LOWER(i.customer.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Invoice> searchByCustomerName(String keyword, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE DATE(i.createdAt) = :date")
    Page<Invoice> searchByDate(LocalDate date, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE LOWER(i.customer.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND DATE(i.createdAt) = :date")
    Page<Invoice> searchByCustomerNameAndDate(String keyword, LocalDate date, Pageable pageable);
}
