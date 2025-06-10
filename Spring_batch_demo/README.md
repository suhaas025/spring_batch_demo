# Spring Batch Demo Project

This project demonstrates the comprehensive features of **Spring Batch** framework for processing large volumes of data efficiently and reliably.

## 🚀 Features Demonstrated

### Core Spring Batch Features
- **Chunk-based Processing**: Read, process, and write data in configurable chunks
- **Job Configuration**: Multiple job definitions with different processing patterns
- **Step Management**: Single-step and multi-step job workflows
- **ItemReader**: CSV file reading with custom field mapping
- **ItemProcessor**: Data transformation and validation logic
- **ItemWriter**: Database persistence with custom business logic
- **Job Parameters**: Runtime configuration and parameterization
- **Job Listeners**: Monitoring and logging job execution lifecycle

### Advanced Features
- **Error Handling**: Validation, skip policies, and failure recovery
- **Job Scheduling**: Automatic job execution using Spring's `@Scheduled`
- **REST API Integration**: HTTP endpoints for job management
- **Database Integration**: JPA entities with H2 in-memory database
- **Monitoring**: Spring Boot Actuator endpoints for job metrics
- **Custom Components**: Custom readers, processors, and writers

## 📁 Project Structure

```
src/main/java/com/example/Spring_batch_demo/
├── SpringBatchDemoApplication.java     # Main application class
├── config/
│   └── BatchConfiguration.java        # Batch job and step configurations
├── model/
│   └── Customer.java                   # JPA entity for customer data
├── dto/
│   └── CustomerCSV.java               # DTO for CSV input
├── repository/
│   └── CustomerRepository.java        # JPA repository for database operations
├── batch/
│   ├── processor/
│   │   └── CustomerItemProcessor.java # Custom item processor
│   ├── writer/
│   │   └── CustomerItemWriter.java    # Custom item writer
│   ├── reader/
│   │   └── CustomerCsvItemReader.java # Custom CSV reader
│   └── listener/
│       └── JobCompletionListener.java # Job execution listener
├── service/
│   └── BatchJobService.java          # Service for job management
├── controller/
│   └── BatchJobController.java       # REST endpoints for job control
└── scheduler/
    └── BatchJobScheduler.java        # Scheduled job execution

src/main/resources/
├── application.properties             # Application configuration
└── data/
    └── customers.csv                  # Sample data for processing
```

## 🏗️ Jobs and Steps

### 1. Import Customer Job (`importCustomerJob`)
- **Purpose**: Read customer data from CSV and save to database
- **Components**:
  - **Reader**: `FlatFileItemReader` - reads CSV files
  - **Processor**: `CustomerItemProcessor` - validates and transforms data
  - **Writer**: `CustomerItemWriter` - saves to H2 database
- **Features**: Validation, data transformation, error handling

### 2. Complex Data Processing Job (`complexDataProcessingJob`)
- **Purpose**: Multi-step processing workflow
- **Steps**:
  1. **CSV to DB Step**: Import customer data
  2. **Data Validation Step**: Custom validation logic
  3. **Data Cleanup Step**: Post-processing cleanup
- **Features**: Step chaining, custom tasklets

## 🛠️ Technology Stack

- **Spring Boot 3.5.0**
- **Spring Batch** - Batch processing framework
- **Spring Data JPA** - Database operations
- **H2 Database** - In-memory database for demo
- **OpenCSV** - CSV file processing
- **Spring Boot Actuator** - Monitoring and metrics
- **Maven** - Dependency management

## 🚦 Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher

### Running the Application

1. **Clone and navigate to the project:**
   ```bash
   cd Spring_batch_demo
   ```

2. **Build the project:**
   ```bash
   ./mvnw clean compile
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the application:**
   - Application: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console
   - Actuator Health: http://localhost:8080/actuator/health

## 📊 Demo Scenarios

### Scenario 1: Manual Job Execution via REST API

**Start Import Job:**
```bash
curl -X POST http://localhost:8080/api/batch/jobs/import-customers
```

**Check Customer Stats:**
```bash
curl http://localhost:8080/api/batch/customers/stats
```

**View All Customers:**
```bash
curl http://localhost:8080/api/batch/customers
```

### Scenario 2: Complex Multi-Step Job

**Start Complex Processing Job:**
```bash
curl -X POST http://localhost:8080/api/batch/jobs/complex-processing
```

### Scenario 3: Database Operations

**View H2 Console:**
1. Go to http://localhost:8080/h2-console
2. Use connection details:
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: `password`

**Query Customer Data:**
```sql
SELECT * FROM customers;
SELECT status, COUNT(*) FROM customers GROUP BY status;
SELECT city, COUNT(*) FROM customers GROUP BY city;
```

### Scenario 4: Monitoring and Metrics

**Check Application Health:**
```bash
curl http://localhost:8080/actuator/health
```

**View Batch Metrics:**
```bash
curl http://localhost:8080/actuator/metrics
```

## 🔧 Configuration Options

### Batch Configuration (`application.properties`)
```properties
# Chunk size for batch processing
batch.chunk.size=10

# Spring Batch settings
spring.batch.job.enabled=false
spring.batch.jdbc.initialize-schema=always
```

### Job Parameters
You can pass custom parameters when starting jobs:
```bash
curl -X POST "http://localhost:8080/api/batch/jobs/import-customers?param1=value1&param2=value2"
```

## 📈 Key Learning Points

### 1. **Chunk-based Processing**
- Data is processed in configurable chunks (default: 10 records)
- Improves memory efficiency for large datasets
- Provides transaction boundaries

### 2. **Error Handling**
- Invalid data is marked with `FAILED` status but still persisted
- Validation failures are logged for debugging
- Jobs continue processing despite individual record failures

### 3. **Job Monitoring**
- Comprehensive logging at all levels
- Job execution statistics and timing
- Database statistics after job completion

### 4. **Flexible Architecture**
- Custom readers, processors, and writers
- Configurable chunk sizes and parameters
- Easy to extend with additional processing logic

### 5. **Integration Patterns**
- REST API integration for job management
- Scheduled job execution
- Database integration with JPA
- Spring Boot ecosystem integration

## 🐛 Sample Data Issues (Intentional)

The sample CSV includes intentionally problematic data to demonstrate error handling:
- Invalid email formats
- Non-numeric age values
- Empty first/last names
- Senior citizens (age > 65) for special processing

## 🔍 Monitoring Job Execution

### Log Output
Monitor the console for detailed job execution logs including:
- Job start/completion times
- Read/write/skip counts
- Validation failures
- Database statistics

### Database Monitoring
Check the `customers` table to see:
- Total records processed
- Success/failure distribution
- Data transformation results

## 🎯 Demo Script

For a comprehensive demo, follow this sequence:

1. **Start the application** and show the log output
2. **Execute import job** via REST API
3. **Check H2 console** to see the data
4. **Show customer statistics** via API
5. **Run complex processing job** to demonstrate multi-step workflows
6. **Clear data** and run again to show repeatability
7. **Explain scheduling** features and configuration options

This Spring Batch demo showcases enterprise-grade batch processing capabilities including data ingestion, transformation, validation, error handling, and monitoring - all essential features for production batch applications.

## 📚 Additional Resources

- [Spring Batch Documentation](https://spring.io/projects/spring-batch)
- [Spring Boot Batch Guide](https://spring.io/guides/gs/batch-processing/)
- [H2 Database Documentation](http://www.h2database.com/html/main.html) 