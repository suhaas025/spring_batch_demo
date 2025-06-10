package com.example.Spring_batch_demo.config;

import org.springframework.batch.core.JobRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ThreadPoolTaskExecutor;
import org.springframework.retry.policy.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.time.LocalDateTime;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    
    @Value("${batch.chunk.size:10}")
    private int chunkSize;
    
    @Value("${batch.max.threads:4}")
    private int maxThreads;
    
    @Value("${batch.retry.limit:3}")
    private int retryLimit;
    
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(maxThreads);
        executor.setMaxPoolSize(maxThreads);
        executor.setQueueCapacity(chunkSize * 2);
        executor.setThreadNamePrefix("BatchThread-");
        return executor;
    }
    
    @Bean
    public RetryPolicy retryPolicy() {
        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(retryLimit);
        return policy;
    }
    
    @Bean
    public FlatFileItemReader<CustomerCSV> customerCsvReader() {
        FlatFileItemReader<CustomerCSV> reader = new FlatFileItemReader<>();
        reader.setLineMapper(lineMapper());
        return reader;
    }
    
    @Bean
    public LineMapper<CustomerCSV> lineMapper() {
        DefaultLineMapper<CustomerCSV> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("id", "name", "email");
        tokenizer.setIncludedFields(1, 2, 3);
        BeanWrapperFieldSetMapper<CustomerCSV> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CustomerCSV.class);
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }
    
    @Bean
    public Step csvToDbStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           FlatFileItemReader<CustomerCSV> reader,
                           CustomerItemProcessor processor,
                           CustomerItemWriter writer,
                           TaskExecutor taskExecutor,
                           RetryPolicy retryPolicy) {
        return new StepBuilder("csvToDbStep", jobRepository)
                .<CustomerCSV, Customer>chunk(chunkSize, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor)
                .throttleLimit(maxThreads)
                .faultTolerant()
                .retry(Exception.class)
                .retryPolicy(retryPolicy)
                .skip(ValidationException.class)
                .skipLimit(10)
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        stepExecution.getExecutionContext().putString("startTime", LocalDateTime.now().toString());
                    }
                    
                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        String startTime = stepExecution.getExecutionContext().getString("startTime");
                        Duration duration = Duration.between(
                            LocalDateTime.parse(startTime),
                            LocalDateTime.now()
                        );
                        stepExecution.getExecutionContext().putString(
                            "processingTime",
                            duration.toString()
                        );
                        return stepExecution.getExitStatus();
                    }
                })
                .build();
    }
} 