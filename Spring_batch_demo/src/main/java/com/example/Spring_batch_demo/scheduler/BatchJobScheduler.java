package com.example.Spring_batch_demo.scheduler;

import com.example.Spring_batch_demo.service.BatchJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BatchJobScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchJobScheduler.class);
    
    private final BatchJobService batchJobService;
    
    public BatchJobScheduler(BatchJobService batchJobService) {
        this.batchJobService = batchJobService;
    }
    
    // Runs every 5 minutes (commented out to avoid overwhelming during demo)
    // @Scheduled(fixedRate = 300000) // 5 minutes
    public void runScheduledImportJob() {
        logger.info("Running scheduled import customer job");
        
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("scheduledRun", "true");
            parameters.put("trigger", "scheduled");
            
            JobExecution jobExecution = batchJobService.runImportCustomerJob(parameters);
            logger.info("Scheduled job started with execution id: {}", jobExecution.getId());
            
        } catch (Exception e) {
            logger.error("Error running scheduled import job", e);
        }
    }
    
    // Runs daily at 2 AM (commented out for demo)
    // @Scheduled(cron = "0 0 2 * * ?")
    public void runDailyComplexProcessingJob() {
        logger.info("Running daily complex processing job");
        
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("dailyRun", "true");
            parameters.put("trigger", "daily-schedule");
            
            JobExecution jobExecution = batchJobService.runComplexDataProcessingJob(parameters);
            logger.info("Daily scheduled job started with execution id: {}", jobExecution.getId());
            
        } catch (Exception e) {
            logger.error("Error running daily scheduled job", e);
        }
    }
    
    // Demo method that can be enabled for testing
    @Scheduled(fixedDelay = 60000, initialDelay = 30000) // Run every minute after 30 seconds initial delay
    public void demoScheduledJob() {
        logger.info("Demo scheduled job triggered - this is just a log message to show scheduling works");
        // Uncomment the following line to actually run a job every minute (not recommended for demo)
        // runScheduledImportJob();
    }
} 