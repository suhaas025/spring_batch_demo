package com.example.Spring_batch_demo.batch.listener;

import com.example.Spring_batch_demo.model.Customer;
import com.example.Spring_batch_demo.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class JobCompletionListener implements JobExecutionListener {
    
    private static final Logger logger = LoggerFactory.getLogger(JobCompletionListener.class);
    
    private final CustomerRepository customerRepository;
    
    public JobCompletionListener(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info("Job Started: {} at {}", 
                jobExecution.getJobInstance().getJobName(), 
                jobExecution.getStartTime());
        
        logger.info("Job Parameters: {}", jobExecution.getJobParameters());
        
        // Log initial statistics
        long totalCustomers = customerRepository.count();
        logger.info("Total customers in database before job: {}", totalCustomers);
    }
    
    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();
        
        Duration duration = Duration.ZERO;
        if (startTime != null && endTime != null) {
            duration = Duration.between(startTime, endTime);
        }
        
        logger.info("Job Completed: {} at {}", 
                jobExecution.getJobInstance().getJobName(), 
                jobExecution.getEndTime());
        
        logger.info("Job Status: {}", jobExecution.getStatus());
        logger.info("Job Duration: {} seconds", duration.getSeconds());
        
        // Log job statistics
        logger.info("Job Execution Summary:");
        logger.info("- Read Count: {}", jobExecution.getStepExecutions().stream()
                .mapToLong(step -> step.getReadCount()).sum());
        logger.info("- Write Count: {}", jobExecution.getStepExecutions().stream()
                .mapToLong(step -> step.getWriteCount()).sum());
        logger.info("- Skip Count: {}", jobExecution.getStepExecutions().stream()
                .mapToLong(step -> step.getSkipCount()).sum());
        
        // Log database statistics
        long totalCustomers = customerRepository.count();
        long processedCustomers = customerRepository.countByStatus(Customer.CustomerStatus.PROCESSED);
        long failedCustomers = customerRepository.countByStatus(Customer.CustomerStatus.FAILED);
        
        logger.info("Database Statistics:");
        logger.info("- Total customers: {}", totalCustomers);
        logger.info("- Processed customers: {}", processedCustomers);
        logger.info("- Failed customers: {}", failedCustomers);
        
        // Log city distribution
        List<Object[]> cityStats = customerRepository.countCustomersByCity();
        logger.info("Customer distribution by city:");
        for (Object[] stat : cityStats) {
            logger.info("- {}: {} customers", stat[0], stat[1]);
        }
        
        // Log any failures
        if (!jobExecution.getAllFailureExceptions().isEmpty()) {
            logger.error("Job failed with {} exceptions:", jobExecution.getAllFailureExceptions().size());
            jobExecution.getAllFailureExceptions().forEach(exception -> 
                    logger.error("Exception: {}", exception.getMessage(), exception));
        }
    }
} 