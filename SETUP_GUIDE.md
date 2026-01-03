# Banking Transaction System - Setup Guide

## 🚀 Quick Start Guide

This guide will help you set up and run the Banking Transaction System on your local machine.

## 📋 Prerequisites

### Required Software
1. **Java Development Kit (JDK) 8 or higher**
   ```bash
   java -version
   javac -version
   ```

2. **MySQL Server 8.0 or higher**
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Or use Docker: `docker run --name mysql-banking -e MYSQL_ROOT_PASSWORD=password -p 3306:3306 -d mysql:8.0`

3. **MySQL JDBC Driver**
   - Download `mysql-connector-java-8.0.x.jar` from: https://dev.mysql.com/downloads/connector/j/
   - Place it in the `lib/` directory of your project

## 🛠️ Setup Instructions

### Step 1: Database Setup

1. **Start MySQL Server**
   ```bash
   # On Windows (if installed as service)
   net start mysql

   # On macOS with Homebrew
   brew services start mysql

   # On Linux
   sudo systemctl start mysql
   ```

2. **Create Database**
   ```sql
   # Connect to MySQL as root
   mysql -u root -p

   # Create the database
   CREATE DATABASE banking_system;
   USE banking_system;

   # Exit MySQL
   exit;
   ```

3. **Run Database Schema**
   ```bash
   # From project root directory
   mysql -u root -p banking_system < database/schema.sql
   ```

4. **Verify Tables Created**
   ```sql
   mysql -u root -p banking_system
   SHOW TABLES;
   # Should show: customers, accounts, transactions
   ```

### Step 2: Configure Database Connection

1. **Update Database Credentials**
   
   Edit `src/main/java/com/banking/util/DBConstants.java`:
   ```java
   public static final String DB_USERNAME = "your_mysql_username";
   public static final String DB_PASSWORD = "your_mysql_password";
   ```

2. **Verify Database URL**
   ```java
   public static final String DB_URL = "jdbc:mysql://localhost:3306/banking_system";
   ```

### Step 3: Project Structure Setup

1. **Create Required Directories**
   ```bash
   mkdir -p lib
   mkdir -p bin
   ```

2. **Download MySQL JDBC Driver**
   - Download `mysql-connector-java-8.0.x.jar`
   - Place it in the `lib/` directory

### Step 4: Compile the Project

```bash
# Navigate to project root
cd banking-transaction-system-java

# Compile all Java files
javac -cp "lib/mysql-connector-java-8.0.x.jar" -d bin src/main/java/com/banking/**/*.java
```

### Step 5: Run the Application

```bash
# Run the main application
java -cp "bin:lib/mysql-connector-java-8.0.x.jar" com.banking.Main
```

**On Windows:**
```cmd
java -cp "bin;lib/mysql-connector-java-8.0.x.jar" com.banking.Main
```

## 🎯 First Time Usage

### 1. Create Your First Customer
- Choose option `1` (Customer Management)
- Choose option `1` (Create New Customer)
- Fill in the required information

### 2. Open Your First Account
- Choose option `2` (Account Management)
- Choose option `1` (Open New Account)
- Enter the customer ID from step 1
- Select account type and initial deposit

### 3. Perform Your First Transaction
- Choose option `3` (Transactions)
- Try a deposit, withdrawal, or transfer

## 🔧 Configuration Options

### Database Connection Pool Settings
In `DBConstants.java`, you can adjust:
```java
public static final int MAX_POOL_SIZE = 20;        // Maximum connections
public static final int MIN_POOL_SIZE = 5;         // Minimum connections
public static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
```

### Transaction Limits
In `TransactionManager.java`, you can modify:
```java
BigDecimal maxAmount = new BigDecimal("100000.00"); // Maximum transaction amount
```

## 🐛 Troubleshooting

### Common Issues

1. **"Database connection failed"**
   - Check if MySQL server is running
   - Verify username/password in `DBConstants.java`
   - Ensure database `banking_system` exists

2. **"ClassNotFoundException: com.mysql.cj.jdbc.Driver"**
   - Ensure MySQL JDBC driver is in the `lib/` directory
   - Check classpath includes the JAR file

3. **"Table doesn't exist"**
   - Run the database schema: `mysql -u root -p banking_system < database/schema.sql`
   - Verify tables exist: `SHOW TABLES;`

4. **"Access denied for user"**
   - Check MySQL username/password
   - Grant necessary permissions:
     ```sql
     GRANT ALL PRIVILEGES ON banking_system.* TO 'your_username'@'localhost';
     FLUSH PRIVILEGES;
     ```

### Performance Issues

1. **Slow Database Operations**
   - Check database indexes are created (included in schema.sql)
   - Monitor connection pool size
   - Consider increasing `MAX_POOL_SIZE`

2. **Memory Issues**
   - Increase JVM heap size: `java -Xmx512m -Xms256m ...`
   - Monitor connection pool for leaks

## 📊 Sample Data

The database schema includes sample data for testing:
- 3 sample customers
- 4 sample accounts
- 4 sample transactions

You can use this data to test the application immediately after setup.

## 🔒 Security Considerations

### For Production Use:
1. **Change Default Passwords**
   - Use strong, unique passwords for database users
   - Consider using environment variables for credentials

2. **Database Security**
   - Create dedicated database user with minimal privileges
   - Enable SSL connections
   - Regular security updates

3. **Application Security**
   - Input validation (already implemented)
   - SQL injection protection (using PreparedStatements)
   - Transaction logging for audit trails

## 📈 Monitoring

### Database Monitoring
```sql
-- Check active connections
SHOW PROCESSLIST;

-- Check table sizes
SELECT table_name, table_rows 
FROM information_schema.tables 
WHERE table_schema = 'banking_system';
```

### Application Monitoring
- Connection pool size: Available in System Information menu
- Transaction success/failure rates: Check transaction status in database
- Performance metrics: Monitor transaction completion times

## 🆘 Getting Help

If you encounter issues:

1. **Check the logs** - Application prints detailed error messages
2. **Verify database connectivity** - Use the built-in connection test
3. **Review configuration** - Double-check `DBConstants.java`
4. **Test with sample data** - Use the provided sample data for testing

## 🎉 Success!

If you see the main menu with database connection confirmation, you're ready to use the Banking Transaction System!

```
🏦 Welcome to the Banking Transaction System
==========================================
✅ Database connection established successfully

==================================================
🏦 BANKING TRANSACTION SYSTEM - MAIN MENU
==================================================
1. 👤 Customer Management
2. 💳 Account Management
3. 💰 Transactions
4. 📊 Reports & Statements
5. ⚙️  System Information
0. 🚪 Exit
==================================================
```