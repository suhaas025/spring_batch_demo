package com.example.Spring_batch_demo.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MetricsConfiguration {

    private final MeterRegistry meterRegistry;

    public MetricsConfiguration(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Bean
    public JobExecutionListener metricsJobListener() {
        return new JobExecutionListener() {
            private Timer.Sample jobTimer;

            @Override
            public void beforeJob(JobExecution jobExecution) {
                jobTimer = Timer.start(meterRegistry);
                
                // Record job start
                meterRegistry.counter("batch.job.starts", 
                        "jobName", jobExecution.getJobInstance().getJobName()).increment();
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                // Record job duration
                jobTimer.stop(meterRegistry.timer("batch.job.duration",
                        "jobName", jobExecution.getJobInstance().getJobName(),
                        "status", jobExecution.getStatus().toString()));

                // Record job status
                meterRegistry.counter("batch.job.completions",
                        "jobName", jobExecution.getJobInstance().getJobName(),
                        "status", jobExecution.getStatus().toString()).increment();

                // Record processed items
                long totalRead = jobExecution.getStepExecutions().stream()
                        .mapToLong(StepExecution::getReadCount)
                        .sum();
                long totalWrite = jobExecution.getStepExecutions().stream()
                        .mapToLong(StepExecution::getWriteCount)
                        .sum();
                long totalSkip = jobExecution.getStepExecutions().stream()
                        .mapToLong(StepExecution::getSkipCount)
                        .sum();

                meterRegistry.gauge("batch.job.read.total",
                        totalRead);
                meterRegistry.gauge("batch.job.write.total",
                        totalWrite);
                meterRegistry.gauge("batch.job.skip.total",
                        totalSkip);
            }
        };
    }

    @Bean
    public StepExecutionListener metricsStepListener() {
        return new StepExecutionListener() {
            private Timer.Sample stepTimer;

            @Override
            public void beforeStep(StepExecution stepExecution) {
                stepTimer = Timer.start(meterRegistry);
                
                // Record step start
                meterRegistry.counter("batch.step.starts",
                        "stepName", stepExecution.getStepName()).increment();
            }

            @Override
            public org.springframework.batch.core.ExitStatus afterStep(StepExecution stepExecution) {
                // Record step duration
                stepTimer.stop(meterRegistry.timer("batch.step.duration",
                        "stepName", stepExecution.getStepName(),
                        "status", stepExecution.getStatus().toString()));

                // Record step metrics
                meterRegistry.gauge("batch.step.read.count",
                        stepExecution.getReadCount());
                meterRegistry.gauge("batch.step.write.count",
                        stepExecution.getWriteCount());
                meterRegistry.gauge("batch.step.commit.count",
                        stepExecution.getCommitCount());
                meterRegistry.gauge("batch.step.rollback.count",
                        stepExecution.getRollbackCount());

                // Record processing rate
                if (stepExecution.getWriteCount() > 0) {
                    double durationSeconds = stepExecution.getEndTime().getTime() - 
                            stepExecution.getStartTime().getTime() / 1000.0;
                    double rate = stepExecution.getWriteCount() / durationSeconds;
                    meterRegistry.gauge("batch.step.processing.rate",
                            rate);
                }

                return stepExecution.getExitStatus();
            }
        };
    }
} 