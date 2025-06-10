package com.example.Spring_batch_demo.batch.writer;

import com.example.Spring_batch_demo.model.Customer;
import com.example.Spring_batch_demo.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerItemWriter implements ItemWriter<Customer> {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerItemWriter.class);
    
    private final CustomerRepository customerRepository;
    
    public CustomerItemWriter(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    @Override
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        List<? extends Customer> customers = chunk.getItems();
        
        logger.info("Writing {} customers to database", customers.size());
        
        try {
            // Convert to List<Customer> for saving
            List<Customer> customerList = customers.stream()
                    .map(Customer.class::cast)
                    .collect(Collectors.toList());
            
            // Save all customers
            List<Customer> savedCustomers = customerRepository.saveAll(customerList);
            
            // Log statistics
            long successfulCount = savedCustomers.stream()
                    .filter(c -> c.getStatus() == Customer.CustomerStatus.PROCESSED)
                    .count();
            long failedCount = savedCustomers.stream()
                    .filter(c -> c.getStatus() == Customer.CustomerStatus.FAILED)
                    .count();
            
            logger.info("Successfully saved {} customers - Success: {}, Failed: {}", 
                    savedCustomers.size(), successfulCount, failedCount);
            
            // Log failed customers for debugging
            List<Customer> failedCustomers = savedCustomers.stream()
                    .filter(c -> c.getStatus() == Customer.CustomerStatus.FAILED)
                    .collect(Collectors.toList());
            
            if (!failedCustomers.isEmpty()) {
                logger.warn("Failed customers: {}", 
                        failedCustomers.stream()
                                .map(c -> c.getEmail())
                                .collect(Collectors.joining(", ")));
            }
            
        } catch (Exception e) {
            logger.error("Error writing customers to database", e);
            throw e;
        }
    }
} 