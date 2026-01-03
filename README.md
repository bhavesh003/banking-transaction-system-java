# Banking Transaction System

A comprehensive Java-based banking system built with MySQL that handles customer accounts, transactions, and banking operations with enterprise-grade security and reliability.

## 🚀 Features

### Core Banking Operations
- **Customer Management**: Create, update, and manage customer profiles
- **Multi-Account Support**: Savings, Checking, and Business accounts with specific business rules
- **Secure Transactions**: Deposits, withdrawals, and transfers with ACID compliance
- **Transaction History**: Complete audit trail with real-time balance tracking
- **Account Statements**: Generate detailed statements for any date range

### Technical Excellence
- **Thread-Safe Operations**: Handles concurrent transactions safely
- **ACID Transaction Management**: Database transactions with commit/rollback
- **Connection Pooling**: Efficient database connection management
- **Input Validation**: Comprehensive validation with meaningful error messages
- **SQL Injection Protection**: All queries use PreparedStatements

## 🏗️ Architecture

```
src/main/java/com/banking/
├── dao/                         # Data Access Layer
│   ├── CustomerDAO.java         # Customer data operations interface
│   ├── AccountDAO.java          # Account data operations interface
│   ├── TransactionDAO.java      # Transaction data operations interface
│   └── impl/                    # DAO implementations
├── exception/                   # Custom Exception Handling
├── model/                       # Domain Models
│   ├── Account.java             # Abstract account base class
│   ├── SavingsAccount.java      # Savings account with interest
│   ├── CheckingAccount.java     # Checking account with overdraft
│   ├── BusinessAccount.java     # Business account with higher limits
│   ├── Customer.java            # Customer entity
│   └── Transaction.java         # Transaction entity
├── service/                     # Business Logic Layer
│   ├── CustomerService.java     # Customer business operations
│   ├── AccountService.java      # Account business operations
│   ├── TransactionService.java  # Transaction business operations
│   └── TransactionManager.java  # ACID transaction management
├── util/                        # Utilities
│   ├── DBConnection.java        # Database connection management
│   └── DBConstants.java         # Configuration constants
└── TestApp.java                 # Test application
```

## 🔧 Technology Stack

- **Language**: Java 8+
- **Database**: MySQL 8.0+ with JDBC
- **Architecture**: Clean Architecture with DAO pattern
- **Transaction Management**: JDBC-based ACID transactions
- **Concurrency**: ReentrantLocks with deadlock prevention

## 🚀 Quick Start

### Prerequisites
- Java 8+ JDK
- MySQL 8.0+
- MySQL JDBC Driver (included in `lib/` folder)

### Setup
1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/banking-transaction-system-java.git
   cd banking-transaction-system-java
   ```

2. **Setup database:**
   ```bash
   mysql -u root -p -e "CREATE DATABASE banking_system;"
   mysql -u root -p banking_system < database/schema.sql
   ```

3. **Configure database connection:**
   ```java
   // Update src/main/java/com/banking/util/DBConstants.java
   public static final String DB_USERNAME = "your_username";
   public static final String DB_PASSWORD = "your_password";
   ```

4. **Compile and run:**
   ```bash
   # Create bin directory
   mkdir bin
   
   # Compile
   javac -cp "lib/mysql-connector-j-9.5.0.jar" -d bin src/main/java/com/banking/TestApp.java src/main/java/com/banking/model/*.java src/main/java/com/banking/util/*.java
   
   # Run
   java -cp "bin;lib/mysql-connector-j-9.5.0.jar" com.banking.TestApp
   ```

## 💡 Usage Examples

### Customer Management
```java
// Create a new customer
Customer customer = new Customer("CUST001", "John", "Doe", 
    "john@email.com", "+1-555-0101", "123 Main St", 
    LocalDate.of(1985, 6, 15), "123-45-6789");
```

### Account Operations
```java
// Create different account types
SavingsAccount savings = new SavingsAccount("SAV001", "CUST001", new BigDecimal("1000.00"));
CheckingAccount checking = new CheckingAccount("CHK001", "CUST001", new BigDecimal("500.00"));
BusinessAccount business = new BusinessAccount("BUS001", "CUST001", 
    new BigDecimal("5000.00"), "My Business LLC", "12-3456789");
```

### Transaction Processing
```java
// Create transactions
Transaction deposit = new Transaction("TXN001", null, "SAV001", 
    TransactionType.DEPOSIT, new BigDecimal("200.00"), "Deposit");
    
Transaction transfer = new Transaction("TXN002", "SAV001", "CHK001", 
    TransactionType.TRANSFER, new BigDecimal("100.00"), "Transfer");
```

## 🛡️ Security Features

- **SQL Injection Protection**: All queries use PreparedStatements
- **Input Validation**: Comprehensive validation with sanitization
- **ACID Transactions**: Ensures data consistency and integrity
- **Connection Security**: Proper connection management and cleanup

## 📊 Database Design

The system uses a normalized MySQL database with three main tables:
- **customers**: Customer information and profiles
- **accounts**: Account details with type-specific fields
- **transactions**: Transaction records with full audit trail

Key features:
- Foreign key constraints for referential integrity
- Check constraints for business rule enforcement
- Strategic indexing for query performance
- Proper data types for financial accuracy

## 🎯 Business Rules

### Account Types
- **Savings**: 2.5% interest rate, $100 minimum balance
- **Checking**: Overdraft protection, $50 minimum balance
- **Business**: Higher transaction limits, business license required

### Transaction Rules
- All monetary values use BigDecimal for precision
- Transactions are atomic (all-or-nothing)
- Complete audit trail for all operations
- Real-time balance updates

## 🔮 Future Enhancements

- REST API implementation
- Web-based user interface
- Mobile application support
- Advanced reporting features
- Integration with external payment systems

## 📝 Documentation

- **[SETUP_GUIDE.md](SETUP_GUIDE.md)**: Detailed setup instructions
- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)**: Production deployment guide
- **Database Schema**: Complete SQL schema with sample data

## 🤝 Contributing

Feel free to fork this project and submit pull requests. For major changes, please open an issue first to discuss what you would like to change.

---
