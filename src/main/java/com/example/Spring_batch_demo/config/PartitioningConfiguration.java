package com.example.Spring_batch_demo.config;

import com.example.Spring_batch_demo.batch.partitioner.CustomerRangePartitioner;
import com.example.Spring_batch_demo.batch.processor.CustomerItemProcessor;
import com.example.Spring_batch_demo.batch.writer.CustomerItemWriter;
import com.example.Spring_batch_demo.dto.CustomerCSV;
import com.example.Spring_batch_demo.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class PartitioningConfiguration {

    @Value("${batch.grid.size:4}")
    private int gridSize;

    @Bean
    public Partitioner customerRangePartitioner() {
        return new CustomerRangePartitioner();
    }

    @Bean
    public Step partitionStep(JobRepository jobRepository,
                            Partitioner partitioner,
                            Step customerProcessingStep,
                            TaskExecutor taskExecutor) {
        return new StepBuilder("partitionStep", jobRepository)
                .partitioner("customerProcessing", partitioner)
                .step(customerProcessingStep)
                .gridSize(gridSize)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step customerProcessingStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     FlatFileItemReader<CustomerCSV> reader,
                                     CustomerItemProcessor processor,
                                     CustomerItemWriter writer) {
        return new StepBuilder("customerProcessingStep", jobRepository)
                .<CustomerCSV, Customer>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job partitionedJob(JobRepository jobRepository,
                             Step partitionStep) {
        return new JobBuilder("partitionedCustomerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(partitionStep)
                .build();
    }
} 