import java.sql.*;

public class functions {
    
    // 1. Retrieve current status and location log for a shipment
    public static void getShipmentStatusAndLocation(int shipmentId) {
        String query = """
            SELECT s.shipment_id, s.sender_name, s.recipient_name, s.current_status,
                   s.created_at, s.estimated_delivery, s.actual_delivery,
                   sl.status_update, sl.update_timestamp, sl.agent_name, sl.notes,
                   l.location_name, l.location_type
            FROM shipments s
            LEFT JOIN status_logs sl ON s.shipment_id = sl.shipment_id
            LEFT JOIN locations l ON sl.location_id = l.location_id
            WHERE s.shipment_id = ?
            ORDER BY sl.update_timestamp DESC
        """;
        
        try (Connection conn = create.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, shipmentId);
            ResultSet rs = pstmt.executeQuery();
            
            boolean found = false;
            System.out.println("\n=== Shipment Status and Location Log ===");
            
            while (rs.next()) {
                if (!found) {
                    System.out.println("Shipment ID: " + rs.getInt("shipment_id"));
                    System.out.println("Sender: " + rs.getString("sender_name"));
                    System.out.println("Recipient: " + rs.getString("recipient_name"));
                    System.out.println("Current Status: " + rs.getString("current_status"));
                    System.out.println("Created: " + rs.getTimestamp("created_at"));
                    System.out.println("Estimated Delivery: " + rs.getTimestamp("estimated_delivery"));
                    System.out.println("Actual Delivery: " + rs.getTimestamp("actual_delivery"));
                    System.out.println("\nLocation History:");
                    found = true;
                }
                
                if (rs.getString("status_update") != null) {
                    System.out.println("- " + rs.getTimestamp("update_timestamp") + 
                                     " | Status: " + rs.getString("status_update") +
                                     " | Location: " + rs.getString("location_name") +
                                     " | Agent: " + rs.getString("agent_name") +
                                     " | Notes: " + rs.getString("notes"));
                }
            }
            
            if (!found) {
                System.out.println("No shipment found with ID: " + shipmentId);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // 2. List all pending shipments assigned to a driver
    public static void getPendingShipmentsForDriver(int driverId) {
        String query = """
            SELECT s.shipment_id, s.sender_name, s.recipient_name, s.current_status,
                   s.created_at, da.estimated_delivery, l1.location_name as origin,
                   l2.location_name as destination
            FROM shipments s
            JOIN driver_assignments da ON s.shipment_id = da.shipment_id
            JOIN locations l1 ON s.origin_location_id = l1.location_id
            JOIN locations l2 ON s.destination_location_id = l2.location_id
            WHERE da.driver_id = ? AND s.current_status = 'pending' AND da.status = 'active'
            ORDER BY da.estimated_delivery
        """;
        
        try (Connection conn = create.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, driverId);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("\n=== Pending Shipments for Driver ID: " + driverId + " ===");
            boolean found = false;
            
            while (rs.next()) {
                found = true;
                System.out.println("Shipment ID: " + rs.getInt("shipment_id"));
                System.out.println("From: " + rs.getString("sender_name") + " (" + rs.getString("origin") + ")");
                System.out.println("To: " + rs.getString("recipient_name") + " (" + rs.getString("destination") + ")");
                System.out.println("Status: " + rs.getString("current_status"));
                System.out.println("Created: " + rs.getTimestamp("created_at"));
                System.out.println("Estimated Delivery: " + rs.getTimestamp("estimated_delivery"));
                System.out.println("---");
            }
            
            if (!found) {
                System.out.println("No pending shipments found for this driver.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // 3. Find average delivery time per route hub
    public static void getAverageDeliveryTimePerHub() {
        String query = """
            SELECT l.location_name, l.location_type,
                   COUNT(s.shipment_id) as total_deliveries,
                   AVG(TIMESTAMPDIFF(HOUR, s.created_at, s.actual_delivery)) as avg_total_hours,
                   AVG(TIMESTAMPDIFF(HOUR, s.estimated_delivery, s.actual_delivery)) as avg_variance_hours
            FROM shipments s
            JOIN locations l ON s.destination_location_id = l.location_id
            WHERE s.actual_delivery IS NOT NULL
            GROUP BY l.location_id, l.location_name, l.location_type
            ORDER BY avg_total_hours
        """;
        
        try (Connection conn = create.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            System.out.println("\n=== Average Delivery Time Per Hub ===");
            System.out.printf("%-30s %-15s %-12s %-15s %-20s%n", "Location", "Type", "Deliveries", "Total Hours", "vs Estimate");
            System.out.println("=".repeat(95));
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                double totalHours = rs.getDouble("avg_total_hours");
                double varianceHours = rs.getDouble("avg_variance_hours");
                String varianceStr = varianceHours < 0 ? 
                    String.format("%.1f hrs early", Math.abs(varianceHours)) : 
                    String.format("%.1f hrs late", varianceHours);
                    
                System.out.printf("%-30s %-15s %-12d %-15.1f %-20s%n",
                    rs.getString("location_name"),
                    rs.getString("location_type"),
                    rs.getInt("total_deliveries"),
                    totalHours,
                    varianceStr);
            }
            
            if (!found) {
                System.out.println("No delivery data available.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // 4. Show shipments delayed beyond estimated delivery
    public static void getDelayedShipments() {
        String query = """
            SELECT s.shipment_id, s.sender_name, s.recipient_name, s.current_status,
                   s.estimated_delivery, s.actual_delivery,
                   TIMESTAMPDIFF(HOUR, s.estimated_delivery, COALESCE(s.actual_delivery, NOW())) as delay_hours,
                   l1.location_name as origin, l2.location_name as destination
            FROM shipments s
            JOIN locations l1 ON s.origin_location_id = l1.location_id
            JOIN locations l2 ON s.destination_location_id = l2.location_id
            WHERE s.estimated_delivery < COALESCE(s.actual_delivery, NOW())
            ORDER BY delay_hours DESC
        """;
        
        try (Connection conn = create.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            System.out.println("\n=== Delayed Shipments ===");
            System.out.printf("%-10s %-20s %-20s %-15s %-15s %-10s%n", 
                "ID", "Sender", "Recipient", "Status", "Delay(hrs)", "Origin");
            System.out.println("=".repeat(100));
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-10d %-20s %-20s %-15s %-15.1f %-10s%n",
                    rs.getInt("shipment_id"),
                    rs.getString("sender_name"),
                    rs.getString("recipient_name"),
                    rs.getString("current_status"),
                    rs.getDouble("delay_hours"),
                    rs.getString("origin"));
            }
            
            if (!found) {
                System.out.println("No delayed shipments found.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // 5. Generate daily shipment volume by origin
    public static void getDailyShipmentVolumeByOrigin() {
        String query = """
            SELECT DATE(s.created_at) as shipment_date,
                   l.location_name as origin_location,
                   COUNT(s.shipment_id) as shipment_count
            FROM shipments s
            JOIN locations l ON s.origin_location_id = l.location_id
            WHERE s.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
            GROUP BY DATE(s.created_at), l.location_id, l.location_name
            ORDER BY shipment_date DESC, shipment_count DESC
        """;
        
        try (Connection conn = create.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            System.out.println("\n=== Daily Shipment Volume by Origin (Last 30 Days) ===");
            System.out.printf("%-15s %-30s %-15s%n", "Date", "Origin Location", "Shipments");
            System.out.println("=".repeat(60));
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-15s %-30s %-15d%n",
                    rs.getDate("shipment_date"),
                    rs.getString("origin_location"),
                    rs.getInt("shipment_count"));
            }
            
            if (!found) {
                System.out.println("No shipment data found for the last 30 days.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Additional utility functions for data insertion
    public static void insertSampleData() {
        try (Connection conn = create.getConnection()) {
            System.out.println("Inserting sample Indian data...");
            
            // Insert sample locations with Indian cities
            String locQuery = "INSERT INTO locations (location_name, location_type, address) VALUES (?, ?, ?)";
            PreparedStatement locStmt = conn.prepareStatement(locQuery);
            
            String[] locations = {"Mumbai Central Hub", "Delhi North Hub", "Bangalore Tech Hub", "Pune Delivery Center", 
                                "Hyderabad HITEC Hub", "Chennai Express Point", "Kolkata East Hub", "Ahmedabad Commercial Hub"};
            String[] types = {"hub", "sub_hub", "sub_hub", "delivery_point", "sub_hub", "delivery_point", "sub_hub", "delivery_point"};
            String[] addresses = {"Andheri East, Mumbai, Maharashtra 400069", "Connaught Place, New Delhi, Delhi 110001", 
                                "Koramangala, Bangalore, Karnataka 560034", "Hinjewadi Phase 1, Pune, Maharashtra 411057",
                                "HITEC City, Hyderabad, Telangana 500081", "T. Nagar, Chennai, Tamil Nadu 600017",
                                "Salt Lake City, Kolkata, West Bengal 700064", "Satellite, Ahmedabad, Gujarat 380015"};
            
            for (int i = 0; i < locations.length; i++) {
                locStmt.setString(1, locations[i]);
                locStmt.setString(2, types[i]);
                locStmt.setString(3, addresses[i]);
                locStmt.executeUpdate();
            }
            System.out.println("✓ Inserted 8 locations");
            
            // Insert sample drivers with Indian names
            String driverQuery = "INSERT INTO drivers (driver_name, license_number, contact_phone, contact_email) VALUES (?, ?, ?, ?)";
            PreparedStatement driverStmt = conn.prepareStatement(driverQuery);
            
            String[] drivers = {"Rajesh Kumar Singh", "Pradeep Sharma", "Manoj Verma", "Sunil Yadav", 
                              "Ashok Pandey", "Vinod Gupta", "Deepak Mishra", "Ramesh Agarwal"};
            String[] licenses = {"MH12DL2024001", "MH14DL2024002", "KA05DL2024003", "DL08DL2024004",
                               "TG09DL2024005", "TN33DL2024006", "WB03DL2024007", "GJ01DL2024008"};
            String[] phones = {"+91-9876543210", "+91-9876543211", "+91-9876543212", "+91-9876543213",
                             "+91-9876543214", "+91-9876543215", "+91-9876543216", "+91-9876543217"};
            String[] emails = {"rajesh.singh@courier.in", "pradeep.sharma@courier.in", "manoj.verma@courier.in", "sunil.yadav@courier.in",
                             "ashok.pandey@courier.in", "vinod.gupta@courier.in", "deepak.mishra@courier.in", "ramesh.agarwal@courier.in"};
            
            for (int i = 0; i < drivers.length; i++) {
                driverStmt.setString(1, drivers[i]);
                driverStmt.setString(2, licenses[i]);
                driverStmt.setString(3, phones[i]);
                driverStmt.setString(4, emails[i]);
                driverStmt.executeUpdate();
            }
            System.out.println("✓ Inserted 8 drivers");
            
            // Insert sample shipments
            insertSampleShipments(conn);
            
            // Insert sample packages
            insertSamplePackages(conn);
            
            // Insert sample status logs
            insertSampleStatusLogs(conn);
            
            // Insert sample driver assignments
            insertSampleDriverAssignments(conn);
            
            System.out.println("✓ Sample Indian data inserted successfully!");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void insertSampleShipments(Connection conn) throws SQLException {
        // Execute direct SQL for complex date functions
        Statement directStmt = conn.createStatement();
        
        String[] shipmentSQL = {
            "INSERT INTO shipments (sender_name, sender_address, sender_phone, recipient_name, recipient_address, recipient_phone, origin_location_id, destination_location_id, created_at, estimated_delivery, current_status, actual_delivery) VALUES ('Harshit Tiwari', 'A-203, Oberoi Heights, Andheri West, Mumbai 400058', '+91-9123456789', 'Viraj Bhanage', 'B-45, Sector 21, Dwarka, New Delhi 110075', '+91-9876543210', 1, 2, NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY), 'in_transit', NULL)",
            "INSERT INTO shipments (sender_name, sender_address, sender_phone, recipient_name, recipient_address, recipient_phone, origin_location_id, destination_location_id, created_at, estimated_delivery, current_status, actual_delivery) VALUES ('Rohan Jangam', '15-4-78, Begumpet, Hyderabad 500016', '+91-9234567890', 'Pratham Jain', '204, Brigade Gateway, Rajajinagar, Bangalore 560010', '+91-9765432109', 5, 3, NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 'pending', NULL)",
            "INSERT INTO shipments (sender_name, sender_address, sender_phone, recipient_name, recipient_address, recipient_phone, origin_location_id, destination_location_id, created_at, estimated_delivery, current_status, actual_delivery) VALUES ('Ankit Kumar', 'C-102, Sushant Lok Phase 1, Gurgaon 122009', '+91-9345678901', 'Tejas Varshney', 'Plot 67, Baner Road, Pune 411045', '+91-9654321098', 2, 4, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'delivered', DATE_SUB(NOW(), INTERVAL 23 HOUR))",
            "INSERT INTO shipments (sender_name, sender_address, sender_phone, recipient_name, recipient_address, recipient_phone, origin_location_id, destination_location_id, created_at, estimated_delivery, current_status, actual_delivery) VALUES ('Saswat Das', '23, Anna Nagar East, Chennai 600102', '+91-9456789012', 'Piyush Kumar', 'Flat 501, New Town Action Area 1, Kolkata 700156', '+91-9543210987', 6, 7, NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY), 'in_transit', NULL)",
            "INSERT INTO shipments (sender_name, sender_address, sender_phone, recipient_name, recipient_address, recipient_phone, origin_location_id, destination_location_id, created_at, estimated_delivery, current_status, actual_delivery) VALUES ('Priya Sharma', '78, CG Road, Navrangpura, Ahmedabad 380009', '+91-9567890123', 'Harshit Tiwari', 'Wakad Road, Hinjewadi Phase 2, Pune 411057', '+91-9432109876', 8, 4, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 'delivered', DATE_SUB(NOW(), INTERVAL 25 HOUR))",
            "INSERT INTO shipments (sender_name, sender_address, sender_phone, recipient_name, recipient_address, recipient_phone, origin_location_id, destination_location_id, created_at, estimated_delivery, current_status, actual_delivery) VALUES ('Viraj Bhanage', 'T-4, Koramangala 4th Block, Bangalore 560034', '+91-9678901234', 'Rohan Jangam', 'Satellite Road, Prahlad Nagar, Ahmedabad 380015', '+91-9321098765', 3, 8, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 'delayed', NULL)",
            "INSERT INTO shipments (sender_name, sender_address, sender_phone, recipient_name, recipient_address, recipient_phone, origin_location_id, destination_location_id, created_at, estimated_delivery, current_status, actual_delivery) VALUES ('Pratham Jain', '456, Marine Drive, Mumbai 400020', '+91-9789012345', 'Ankit Kumar', 'Jubilee Hills, Road No 36, Hyderabad 500033', '+91-9210987654', 1, 5, NOW(), DATE_ADD(NOW(), INTERVAL 2 DAY), 'pending', NULL)",
            "INSERT INTO shipments (sender_name, sender_address, sender_phone, recipient_name, recipient_address, recipient_phone, origin_location_id, destination_location_id, created_at, estimated_delivery, current_status, actual_delivery) VALUES ('Tejas Varshney', '88, Park Street, Kolkata 700016', '+91-9890123456', 'Saswat Das', 'Anna Salai, Thousand Lights, Chennai 600002', '+91-9109876543', 7, 6, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'delivered', DATE_SUB(NOW(), INTERVAL 22 HOUR))"
        };
        
        for (String sql : shipmentSQL) {
            directStmt.executeUpdate(sql);
        }
        
        System.out.println("✓ Inserted 8 shipments");
        directStmt.close();
    }
    
    private static void insertSamplePackages(Connection conn) throws SQLException {
        String packageQuery = "INSERT INTO packages (shipment_id, weight, content_description, package_type) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(packageQuery);
        
        Object[][] packages = {
            {1, 1.5, "Electronics - OnePlus Mobile Phone", "Fragile"},
            {1, 0.2, "Mobile Accessories - Screen Guard & Case", "Standard"},
            {2, 3.2, "Ayurvedic Medicine - Chyawanprash & Health Supplements", "Standard"},
            {2, 0.8, "Organic Spices - Turmeric & Red Chili Powder", "Standard"},
            {3, 4.5, "Books - NCERT Textbooks for Class 12", "Standard"},
            {3, 2.1, "Stationery - Notebooks & Pens", "Standard"},
            {4, 0.9, "Handicrafts - Wooden Decorative Items", "Fragile"},
            {5, 2.8, "Clothing - Cotton Kurtas and Sarees", "Standard"},
            {5, 1.2, "Jewelry - Artificial Earrings Set", "Fragile"},
            {6, 5.5, "Food Items - Gujarati Snacks (Dhokla Mix, Khakhra)", "Perishable"},
            {6, 1.8, "Tea - Assam Tea Leaves Premium Quality", "Standard"},
            {7, 3.5, "Electronics - Bluetooth Headphones", "Fragile"},
            {7, 0.5, "Gift Items - Decorative Photo Frame", "Standard"},
            {8, 2.3, "Books - Bengali Literature Collection", "Standard"},
            {8, 1.1, "Traditional Items - Handwoven Silk Scarf", "Delicate"}
        };
        
        for (Object[] pkg : packages) {
            stmt.setInt(1, (Integer) pkg[0]);
            stmt.setDouble(2, (Double) pkg[1]);
            stmt.setString(3, (String) pkg[2]);
            stmt.setString(4, (String) pkg[3]);
            stmt.executeUpdate();
        }
        
        System.out.println("✓ Inserted 15 packages");
        stmt.close();
    }
    
    private static void insertSampleStatusLogs(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        String[] statusLogs = {
            "INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES (1, 'pending', 1, 'System', 'Shipment created at Mumbai Central Hub')",
            "INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES (1, 'in_transit', 1, 'Rajesh Kumar Singh', 'Package picked up from Mumbai Central Hub')",
            "INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES (1, 'in_transit', 2, 'Rajesh Kumar Singh', 'Package arrived at Delhi North Hub')",
            "INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES (2, 'pending', 5, 'System', 'Shipment created at Hyderabad HITEC Hub')",
            "INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES (3, 'pending', 2, 'System', 'Shipment created at Delhi North Hub')",
            "INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES (3, 'in_transit', 2, 'Sunil Yadav', 'Out for delivery from Delhi North Hub')",
            "INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES (3, 'in_transit', 4, 'Sunil Yadav', 'Package arrived at Pune Delivery Center')",
            "INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES (3, 'delivered', 4, 'Sunil Yadav', 'Package delivered successfully')",
            "INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES (4, 'pending', 6, 'System', 'Shipment created at Chennai Express Point')",
            "INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES (4, 'in_transit', 6, 'Vinod Gupta', 'Package picked up from Chennai')",
            "INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES (5, 'pending', 8, 'System', 'Shipment created at Ahmedabad Commercial Hub')",
            "INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES (5, 'delivered', 4, 'Ramesh Agarwal', 'Package delivered to recipient')"
        };
        
        for (String sql : statusLogs) {
            stmt.executeUpdate(sql);
        }
        
        System.out.println("✓ Inserted status logs");
        stmt.close();
    }
    
    private static void insertSampleDriverAssignments(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        String[] assignments = {
            "INSERT INTO driver_assignments (driver_id, shipment_id, estimated_delivery, status) VALUES (1, 1, DATE_ADD(NOW(), INTERVAL 2 DAY), 'active')",
            "INSERT INTO driver_assignments (driver_id, shipment_id, estimated_delivery, status) VALUES (5, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), 'active')",
            "INSERT INTO driver_assignments (driver_id, shipment_id, estimated_delivery, status) VALUES (4, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), 'completed')",
            "INSERT INTO driver_assignments (driver_id, shipment_id, estimated_delivery, status) VALUES (6, 4, DATE_ADD(NOW(), INTERVAL 2 DAY), 'active')",
            "INSERT INTO driver_assignments (driver_id, shipment_id, estimated_delivery, status) VALUES (8, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), 'completed')",
            "INSERT INTO driver_assignments (driver_id, shipment_id, estimated_delivery, status) VALUES (3, 6, DATE_SUB(NOW(), INTERVAL 3 DAY), 'active')",
            "INSERT INTO driver_assignments (driver_id, shipment_id, estimated_delivery, status) VALUES (2, 7, DATE_ADD(NOW(), INTERVAL 2 DAY), 'active')",
            "INSERT INTO driver_assignments (driver_id, shipment_id, estimated_delivery, status) VALUES (7, 8, DATE_SUB(NOW(), INTERVAL 1 DAY), 'completed')"
        };
        
        for (String sql : assignments) {
            stmt.executeUpdate(sql);
        }
        
        System.out.println("✓ Inserted driver assignments");
        stmt.close();
    }
    
    // Diagnostic function to check current shipments in database
    public static void showAllShipments() {
        String query = "SELECT shipment_id, sender_name, recipient_name, current_status FROM shipments ORDER BY shipment_id";
        
        try (Connection conn = create.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            System.out.println("\n=== Current Shipments in Database ===");
            System.out.printf("%-5s %-20s %-20s %-15s%n", "ID", "Sender", "Recipient", "Status");
            System.out.println("=".repeat(65));
            
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-5d %-20s %-20s %-15s%n",
                    rs.getInt("shipment_id"),
                    rs.getString("sender_name"),
                    rs.getString("recipient_name"),
                    rs.getString("current_status"));
            }
            
            if (!found) {
                System.out.println("No shipments found in database.");
                System.out.println("Please run 'Insert Sample Data' first.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
