package ra.edu.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ra.edu.model.entity.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    Page<Customer> getAll(Pageable pageable);


    Customer save(Customer customer);

    Optional<Customer> findById(Integer id);

    void deleteById(Integer id);

    boolean existsByName(String name);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    Optional<Customer> findByName(String name);

    Optional<Customer> findByPhone(String phone);
}
