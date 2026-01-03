# Banking Transaction System - Deployment Guide

## 🚀 **Production Deployment Checklist**

This guide covers deploying the Banking Transaction System to production environments.

## 📋 **Pre-Deployment Requirements**

### **System Requirements**
- **Java**: JDK 8 or higher
- **Database**: MySQL 8.0+ or MariaDB 10.3+
- **Memory**: Minimum 512MB RAM (recommended 2GB+)
- **Storage**: 100MB for application + database storage needs
- **Network**: Port 3306 for MySQL, custom port for application

### **Security Requirements**
- **Database User**: Create dedicated user with minimal privileges
- **SSL/TLS**: Enable encrypted database connections
- **Firewall**: Restrict database access to application servers only
- **Credentials**: Use environment variables, never hardcode passwords

## 🔧 **Environment Configuration**

### **1. Database Setup**
```sql
-- Create production database
CREATE DATABASE banking_system_prod;

-- Create dedicated user
CREATE USER 'banking_app'@'%' IDENTIFIED BY 'secure_password_here';

-- Grant minimal required privileges
GRANT SELECT, INSERT, UPDATE ON banking_system_prod.* TO 'banking_app'@'%';
GRANT DELETE ON banking_system_prod.transactions TO 'banking_app'@'%';

-- Apply security settings
FLUSH PRIVILEGES;
```

### **2. Application Configuration**
```java
// Production DBConstants.java
public static final String DB_URL = System.getenv("DB_URL");
public static final String DB_USERNAME = System.getenv("DB_USERNAME");
public static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

// Production connection pool settings
public static final int MAX_POOL_SIZE = 50;
public static final int MIN_POOL_SIZE = 10;
public static final int CONNECTION_TIMEOUT = 30000;
```

### **3. Environment Variables**
```bash
# Database Configuration
export DB_URL="jdbc:mysql://prod-db-server:3306/banking_system_prod?useSSL=true"
export DB_USERNAME="banking_app"
export DB_PASSWORD="secure_password_here"

# Application Configuration
export APP_ENV="production"
export LOG_LEVEL="INFO"
export MAX_TRANSACTION_AMOUNT="100000.00"
```

## 🐳 **Docker Deployment**

### **Dockerfile**
```dockerfile
FROM openjdk:11-jre-slim

# Create app directory
WORKDIR /app

# Copy application files
COPY bin/ ./bin/
COPY lib/ ./lib/
COPY database/ ./database/

# Create non-root user
RUN groupadd -r banking && useradd -r -g banking banking
RUN chown -R banking:banking /app
USER banking

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD java -cp "bin:lib/*" com.banking.util.HealthCheck || exit 1

# Run application
CMD ["java", "-cp", "bin:lib/*", "com.banking.Main"]
```

### **Docker Compose**
```yaml
version: '3.8'

services:
  banking-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:mysql://banking-db:3306/banking_system
      - DB_USERNAME=banking_app
      - DB_PASSWORD=${DB_PASSWORD}
    depends_on:
      - banking-db
    restart: unless-stopped

  banking-db:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=banking_system
      - MYSQL_USER=banking_app
      - MYSQL_PASSWORD=${DB_PASSWORD}
    volumes:
      - banking_data:/var/lib/mysql
      - ./database/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    ports:
      - "3306:3306"
    restart: unless-stopped

volumes:
  banking_data:
```

## ☁️ **Cloud Deployment Options**

### **AWS Deployment**
```bash
# RDS Database
aws rds create-db-instance \
  --db-instance-identifier banking-prod-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password ${DB_PASSWORD} \
  --allocated-storage 20

# ECS Service
aws ecs create-service \
  --cluster banking-cluster \
  --service-name banking-service \
  --task-definition banking-task:1 \
  --desired-count 2
```

### **Google Cloud Platform**
```bash
# Cloud SQL Database
gcloud sql instances create banking-db \
  --database-version=MYSQL_8_0 \
  --tier=db-f1-micro \
  --region=us-central1

# Cloud Run Service
gcloud run deploy banking-service \
  --image gcr.io/project-id/banking-app \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

## 📊 **Monitoring & Logging**

### **Application Metrics**
```java
// Add to DBConnection.java
public static class Metrics {
    private static final AtomicLong activeConnections = new AtomicLong(0);
    private static final AtomicLong totalTransactions = new AtomicLong(0);
    private static final AtomicLong failedTransactions = new AtomicLong(0);
    
    public static void recordTransaction(boolean success) {
        totalTransactions.incrementAndGet();
        if (!success) failedTransactions.incrementAndGet();
    }
}
```

### **Health Check Endpoint**
```java
// HealthCheck.java
public class HealthCheck {
    public static boolean isHealthy() {
        try {
            return DBConnection.testConnection();
        } catch (Exception e) {
            return false;
        }
    }
}
```

### **Logging Configuration**
```java
// Add structured logging
private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

public Transaction transfer(String from, String to, BigDecimal amount) {
    logger.info("Transfer initiated: from={}, to={}, amount={}", from, to, amount);
    try {
        // ... transaction logic
        logger.info("Transfer completed: transactionId={}", transaction.getId());
        return transaction;
    } catch (Exception e) {
        logger.error("Transfer failed: from={}, to={}, amount={}, error={}", 
                    from, to, amount, e.getMessage());
        throw e;
    }
}
```

## 🔒 **Security Hardening**

### **Database Security**
```sql
-- Disable remote root access
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');

-- Remove anonymous users
DELETE FROM mysql.user WHERE User='';

-- Remove test database
DROP DATABASE IF EXISTS test;

-- Enable SSL
SET GLOBAL require_secure_transport=ON;
```

### **Application Security**
```java
// Input validation
public class SecurityUtils {
    private static final Pattern ACCOUNT_NUMBER_PATTERN = 
        Pattern.compile("^[A-Z]{3}\\d{6}$");
    
    public static boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null && 
               ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches();
    }
}
```

## 📈 **Performance Optimization**

### **Database Optimization**
```sql
-- Add performance indexes
CREATE INDEX idx_transactions_account_date ON transactions(from_account_id, created_at);
CREATE INDEX idx_transactions_status ON transactions(status) WHERE status != 'COMPLETED';

-- Optimize connection settings
SET GLOBAL max_connections = 200;
SET GLOBAL innodb_buffer_pool_size = 1073741824; -- 1GB
```

### **Application Optimization**
```java
// Connection pool tuning
public static final int MAX_POOL_SIZE = Math.max(50, Runtime.getRuntime().availableProcessors() * 4);
public static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
public static final int IDLE_TIMEOUT = 300000; // 5 minutes
```

## 🔄 **Backup & Recovery**

### **Database Backup**
```bash
#!/bin/bash
# Daily backup script
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u backup_user -p banking_system_prod > backup_${DATE}.sql
aws s3 cp backup_${DATE}.sql s3://banking-backups/
```

### **Disaster Recovery**
```bash
# Recovery procedure
mysql -u root -p banking_system_prod < backup_20240101_120000.sql
```

## 📋 **Deployment Checklist**

### **Pre-Deployment**
- [ ] Database schema applied
- [ ] Environment variables configured
- [ ] SSL certificates installed
- [ ] Firewall rules configured
- [ ] Backup procedures tested

### **Deployment**
- [ ] Application deployed
- [ ] Health checks passing
- [ ] Database connectivity verified
- [ ] Transaction processing tested
- [ ] Monitoring alerts configured

### **Post-Deployment**
- [ ] Performance metrics baseline established
- [ ] Log aggregation working
- [ ] Backup schedule verified
- [ ] Security scan completed
- [ ] Documentation updated

## 🚨 **Troubleshooting**

### **Common Issues**
1. **Connection Pool Exhaustion**: Increase pool size or reduce timeout
2. **Database Deadlocks**: Review transaction ordering and duration
3. **Memory Leaks**: Monitor connection cleanup and object lifecycle
4. **Performance Degradation**: Check query execution plans and indexes

### **Monitoring Commands**
```bash
# Check application health
curl http://localhost:8080/health

# Monitor database connections
mysql -e "SHOW PROCESSLIST;"

# Check application logs
tail -f /var/log/banking-app.log

# Monitor system resources
top -p $(pgrep java)
```

---

**This deployment guide ensures your banking system runs reliably and securely in production environments.**