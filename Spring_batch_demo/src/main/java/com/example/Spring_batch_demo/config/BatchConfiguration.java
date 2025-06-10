package com.example.Spring_batch_demo.config;

import com.example.Spring_batch_demo.batch.listener.JobCompletionListener;
import com.example.Spring_batch_demo.batch.processor.CustomerItemProcessor;
import com.example.Spring_batch_demo.batch.writer.CustomerItemWriter;
import com.example.Spring_batch_demo.dto.CustomerCSV;
import com.example.Spring_batch_demo.model.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {
    
    @Value("${batch.chunk.size:10}")
    private int chunkSize;
    
    @Bean
    public FlatFileItemReader<CustomerCSV> customerCsvReader() {
        FlatFileItemReader<CustomerCSV> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("data/customers.csv"));
        reader.setLinesToSkip(1); // Skip header line
        
        DefaultLineMapper<CustomerCSV> lineMapper = new DefaultLineMapper<>();
        
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("first_name", "last_name", "email", "age", "city");
        tokenizer.setDelimiter(",");
        
        BeanWrapperFieldSetMapper<CustomerCSV> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CustomerCSV.class);
        
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        
        reader.setLineMapper(lineMapper);
        return reader;
    }
    
    @Bean
    public Step csvToDbStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           FlatFileItemReader<CustomerCSV> reader,
                           CustomerItemProcessor processor,
                           CustomerItemWriter writer) {
        return new StepBuilder("csvToDbStep", jobRepository)
                .<CustomerCSV, Customer>chunk(chunkSize, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .allowStartIfComplete(true)
                .build();
    }
    
    @Bean
    public Job importCustomerJob(JobRepository jobRepository,
                                Step csvToDbStep,
                                JobCompletionListener listener) {
        return new JobBuilder("importCustomerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(csvToDbStep)
                .build();
    }
    
    // Advanced batch job with multiple steps
    @Bean
    public Step dataValidationStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder("dataValidationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // Custom validation logic
                    System.out.println("Performing data validation...");
                    // Add your validation logic here
                    return null;
                }, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
    
    @Bean
    public Step dataCleanupStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
        return new StepBuilder("dataCleanupStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // Custom cleanup logic
                    System.out.println("Performing data cleanup...");
                    // Add your cleanup logic here
                    return null;
                }, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
    
    @Bean
    public Job complexDataProcessingJob(JobRepository jobRepository,
                                       Step csvToDbStep,
                                       Step dataValidationStep,
                                       Step dataCleanupStep,
                                       JobCompletionListener listener) {
        return new JobBuilder("complexDataProcessingJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(csvToDbStep)
                .next(dataValidationStep)
                .next(dataCleanupStep)
                .build();
    }
} 