package com.example.Spring_batch_demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class BatchJobService {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchJobService.class);
    
    private final JobLauncher jobLauncher;
    private final Job importCustomerJob;
    private final Job complexDataProcessingJob;
    
    public BatchJobService(JobLauncher jobLauncher,
                          @Qualifier("importCustomerJob") Job importCustomerJob,
                          @Qualifier("complexDataProcessingJob") Job complexDataProcessingJob) {
        this.jobLauncher = jobLauncher;
        this.importCustomerJob = importCustomerJob;
        this.complexDataProcessingJob = complexDataProcessingJob;
    }
    
    public JobExecution runImportCustomerJob() {
        return runImportCustomerJob(new HashMap<>());
    }
    
    public JobExecution runImportCustomerJob(Map<String, String> parameters) {
        logger.info("Starting importCustomerJob with parameters: {}", parameters);
        
        try {
            JobParameters jobParameters = createJobParameters(parameters);
            JobExecution jobExecution = jobLauncher.run(importCustomerJob, jobParameters);
            
            logger.info("ImportCustomerJob started with execution id: {}", jobExecution.getId());
            return jobExecution;
            
        } catch (JobExecutionAlreadyRunningException | JobRestartException | 
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            logger.error("Error starting importCustomerJob", e);
            throw new RuntimeException("Failed to start job", e);
        }
    }
    
    public JobExecution runComplexDataProcessingJob() {
        return runComplexDataProcessingJob(new HashMap<>());
    }
    
    public JobExecution runComplexDataProcessingJob(Map<String, String> parameters) {
        logger.info("Starting complexDataProcessingJob with parameters: {}", parameters);
        
        try {
            JobParameters jobParameters = createJobParameters(parameters);
            JobExecution jobExecution = jobLauncher.run(complexDataProcessingJob, jobParameters);
            
            logger.info("ComplexDataProcessingJob started with execution id: {}", jobExecution.getId());
            return jobExecution;
            
        } catch (JobExecutionAlreadyRunningException | JobRestartException | 
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            logger.error("Error starting complexDataProcessingJob", e);
            throw new RuntimeException("Failed to start job", e);
        }
    }
    
    private JobParameters createJobParameters(Map<String, String> parameters) {
        JobParametersBuilder builder = new JobParametersBuilder();
        
        // Add timestamp to make each execution unique
        builder.addString("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")));
        
        // Add custom parameters
        parameters.forEach(builder::addString);
        
        return builder.toJobParameters();
    }
    
    public String getJobStatus(Long jobExecutionId) {
        // In a real application, you would query the job repository
        // For now, return a placeholder
        return "Job status checking not implemented yet";
    }
} 