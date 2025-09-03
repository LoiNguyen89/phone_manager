package ra.edu.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ra.edu.model.entity.Customer;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    boolean existsByName(String name);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);

    Optional<Customer> findByName(String name);
    Optional<Customer> findByPhone(String phone);

}