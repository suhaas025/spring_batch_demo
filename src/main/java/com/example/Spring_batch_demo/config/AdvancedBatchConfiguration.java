package com.example.Spring_batch_demo.config;

import com.example.Spring_batch_demo.batch.listener.JobCompletionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class AdvancedBatchConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedBatchConfiguration.class);
    
    @Bean
    public JobExecutionDecider validationDecider() {
        return (jobExecution, stepExecution) -> {
            if (stepExecution != null) {
                long failedCount = stepExecution.getWriteCount() - stepExecution.getFilterCount();
                return failedCount > 0 ? 
                       new FlowExecutionStatus("REQUIRES_VALIDATION") : 
                       new FlowExecutionStatus("VALIDATION_SKIPPED");
            }
            return new FlowExecutionStatus("VALIDATION_SKIPPED");
        };
    }
    
    @Bean
    public Flow parallelValidationFlow(
            @Qualifier("dataValidationStep") Step dataValidationStep,
            @Qualifier("dataEnrichmentStep") Step dataEnrichmentStep,
            TaskExecutor taskExecutor) {
        
        Flow validationFlow = new FlowBuilder<SimpleFlow>("validationFlow")
                .start(dataValidationStep)
                .build();
        
        Flow enrichmentFlow = new FlowBuilder<SimpleFlow>("enrichmentFlow")
                .start(dataEnrichmentStep)
                .build();
        
        return new FlowBuilder<SimpleFlow>("parallelValidationFlow")
                .split(taskExecutor)
                .add(validationFlow, enrichmentFlow)
                .build();
    }
    
    @Bean
    public Step dataValidationStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder("dataValidationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    logger.info("Performing data validation...");
                    // Validate customer data (age, email format, etc.)
                    Thread.sleep(1000); // Simulate work
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    @Bean
    public Step dataEnrichmentStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder("dataEnrichmentStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    logger.info("Enriching customer data...");
                    // Add additional customer information (demographics, preferences)
                    Thread.sleep(1000); // Simulate work
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    @Bean
    public Step errorHandlingStep(JobRepository jobRepository,
                                 PlatformTransactionManager transactionManager) {
        return new StepBuilder("errorHandlingStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    logger.info("Processing failed records...");
                    // Handle failed records (retry, notify, archive)
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
    
    @Bean
    public Job advancedCustomerProcessingJob(
            JobRepository jobRepository,
            @Qualifier("csvToDbStep") Step csvToDbStep,
            @Qualifier("errorHandlingStep") Step errorHandlingStep,
            @Qualifier("parallelValidationFlow") Flow parallelValidationFlow,
            JobExecutionDecider validationDecider,
            JobCompletionListener listener) {
        
        return new JobBuilder("advancedCustomerProcessingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(csvToDbStep)
                .next(validationDecider)
                .on("REQUIRES_VALIDATION").to(parallelValidationFlow)
                .next(errorHandlingStep)
                .from(validationDecider)
                .on("VALIDATION_SKIPPED").to(errorHandlingStep)
                .end()
                .build();
    }
} 