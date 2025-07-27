import java.util.Scanner;

public class main {
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("=== " + ResourceLoader.getApplicationInfo() + " ===");
        System.out.println("Initializing system...");
        
        // Get MySQL password from user
        getMySQLPassword();
        
        boolean running = true;
        
        while (running) {
            displayMainMenu();
            int choice = getMenuChoice();
            
            switch (choice) {
                case 1:
                    createDatabaseAndTables();
                    break;
                case 2:
                    insertSampleData();
                    break;
                case 3:
                    runQueryMenu();
                    break;
                case 4:
                    System.out.println("Thank you for using Courier Service Management System!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        
        scanner.close();
    }
    
    private static void getMySQLPassword() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("DATABASE CONNECTION SETUP");
        System.out.println("=".repeat(50));
        System.out.println("Please enter your MySQL connection details:");
        System.out.println("Server: localhost:3306");
        System.out.println("Username: root");
        
        // Get password using simple text input
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        // Set the password in the create class
        create.setPassword(password);
        
        // Test the connection
        System.out.print("Testing database connection... ");
        try {
            java.sql.Connection testConn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/", "root", password);
            testConn.close();
            System.out.println("✓ Connection successful!");
        } catch (java.sql.SQLException e) {
            System.out.println("✗ Connection failed!");
            System.out.println("Error: " + e.getMessage());
            System.out.println("Please check your MySQL server and credentials.");
            System.exit(1);
        }
        
        System.out.println("=".repeat(50));
    }
    
    private static void displayMainMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("COURIER SERVICE MANAGEMENT SYSTEM");
        System.out.println("=".repeat(50));
        System.out.println("1. Create Database and Tables");
        System.out.println("2. Insert Sample Data");
        System.out.println("3. Run Queries");
        System.out.println("4. Exit");
        System.out.println("=".repeat(50));
        System.out.print("Enter your choice (1-4): ");
    }
    
    private static void runQueryMenu() {
        boolean queryRunning = true;
        
        while (queryRunning) {
            displayQueryMenu();
            int choice = getMenuChoice();
            
            switch (choice) {
                case 1:
                    System.out.print("Enter Shipment ID: ");
                    int shipmentId = scanner.nextInt();
                    functions.getShipmentStatusAndLocation(shipmentId);
                    break;
                case 2:
                    System.out.print("Enter Driver ID: ");
                    int driverId = scanner.nextInt();
                    functions.getPendingShipmentsForDriver(driverId);
                    break;
                case 3:
                    functions.getAverageDeliveryTimePerHub();
                    break;
                case 4:
                    functions.getDelayedShipments();
                    break;
                case 5:
                    functions.getDailyShipmentVolumeByOrigin();
                    break;
                case 6:
                    functions.showAllShipments();
                    break;
                case 7:
                    queryRunning = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
            
            if (queryRunning) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine(); // consume newline
                scanner.nextLine(); // wait for user input
            }
        }
    }
    
    private static void displayQueryMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("QUERY OPERATIONS");
        System.out.println("=".repeat(50));
        System.out.println("1. Get Shipment Status and Location Log");
        System.out.println("2. List Pending Shipments for Driver");
        System.out.println("3. Average Delivery Time per Hub");
        System.out.println("4. Show Delayed Shipments");
        System.out.println("5. Daily Shipment Volume by Origin");
        System.out.println("6. Show All Shipments (Diagnostic)");
        System.out.println("7. Back to Main Menu");
        System.out.println("=".repeat(50));
        System.out.print("Enter your choice (1-7): ");
    }
    
    private static void createDatabaseAndTables() {
        System.out.println("\nCreating database and tables...");
        try {
            create.createDatabase();
            System.out.println("✓ Database and tables created successfully!");
        } catch (Exception e) {
            System.out.println("✗ Error creating database: " + e.getMessage());
            System.out.println("\nPlease ensure:");
            System.out.println("1. MySQL server is running");
            System.out.println("2. MySQL JDBC driver is in classpath");
            System.out.println("3. Username/password in create.java are correct");
        }
    }
    
    private static void insertSampleData() {
        System.out.println("\nInserting sample data...");
        try {
            functions.insertSampleData();
            System.out.println("✓ Sample data inserted successfully!");
        } catch (Exception e) {
            System.out.println("✗ Error inserting sample data: " + e.getMessage());
            System.out.println("Please ensure the database and tables are created first.");
        }
    }
    
    private static int getMenuChoice() {
        try {
            return scanner.nextInt();
        } catch (Exception e) {
            scanner.nextLine(); // clear invalid input
            return -1;
        }
    }
}
