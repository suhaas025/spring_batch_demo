package com.example.Spring_batch_demo.batch.processor;

import com.example.Spring_batch_demo.dto.CustomerCSV;
import com.example.Spring_batch_demo.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Component
public class CustomerItemProcessor implements ItemProcessor<CustomerCSV, Customer> {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerItemProcessor.class);
    
    private final Validator validator;
    
    public CustomerItemProcessor(Validator validator) {
        this.validator = validator;
    }
    
    @Override
    public Customer process(CustomerCSV customerCSV) throws Exception {
        logger.debug("Processing customer: {}", customerCSV);
        
        try {
            // Transform CSV data to Customer entity
            Customer customer = new Customer();
            customer.setFirstName(transformName(customerCSV.getFirstName()));
            customer.setLastName(transformName(customerCSV.getLastName()));
            customer.setEmail(customerCSV.getEmail() != null ? customerCSV.getEmail().toLowerCase().trim() : null);
            customer.setAge(parseAge(customerCSV.getAge()));
            customer.setCity(transformName(customerCSV.getCity()));
            customer.setProcessedAt(LocalDateTime.now());
            customer.setStatus(Customer.CustomerStatus.PROCESSED);
            
            // Validate the customer object
            Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
            if (!violations.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (ConstraintViolation<Customer> violation : violations) {
                    sb.append(violation.getMessage()).append("; ");
                }
                logger.warn("Validation failed for customer {}: {}", customerCSV.getEmail(), sb.toString());
                customer.setStatus(Customer.CustomerStatus.FAILED);
                return customer; // Still return the customer but mark as failed
            }
            
            // Additional business logic
            if (customer.getAge() != null && customer.getAge() >= 65) {
                logger.info("Senior customer identified: {} {}", customer.getFirstName(), customer.getLastName());
            }
            
            logger.info("Successfully processed customer: {} {}", customer.getFirstName(), customer.getLastName());
            return customer;
            
        } catch (Exception e) {
            logger.error("Error processing customer: {}", customerCSV, e);
            // Create a failed customer record
            Customer failedCustomer = new Customer();
            failedCustomer.setEmail(customerCSV.getEmail());
            failedCustomer.setFirstName(customerCSV.getFirstName());
            failedCustomer.setLastName(customerCSV.getLastName());
            failedCustomer.setProcessedAt(LocalDateTime.now());
            failedCustomer.setStatus(Customer.CustomerStatus.FAILED);
            return failedCustomer;
        }
    }
    
    private String transformName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        // Capitalize first letter and make rest lowercase
        String trimmed = name.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
    
    private Integer parseAge(String ageStr) {
        if (ageStr == null || ageStr.trim().isEmpty()) {
            return null;
        }
        try {
            int age = Integer.parseInt(ageStr.trim());
            return age >= 0 && age <= 150 ? age : null; // Basic age validation
        } catch (NumberFormatException e) {
            logger.warn("Invalid age format: {}", ageStr);
            return null;
        }
    }
} 