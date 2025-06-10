package com.example.Spring_batch_demo.batch.reader;

import com.example.Spring_batch_demo.dto.CustomerCSV;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.util.Iterator;

@Component
public class CustomerCsvItemReader implements ItemReader<CustomerCSV> {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerCsvItemReader.class);
    
    private Iterator<CustomerCSV> customerIterator;
    private boolean initialized = false;
    
    public void setResource(Resource resource) {
        try {
            logger.info("Initializing CSV reader for resource: {}", resource.getFilename());
            
            CsvToBean<CustomerCSV> csvToBean = new CsvToBeanBuilder<CustomerCSV>(new FileReader(resource.getFile()))
                    .withType(CustomerCSV.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();
            
            customerIterator = csvToBean.iterator();
            initialized = true;
            
            logger.info("CSV reader initialized successfully");
            
        } catch (Exception e) {
            logger.error("Error initializing CSV reader", e);
            throw new RuntimeException("Failed to initialize CSV reader", e);
        }
    }
    
    @Override
    public CustomerCSV read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            throw new IllegalStateException("Reader not initialized. Call setResource() first.");
        }
        
        if (customerIterator != null && customerIterator.hasNext()) {
            CustomerCSV customer = customerIterator.next();
            logger.debug("Read customer: {}", customer);
            return customer;
        }
        
        logger.info("Finished reading all customers from CSV");
        return null; // End of input
    }
} 