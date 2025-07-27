import java.sql.*;

public class create {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "courier_service";
    private static final String USERNAME = "root";
    private static String PASSWORD = null; // Will be set from user input
    
    static {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }
    
    // Method to set the password from user input
    public static void setPassword(String password) {
        PASSWORD = password;
    }
    
    // Method to check if password is set
    public static boolean isPasswordSet() {
        return PASSWORD != null && !PASSWORD.isEmpty();
    }
    
    public static void createDatabase() {
        if (!isPasswordSet()) {
            System.err.println("Database password not set! Please set password first.");
            return;
        }
        
        try {
            // Connect to MySQL server
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            Statement stmt = conn.createStatement();
            
            // Create database
            String sql = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            stmt.executeUpdate(sql);
            System.out.println("Database created successfully...");
            
            // Use the database
            stmt.executeUpdate("USE " + DB_NAME);
            
            createTables(conn);
            
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Drop existing tables first (in reverse order due to foreign key constraints)
        System.out.println("Dropping existing tables if they exist...");
        stmt.executeUpdate("DROP TABLE IF EXISTS driver_assignments");
        stmt.executeUpdate("DROP TABLE IF EXISTS status_logs");
        stmt.executeUpdate("DROP TABLE IF EXISTS packages");
        stmt.executeUpdate("DROP TABLE IF EXISTS shipments");
        stmt.executeUpdate("DROP TABLE IF EXISTS drivers");
        stmt.executeUpdate("DROP TABLE IF EXISTS locations");
        
        System.out.println("Creating new tables...");
        
        // Create Locations table
        String locationsTable = """
            CREATE TABLE locations (
                location_id INT PRIMARY KEY AUTO_INCREMENT,
                location_name VARCHAR(255) NOT NULL,
                location_type ENUM('hub', 'sub_hub', 'delivery_point') NOT NULL,
                parent_location_id INT,
                address TEXT,
                FOREIGN KEY (parent_location_id) REFERENCES locations(location_id)
            )
        """;
        stmt.executeUpdate(locationsTable);
        
        // Create Drivers table
        String driversTable = """
            CREATE TABLE drivers (
                driver_id INT PRIMARY KEY AUTO_INCREMENT,
                driver_name VARCHAR(255) NOT NULL,
                license_number VARCHAR(50) UNIQUE NOT NULL,
                contact_phone VARCHAR(20),
                contact_email VARCHAR(255),
                max_active_shipments INT DEFAULT 5,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        stmt.executeUpdate(driversTable);
        
        // Create Shipments table
        String shipmentsTable = """
            CREATE TABLE shipments (
                shipment_id INT PRIMARY KEY AUTO_INCREMENT,
                sender_name VARCHAR(255) NOT NULL,
                sender_address TEXT NOT NULL,
                sender_phone VARCHAR(20),
                recipient_name VARCHAR(255) NOT NULL,
                recipient_address TEXT NOT NULL,
                recipient_phone VARCHAR(20),
                origin_location_id INT NOT NULL,
                destination_location_id INT NOT NULL,
                current_status ENUM('pending', 'in_transit', 'delivered', 'returned', 'delayed') DEFAULT 'pending',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                estimated_delivery DATETIME,
                actual_delivery DATETIME,
                FOREIGN KEY (origin_location_id) REFERENCES locations(location_id),
                FOREIGN KEY (destination_location_id) REFERENCES locations(location_id)
            )
        """;
        stmt.executeUpdate(shipmentsTable);
        
        // Create Packages table
        String packagesTable = """
            CREATE TABLE packages (
                package_id INT PRIMARY KEY AUTO_INCREMENT,
                shipment_id INT NOT NULL,
                weight DECIMAL(10,2) NOT NULL,
                content_description TEXT,
                package_type VARCHAR(100),
                FOREIGN KEY (shipment_id) REFERENCES shipments(shipment_id) ON DELETE CASCADE
            )
        """;
        stmt.executeUpdate(packagesTable);
        
        // Create Status_Logs table
        String statusLogsTable = """
            CREATE TABLE status_logs (
                log_id INT PRIMARY KEY AUTO_INCREMENT,
                shipment_id INT NOT NULL,
                status_update ENUM('pending', 'in_transit', 'delivered', 'returned', 'delayed') NOT NULL,
                location_id INT,
                update_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                agent_name VARCHAR(255),
                notes TEXT,
                FOREIGN KEY (shipment_id) REFERENCES shipments(shipment_id) ON DELETE CASCADE,
                FOREIGN KEY (location_id) REFERENCES locations(location_id)
            )
        """;
        stmt.executeUpdate(statusLogsTable);
        
        // Create Driver_Assignments table
        String driverAssignmentsTable = """
            CREATE TABLE driver_assignments (
                assignment_id INT PRIMARY KEY AUTO_INCREMENT,
                driver_id INT NOT NULL,
                shipment_id INT NOT NULL,
                assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                estimated_delivery DATETIME,
                status ENUM('active', 'completed', 'cancelled') DEFAULT 'active',
                FOREIGN KEY (driver_id) REFERENCES drivers(driver_id),
                FOREIGN KEY (shipment_id) REFERENCES shipments(shipment_id)
            )
        """;
        stmt.executeUpdate(driverAssignmentsTable);
        
        // Create indexes for better performance (with error handling for existing indexes)
        createIndexSafely(stmt, "idx_shipment_status", "shipments", "current_status");
        createIndexSafely(stmt, "idx_shipment_created", "shipments", "created_at");
        createIndexSafely(stmt, "idx_status_logs_shipment", "status_logs", "shipment_id");
        createIndexSafely(stmt, "idx_driver_assignments_driver", "driver_assignments", "driver_id");
        createIndexSafely(stmt, "idx_driver_assignments_status", "driver_assignments", "status");
        
        System.out.println("All tables created successfully...");
        stmt.close();
    }
    
    private static void createIndexSafely(Statement stmt, String indexName, String tableName, String columnName) {
        try {
            String createIndexSQL = "CREATE INDEX " + indexName + " ON " + tableName + "(" + columnName + ")";
            stmt.executeUpdate(createIndexSQL);
            System.out.println("Index " + indexName + " created successfully.");
        } catch (SQLException e) {
            // Index might already exist, check if it's a "Duplicate key name" error
            if (e.getMessage().contains("Duplicate key name") || e.getMessage().contains("already exists")) {
                System.out.println("Index " + indexName + " already exists, skipping.");
            } else {
                System.err.println("Warning: Could not create index " + indexName + ": " + e.getMessage());
            }
        }
    }
    
    public static Connection getConnection() {
        if (!isPasswordSet()) {
            System.err.println("Database password not set! Please set password first.");
            return null;
        }
        
        try {
            return DriverManager.getConnection(DB_URL + DB_NAME, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
