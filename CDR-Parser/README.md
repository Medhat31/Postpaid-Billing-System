# CDR Parser

A Java-based Call Detail Record (CDR) ingestion service. The application monitors a specified directory for incoming Call Detail Records in CSV format, parses them using OpenCSV, writes the parsed data to a PostgreSQL database via jOOQ (Java Object-Oriented Querying), and archives the processed files.

## Features

- **Real-Time Directory Monitoring**: Leverages Java's NIO `WatchService` to detect newly created `.csv` files.
- **Robust CSV Mapping**: Automatically binds CSV headers directly to Java beans using `OpenCSV` annotations.
- **SQL Generation with jOOQ**: Interacts with PostgreSQL using compile-safe SQL queries generated dynamically from the database schema.
- **Automated Resource Management**: Registers JVM shutdown hooks to gracefully close database connection pools and handles I/O streams safely.
- **Complete Documentation**: Fully documented codebase compiling with zero warnings and generating Javadocs including private members.

---

## Prerequisites

- **Java Development Kit (JDK)**: Version 21 or higher.
- **Build Tool**: Apache Maven 3.6.0+.
- **Database**: PostgreSQL (e.g., Neon Postgres Cloud Database is pre-configured).

---

## Getting Started

### 1. Configuration

The application is configured using two properties files located in the root of the project:

#### `io.properties`
Configures the directory watching service paths:
```properties
input.dir=/opt/billing/cdr
output.dir=/opt/billing/archive
```
- `input.dir`: The directory the service monitors for incoming `.csv` files.
- `output.dir`: The directory where files are moved once they have been successfully parsed and stored.

#### `db.properties`
Configures database credentials and connection parameters:
```properties
db.url=jdbc:postgresql://<host>/neondb?sslmode=require&channelBinding=require
db.user=<username>
db.password=<password>
```

### 2. Database Schema

The application expects a PostgreSQL table named `cdr` in the `public` schema. The jOOQ code generator relies on this table structure:

```sql
CREATE TABLE cdr (
    cdr_id SERIAL PRIMARY KEY,
    contract_id INTEGER NOT NULL,
    dial_a VARCHAR(20) NOT NULL,
    dial_b VARCHAR(20) NOT NULL,
    service_type SMALLINT NOT NULL,
    duration DECIMAL(10, 2) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    external_fee_piasters INTEGER NOT NULL,
    is_rated BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for performance optimizations
CREATE INDEX idx_cdr_contract ON cdr(contract_id);
CREATE INDEX idx_cdr_is_rated ON cdr(is_rated);
```

---

## Build and Run Instructions

### Compilation and Code Generation
Generate the jOOQ database record classes and compile the project:
```bash
mvn compile
```

### Run the Application
Start the directory watching daemon:
```bash
mvn exec:java
```

---

## Documentation

This project enforces comprehensive doc linting standards. Javadocs can be generated (including private methods and fields) by running:

```bash
mvn javadoc:javadoc
```

The resulting HTML documentation will be generated in `target/site/apidocs/index.html`.

---

## Project Structure

```
├── pom.xml                   # Maven project configuration (Java 21, OpenCSV, jOOQ, Javadoc plugin)
├── db.properties             # Database credentials
├── io.properties             # File path configurations
├── src
│   └── main
│       └── java
│           └── com
│               └── billing
│               │   └── cdrparser
│               │       ├── CDRParser.java       # Main entry point and directory watcher
│               │       ├── CsvReader.java       # CSV processing utility using OpenCSV
│               │       ├── DatabaseAdaptor.java # Database connection management and DSL context builder
│               │       ├── DatabaseWriter.java  # Persistent writing of LineItems via jOOQ
│               │       └── LineItem.java        # CDR Java Bean mapped from CSV
```
