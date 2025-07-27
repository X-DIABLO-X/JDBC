# Courier Service Management System

A comprehensive Java-based Courier Service Management System built using JDBC for database connectivity and MySQL for data storage. This system provides complete functionality for managing courier shipments, tracking packages, managing drivers, and generating various reports.

## 🚀 Features

### Core Functionality
- **Database Management**: Automated database and table creation
- **Sample Data Population**: Insert realistic Indian courier service data
- **Interactive Menu System**: User-friendly console interface
- **Secure Database Connection**: Dynamic password input with connection testing

### Shipment Management
- Track shipment status and location history
- Manage shipment lifecycle (pending → in_transit → delivered)
- Handle delayed and returned shipments
- Support for multiple packages per shipment

### Driver Operations
- Assign drivers to shipments
- Track pending shipments for specific drivers
- Manage driver workload and capacity
- Driver performance monitoring

### Location & Route Management
- Multi-level location hierarchy (hubs, sub_hubs, delivery_points)
- Origin and destination tracking
- Location-based analytics

### Analytics & Reporting
- **Shipment Status Tracking**: Real-time status and location log for any shipment
- **Driver Workload**: List pending shipments assigned to specific drivers
- **Performance Analytics**: Average delivery time per hub/location
- **Delay Analysis**: Identify and track delayed shipments
- **Volume Reports**: Daily shipment volume by origin location
- **Diagnostic Tools**: Database status and shipment overview

## 📋 Prerequisites

### System Requirements
- **Java**: JDK 8 or higher
- **MySQL**: MySQL Server 5.7 or higher
- **Operating System**: Windows, macOS, or Linux

### Database Setup
1. Install and start MySQL Server
2. Create a MySQL user with database creation privileges
3. Note down your MySQL credentials (host, port, username, password)

## 🛠️ Build Instructions

### Method 1: Using Command Line (Recommended)

1. **Clone or download the project**
   ```bash
   # Navigate to the project directory
   cd "d:\HARSHIT\JDBC"
   ```

2. **Verify Java installation**
   ```bash
   java -version
   javac -version
   ```

3. **Compile the Java files**
   ```bash
   # Compile all Java files with MySQL connector in classpath
   javac -cp "mysql-connector-j-9.4.0.jar;." *.java
   ```

4. **Run the application**
   ```bash
   # Run the main class with MySQL connector in classpath
   java -cp "mysql-connector-j-9.4.0.jar;." main
   ```

### Method 2: Using the Pre-built JAR

1. **Run the pre-built JAR file**
   ```bash
   java -jar final.jar
   ```

### Method 3: Rebuild the JAR (Optional)

1. **Create a new JAR file**
   ```bash
   # Create JAR with manifest
   jar cfm final.jar MANIFEST.MF *.class mysql-connector-j-9.4.0.jar
   ```

2. **Run the newly created JAR**
   ```bash
   java -jar final.jar
   ```

## 📖 Usage Guide

### Initial Setup

1. **Start the Application**
   - Run the application using any of the build methods above
   - The system will prompt for MySQL connection details

2. **Database Connection Setup**
   ```
   === Courier Service Management System ===
   DATABASE CONNECTION SETUP
   Server: localhost:3306
   Username: root
   Password: [Enter your MySQL password]
   ```

3. **Main Menu Navigation**
   ```
   COURIER SERVICE MANAGEMENT SYSTEM
   1. Create Database and Tables
   2. Insert Sample Data
   3. Run Queries
   4. Exit
   ```

### Step-by-Step Operation

#### Step 1: Create Database Structure
- Select option `1` from the main menu
- This creates the `courier_service` database and all required tables:
  - `locations` - Hub and delivery point information
  - `drivers` - Driver details and contact information
  - `shipments` - Shipment records with sender/recipient details
  - `packages` - Package details associated with shipments
  - `status_logs` - Tracking history for each shipment
  - `driver_assignments` - Driver-shipment assignments

#### Step 2: Populate Sample Data
- Select option `2` from the main menu
- Inserts realistic Indian courier service data including:
  - 8 locations across major Indian cities
  - 8 drivers with Indian names and contact details
  - 8 shipments with realistic routes
  - 15 packages with various content types
  - Status logs and driver assignments

#### Step 3: Use Query Features
Select option `3` to access the query menu:

1. **Shipment Status & Location Log**
   - Enter a shipment ID to view complete tracking history
   - Shows sender, recipient, current status, and location updates

2. **Driver Pending Shipments**
   - Enter a driver ID to see all pending deliveries
   - Displays shipment details and estimated delivery times

3. **Hub Performance Analysis**
   - View average delivery times for each location
   - Compare actual vs. estimated delivery performance

4. **Delayed Shipments Report**
   - Lists all shipments exceeding estimated delivery time
   - Shows delay duration in hours

5. **Daily Volume Report**
   - Displays shipment volume by origin location
   - Covers the last 30 days of activity

6. **Diagnostic View**
   - Shows all shipments in the database
   - Useful for troubleshooting and overview

## 🗄️ Database Schema

### Tables Structure

```sql
locations: Location hierarchy and address information
├── location_id (PK)
├── location_name
├── location_type (hub/sub_hub/delivery_point)
├── parent_location_id (FK)
└── address

drivers: Driver information and contact details
├── driver_id (PK)
├── driver_name
├── license_number (UNIQUE)
├── contact_phone
├── contact_email
├── max_active_shipments
└── created_at

shipments: Main shipment records
├── shipment_id (PK)
├── sender_name, sender_address, sender_phone
├── recipient_name, recipient_address, recipient_phone
├── origin_location_id (FK), destination_location_id (FK)
├── current_status
├── created_at, estimated_delivery, actual_delivery
└── Various timestamps

packages: Package details for each shipment
├── package_id (PK)
├── shipment_id (FK)
├── weight
├── content_description
└── package_type

status_logs: Tracking history
├── log_id (PK)
├── shipment_id (FK)
├── status_update
├── location_id (FK)
├── update_timestamp
├── agent_name
└── notes

driver_assignments: Driver-shipment assignments
├── assignment_id (PK)
├── driver_id (FK)
├── shipment_id (FK)
├── assigned_at
├── estimated_delivery
└── status
```

## 🔧 Configuration

### Database Configuration
- **Host**: localhost:3306 (default)
- **Database Name**: courier_service
- **Username**: root (default)
- **Password**: User-provided during runtime

### Customization Options
- Modify location data in `functions.java` → `insertSampleData()`
- Update driver information in the same method
- Adjust shipment statuses and delivery timeframes
- Customize report formats in respective query methods

## 📁 Project Structure

```
JDBC/
├── main.java              # Main application entry point and menu system
├── create.java            # Database creation and connection management
├── functions.java         # Core business logic and query operations
├── *.class               # Compiled Java bytecode files
├── final.jar             # Pre-built executable JAR file
├── mysql-connector-j-9.4.0.jar  # MySQL JDBC driver
├── setup_database.sql    # Manual database setup script
├── cleanup_database.sql  # Database reset script
├── project.pdf          # Project documentation
└── README.md            # This file
```

## 🚨 Troubleshooting

### Common Issues

1. **Connection Failed**
   - Verify MySQL server is running
   - Check username/password credentials
   - Ensure MySQL is listening on port 3306

2. **ClassNotFoundException: MySQL Driver**
   - Ensure `mysql-connector-j-9.4.0.jar` is in the classpath
   - Use the exact classpath syntax for your operating system

3. **Permission Denied**
   - MySQL user needs CREATE, INSERT, UPDATE, DELETE, and SELECT privileges
   - Grant necessary permissions: `GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost';`

4. **Tables Already Exist**
   - The system automatically drops and recreates tables
   - Use `cleanup_database.sql` for manual cleanup if needed

5. **No Data Found**
   - Ensure you've run "Insert Sample Data" before running queries
   - Check database connection and table creation success messages

### Manual Database Setup
If the Java application fails to create the database, use the provided SQL scripts:

```bash
# Connect to MySQL
mysql -u root -p

# Run the setup script
source setup_database.sql

# Or run cleanup if needed
source cleanup_database.sql
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly with different data sets
5. Submit a pull request

## 📄 License

This project is developed for educational purposes as part of a JDBC programming assignment.

## 👨‍💻 Author

Developed by Harshit Tiwari as part of a database management and JDBC programming project.

---

## 🔍 Quick Start Commands

```bash
# Navigate to project directory
cd "d:\HARSHIT\JDBC"

# Compile and run
javac -cp "mysql-connector-j-9.4.0.jar;." *.java
java -cp "mysql-connector-j-9.4.0.jar;." main

# Or use the pre-built JAR
java -jar final.jar
```

For any issues or questions, please refer to the troubleshooting section or check the database connection settings.
