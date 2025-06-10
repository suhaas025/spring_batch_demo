package com.example.Spring_batch_demo.controller;

import com.example.Spring_batch_demo.model.Customer;
import com.example.Spring_batch_demo.repository.CustomerRepository;
import com.example.Spring_batch_demo.service.BatchJobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.StepExecution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/batch")
public class BatchJobController {
    
    private final BatchJobService batchJobService;
    private final CustomerRepository customerRepository;
    private final JobExplorer jobExplorer;
    
    public BatchJobController(BatchJobService batchJobService, 
                             CustomerRepository customerRepository,
                             JobExplorer jobExplorer) {
        this.batchJobService = batchJobService;
        this.customerRepository = customerRepository;
        this.jobExplorer = jobExplorer;
    }
    
    @PostMapping("/jobs/import-customers")
    public ResponseEntity<Map<String, Object>> runImportCustomersJob(@RequestParam(required = false) Map<String, String> parameters) {
        try {
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            
            JobExecution jobExecution = batchJobService.runImportCustomerJob(parameters);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jobExecutionId", jobExecution.getId());
            response.put("jobName", jobExecution.getJobInstance().getJobName());
            response.put("status", jobExecution.getStatus().toString());
            response.put("startTime", jobExecution.getStartTime());
            response.put("message", "Job started successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to start job");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/jobs/complex-processing")
    public ResponseEntity<Map<String, Object>> runComplexProcessingJob(@RequestParam(required = false) Map<String, String> parameters) {
        try {
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            
            JobExecution jobExecution = batchJobService.runComplexDataProcessingJob(parameters);
            
            Map<String, Object> response = new HashMap<>();
            response.put("jobExecutionId", jobExecution.getId());
            response.put("jobName", jobExecution.getJobInstance().getJobName());
            response.put("status", jobExecution.getStatus().toString());
            response.put("startTime", jobExecution.getStartTime());
            response.put("message", "Job started successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to start job");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/jobs/{jobExecutionId}/details")
    public ResponseEntity<Map<String, Object>> getJobExecutionDetails(@PathVariable Long jobExecutionId) {
        try {
            JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);
            
            if (jobExecution == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Job execution not found");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("jobExecutionId", jobExecution.getId());
            response.put("jobName", jobExecution.getJobInstance().getJobName());
            response.put("status", jobExecution.getStatus().toString());
            response.put("startTime", jobExecution.getStartTime());
            response.put("endTime", jobExecution.getEndTime());
            response.put("exitCode", jobExecution.getExitStatus().getExitCode());
            response.put("exitDescription", jobExecution.getExitStatus().getExitDescription());
            
            // Get failure exceptions
            List<String> failureExceptions = jobExecution.getAllFailureExceptions().stream()
                    .map(Throwable::getMessage)
                    .collect(Collectors.toList());
            response.put("failureExceptions", failureExceptions);
            
            // Get step execution details
            List<Map<String, Object>> stepExecutions = jobExecution.getStepExecutions().stream()
                    .map(stepExecution -> {
                        Map<String, Object> stepInfo = new HashMap<>();
                        stepInfo.put("stepName", stepExecution.getStepName());
                        stepInfo.put("status", stepExecution.getStatus().toString());
                        stepInfo.put("readCount", stepExecution.getReadCount());
                        stepInfo.put("writeCount", stepExecution.getWriteCount());
                        stepInfo.put("skipCount", stepExecution.getSkipCount());
                        stepInfo.put("commitCount", stepExecution.getCommitCount());
                        stepInfo.put("rollbackCount", stepExecution.getRollbackCount());
                        stepInfo.put("exitCode", stepExecution.getExitStatus().getExitCode());
                        stepInfo.put("exitDescription", stepExecution.getExitStatus().getExitDescription());
                        
                        // Get step failure exceptions
                        List<String> stepFailures = stepExecution.getFailureExceptions().stream()
                                .map(Throwable::getMessage)
                                .collect(Collectors.toList());
                        stepInfo.put("failureExceptions", stepFailures);
                        
                        return stepInfo;
                    })
                    .collect(Collectors.toList());
            response.put("stepExecutions", stepExecutions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get job execution details");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return ResponseEntity.ok(customers);
    }
    
    @GetMapping("/customers/stats")
    public ResponseEntity<Map<String, Object>> getCustomerStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalCustomers = customerRepository.count();
        long processedCustomers = customerRepository.countByStatus(Customer.CustomerStatus.PROCESSED);
        long failedCustomers = customerRepository.countByStatus(Customer.CustomerStatus.FAILED);
        long newCustomers = customerRepository.countByStatus(Customer.CustomerStatus.NEW);
        
        stats.put("totalCustomers", totalCustomers);
        stats.put("processedCustomers", processedCustomers);
        stats.put("failedCustomers", failedCustomers);
        stats.put("newCustomers", newCustomers);
        
        return ResponseEntity.ok(stats);
    }
    
    @DeleteMapping("/customers")
    public ResponseEntity<Map<String, Object>> deleteAllCustomers() {
        long count = customerRepository.count();
        customerRepository.deleteAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All customers deleted");
        response.put("deletedCount", count);
        
        return ResponseEntity.ok(response);
    }
} 