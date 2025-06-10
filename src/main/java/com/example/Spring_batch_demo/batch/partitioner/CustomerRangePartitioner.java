package com.example.Spring_batch_demo.batch.partitioner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class CustomerRangePartitioner implements Partitioner {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerRangePartitioner.class);
    
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        logger.info("Partitioning data for {} threads", gridSize);
        
        Map<String, ExecutionContext> partitions = new HashMap<>(gridSize);
        
        // Example: Partition by customer ID ranges
        int range = 1000; // Assume we have 1000 customers per partition
        int targetSize = range * gridSize; // Total range to process
        
        int number = 0;
        int start = 0;
        int end = range;
        
        while (start < targetSize) {
            ExecutionContext context = new ExecutionContext();
            context.putInt("partitionNumber", number);
            context.putInt("startRange", start);
            context.putInt("endRange", end);
            
            // Give each partition a unique name
            String partitionName = "partition" + number;
            partitions.put(partitionName, context);
            
            start += range;
            end += range;
            number++;
            
            logger.info("Created partition: {}, range: {}-{}", partitionName, start, end);
        }
        
        return partitions;
    }
} 