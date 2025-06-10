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

    @GetMapping("/jobs/executions")
    public ResponseEntity<Map<String, Object>> getJobExecutions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<JobExecution> jobExecutions = jobExplorer.findJobExecutions(null)
                    .stream()
                    .skip(page * size)
                    .limit(size)
                    .collect(Collectors.toList());

            long totalElements = jobExplorer.findJobExecutions(null).size();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            List<Map<String, Object>> content = jobExecutions.stream()
                    .map(jobExecution -> {
                        Map<String, Object> execution = new HashMap<>();
                        execution.put("jobExecutionId", jobExecution.getId());
                        execution.put("jobName", jobExecution.getJobInstance().getJobName());
                        execution.put("status", jobExecution.getStatus().toString());
                        execution.put("startTime", jobExecution.getStartTime());
                        execution.put("endTime", jobExecution.getEndTime());
                        execution.put("exitCode", jobExecution.getExitStatus().getExitCode());
                        execution.put("exitDescription", jobExecution.getExitStatus().getExitDescription());
                        return execution;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", content);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get job executions");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
} 