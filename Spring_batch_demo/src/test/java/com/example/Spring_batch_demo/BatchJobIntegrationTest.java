package com.example.Spring_batch_demo;

import com.example.Spring_batch_demo.model.Customer;
import com.example.Spring_batch_demo.repository.CustomerRepository;
import com.example.Spring_batch_demo.service.BatchJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@SpringBatchTest
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false",
    "logging.level.com.example.Spring_batch_demo=INFO"
})
class BatchJobIntegrationTest {
    
    @Autowired
    private BatchJobService batchJobService;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }
    
    @Test
    void testImportCustomerJob() throws Exception {
        // Given
        long initialCount = customerRepository.count();
        assertEquals(0, initialCount);
        
        // When
        JobExecution jobExecution = batchJobService.runImportCustomerJob();
        
        // Wait for job to complete (in a real test, you might use JobTestUtils)
        Thread.sleep(2000);
        
        // Then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        
        long finalCount = customerRepository.count();
        assertTrue(finalCount > 0, "Should have imported some customers");
        
        // Verify some customers were processed successfully
        long processedCount = customerRepository.countByStatus(Customer.CustomerStatus.PROCESSED);
        assertTrue(processedCount > 0, "Should have some processed customers");
        
        // Verify some customers failed (due to intentionally bad data)
        long failedCount = customerRepository.countByStatus(Customer.CustomerStatus.FAILED);
        assertTrue(failedCount > 0, "Should have some failed customers due to validation issues");
        
        System.out.println("Total customers imported: " + finalCount);
        System.out.println("Processed customers: " + processedCount);
        System.out.println("Failed customers: " + failedCount);
    }
    
    @Test
    void testComplexDataProcessingJob() throws Exception {
        // Given
        long initialCount = customerRepository.count();
        assertEquals(0, initialCount);
        
        // When
        JobExecution jobExecution = batchJobService.runComplexDataProcessingJob();
        
        // Wait for job to complete
        Thread.sleep(3000);
        
        // Then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        
        long finalCount = customerRepository.count();
        assertTrue(finalCount > 0, "Should have imported some customers");
        
        // Verify the job had multiple steps
        assertTrue(jobExecution.getStepExecutions().size() >= 3, 
                "Complex job should have at least 3 steps");
        
        System.out.println("Complex job completed with " + 
                jobExecution.getStepExecutions().size() + " steps");
    }
} 