package com.example.Spring_batch_demo.repository;

import com.example.Spring_batch_demo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByEmail(String email);
    
    List<Customer> findByStatus(Customer.CustomerStatus status);
    
    List<Customer> findByCity(String city);
    
    List<Customer> findByAgeBetween(Integer minAge, Integer maxAge);
    
    @Query("SELECT c FROM Customer c WHERE c.age > :age")
    List<Customer> findCustomersOlderThan(Integer age);
    
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.status = :status")
    Long countByStatus(Customer.CustomerStatus status);
    
    @Query("SELECT c.city, COUNT(c) FROM Customer c GROUP BY c.city")
    List<Object[]> countCustomersByCity();
} 