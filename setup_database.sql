-- Courier Service Database Schema
-- Run this script manually in MySQL if the Java application fails to create the database

DROP DATABASE IF EXISTS courier_service;
CREATE DATABASE courier_service;
USE courier_service;

-- Drop tables if they exist (in reverse order due to foreign key constraints)
DROP TABLE IF EXISTS driver_assignments;
DROP TABLE IF EXISTS status_logs;
DROP TABLE IF EXISTS packages;
DROP TABLE IF EXISTS shipments;
DROP TABLE IF EXISTS drivers;
DROP TABLE IF EXISTS locations;

-- Create Locations table
CREATE TABLE locations (
    location_id INT PRIMARY KEY AUTO_INCREMENT,
    location_name VARCHAR(255) NOT NULL,
    location_type ENUM('hub', 'sub_hub', 'delivery_point') NOT NULL,
    parent_location_id INT,
    address TEXT,
    FOREIGN KEY (parent_location_id) REFERENCES locations(location_id)
);

-- Create Drivers table
CREATE TABLE drivers (
    driver_id INT PRIMARY KEY AUTO_INCREMENT,
    driver_name VARCHAR(255) NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    max_active_shipments INT DEFAULT 5,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Shipments table
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
);

-- Create Packages table
CREATE TABLE packages (
    package_id INT PRIMARY KEY AUTO_INCREMENT,
    shipment_id INT NOT NULL,
    weight DECIMAL(10,2) NOT NULL,
    content_description TEXT,
    package_type VARCHAR(100),
    FOREIGN KEY (shipment_id) REFERENCES shipments(shipment_id) ON DELETE CASCADE
);

-- Create Status_Logs table
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
);

-- Create Driver_Assignments table
CREATE TABLE driver_assignments (
    assignment_id INT PRIMARY KEY AUTO_INCREMENT,
    driver_id INT NOT NULL,
    shipment_id INT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estimated_delivery DATETIME,
    status ENUM('active', 'completed', 'cancelled') DEFAULT 'active',
    FOREIGN KEY (driver_id) REFERENCES drivers(driver_id),
    FOREIGN KEY (shipment_id) REFERENCES shipments(shipment_id)
);

-- Create indexes for better performance
CREATE INDEX idx_shipment_status ON shipments(current_status);
CREATE INDEX idx_shipment_created ON shipments(created_at);
CREATE INDEX idx_status_logs_shipment ON status_logs(shipment_id);
CREATE INDEX idx_driver_assignments_driver ON driver_assignments(driver_id);
CREATE INDEX idx_driver_assignments_status ON driver_assignments(status);

-- Reset AUTO_INCREMENT to ensure consistent IDs
ALTER TABLE locations AUTO_INCREMENT = 1;
ALTER TABLE drivers AUTO_INCREMENT = 1;
ALTER TABLE shipments AUTO_INCREMENT = 1;
ALTER TABLE packages AUTO_INCREMENT = 1;
ALTER TABLE status_logs AUTO_INCREMENT = 1;
ALTER TABLE driver_assignments AUTO_INCREMENT = 1;

-- Insert sample data with Indian locations and names
INSERT INTO locations (location_name, location_type, address) VALUES 
('Mumbai Central Hub', 'hub', 'Andheri East, Mumbai, Maharashtra 400069'),
('Delhi North Hub', 'sub_hub', 'Connaught Place, New Delhi, Delhi 110001'),
('Bangalore Tech Hub', 'sub_hub', 'Koramangala, Bangalore, Karnataka 560034'),
('Pune Delivery Center', 'delivery_point', 'Hinjewadi Phase 1, Pune, Maharashtra 411057'),
('Hyderabad HITEC Hub', 'sub_hub', 'HITEC City, Hyderabad, Telangana 500081'),
('Chennai Express Point', 'delivery_point', 'T. Nagar, Chennai, Tamil Nadu 600017'),
('Kolkata East Hub', 'sub_hub', 'Salt Lake City, Kolkata, West Bengal 700064'),
('Ahmedabad Commercial Hub', 'delivery_point', 'Satellite, Ahmedabad, Gujarat 380015');

INSERT INTO drivers (driver_name, license_number, contact_phone, contact_email) VALUES
('Rajesh Kumar Singh', 'MH12DL2024001', '+91-9876543210', 'rajesh.singh@courier.in'),
('Pradeep Sharma', 'MH14DL2024002', '+91-9876543211', 'pradeep.sharma@courier.in'),
('Manoj Verma', 'KA05DL2024003', '+91-9876543212', 'manoj.verma@courier.in'),
('Sunil Yadav', 'DL08DL2024004', '+91-9876543213', 'sunil.yadav@courier.in'),
('Ashok Pandey', 'TG09DL2024005', '+91-9876543214', 'ashok.pandey@courier.in'),
('Vinod Gupta', 'TN33DL2024006', '+91-9876543215', 'vinod.gupta@courier.in'),
('Deepak Mishra', 'WB03DL2024007', '+91-9876543216', 'deepak.mishra@courier.in'),
('Ramesh Agarwal', 'GJ01DL2024008', '+91-9876543217', 'ramesh.agarwal@courier.in');

-- Insert sample shipments for testing with Indian senders and recipients
INSERT INTO shipments (sender_name, sender_address, sender_phone, recipient_name, recipient_address, recipient_phone, origin_location_id, destination_location_id, estimated_delivery, current_status, actual_delivery) VALUES
('Harshit Tiwari', 'A-203, Oberoi Heights, Andheri West, Mumbai 400058', '+91-9123456789', 'Viraj Bhanage', 'B-45, Sector 21, Dwarka, New Delhi 110075', '+91-9876543210', 1, 2, DATE_ADD(NOW(), INTERVAL 2 DAY), 'in_transit', NULL),
('Rohan Jangam', '15-4-78, Begumpet, Hyderabad 500016', '+91-9234567890', 'Pratham Jain', '204, Brigade Gateway, Rajajinagar, Bangalore 560010', '+91-9765432109', 5, 3, DATE_ADD(NOW(), INTERVAL 1 DAY), 'pending', NULL),
('Ankit Kumar', 'C-102, Sushant Lok Phase 1, Gurgaon 122009', '+91-9345678901', 'Tejas Varshney', 'Plot 67, Baner Road, Pune 411045', '+91-9654321098', 2, 4, DATE_SUB(NOW(), INTERVAL 1 DAY), 'delivered', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('Saswat Das', '23, Anna Nagar East, Chennai 600102', '+91-9456789012', 'Piyush Kumar', 'Flat 501, New Town Action Area 1, Kolkata 700156', '+91-9543210987', 6, 7, DATE_ADD(NOW(), INTERVAL 2 DAY), 'in_transit', NULL),
('Priya Sharma', '78, CG Road, Navrangpura, Ahmedabad 380009', '+91-9567890123', 'Harshit Tiwari', 'Wakad Road, Hinjewadi Phase 2, Pune 411057', '+91-9432109876', 8, 4, DATE_SUB(NOW(), INTERVAL 2 DAY), 'delivered', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('Viraj Bhanage', 'T-4, Koramangala 4th Block, Bangalore 560034', '+91-9678901234', 'Rohan Jangam', 'Satellite Road, Prahlad Nagar, Ahmedabad 380015', '+91-9321098765', 3, 8, DATE_SUB(NOW(), INTERVAL 3 DAY), 'delayed', NULL),
('Pratham Jain', '456, Marine Drive, Mumbai 400020', '+91-9789012345', 'Ankit Kumar', 'Jubilee Hills, Road No 36, Hyderabad 500033', '+91-9210987654', 1, 5, DATE_ADD(NOW(), INTERVAL 2 DAY), 'pending', NULL),
('Tejas Varshney', '88, Park Street, Kolkata 700016', '+91-9890123456', 'Saswat Das', 'Anna Salai, Thousand Lights, Chennai 600002', '+91-9109876543', 7, 6, DATE_SUB(NOW(), INTERVAL 1 DAY), 'delivered', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
-- Additional shipments for better query demonstration
('Neha Singh', '12, Golf Course Road, Gurgaon 122001', '+91-9234567891', 'Rahul Mehta', '456, Banjara Hills, Hyderabad 500034', '+91-9345678902', 2, 5, DATE_SUB(NOW(), INTERVAL 5 DAY), 'delivered', DATE_SUB(NOW(), INTERVAL 4 DAY)),
('Kiran Patel', '78, SG Highway, Ahmedabad 380054', '+91-9456789013', 'Amit Joshi', '234, Koregaon Park, Pune 411001', '+91-9567890124', 8, 4, DATE_SUB(NOW(), INTERVAL 2 DAY), 'delivered', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('Suman Reddy', '90, Jubilee Hills, Hyderabad 500033', '+91-9678901235', 'Kavya Nair', '567, MG Road, Bangalore 560001', '+91-9789012346', 5, 3, DATE_ADD(NOW(), INTERVAL 1 DAY), 'pending', NULL),
('Ravi Kumar', '123, Anna Nagar, Chennai 600040', '+91-9890123457', 'Deepika Shah', '789, Park Street, Kolkata 700017', '+91-9901234568', 6, 7, DATE_SUB(NOW(), INTERVAL 6 DAY), 'delivered', DATE_SUB(NOW(), INTERVAL 5 DAY)),
('Pooja Gupta', '45, CP, New Delhi 110001', '+91-9012345679', 'Suresh Iyer', '321, Brigade Road, Bangalore 560025', '+91-9123456780', 2, 3, DATE_SUB(NOW(), INTERVAL 4 DAY), 'delivered', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('Manoj Singh', '67, Linking Road, Mumbai 400050', '+91-9234567801', 'Priyanka Das', '890, Salt Lake, Kolkata 700064', '+91-9345678912', 1, 7, DATE_SUB(NOW(), INTERVAL 8 DAY), 'delivered', DATE_SUB(NOW(), INTERVAL 7 DAY)),
('Anjali Sharma', '234, Sector 17, Chandigarh 160017', '+91-9456789023', 'Rajesh Nair', 'T. Nagar, Chennai 600017', '+91-9567890134', 2, 6, DATE_SUB(NOW(), INTERVAL 3 DAY), 'returned', NULL);

-- Insert sample packages with Indian products
INSERT INTO packages (shipment_id, weight, content_description, package_type) VALUES
(1, 1.5, 'Electronics - OnePlus Mobile Phone', 'Fragile'),
(1, 0.2, 'Mobile Accessories - Screen Guard & Case', 'Standard'),
(2, 3.2, 'Ayurvedic Medicine - Chyawanprash & Health Supplements', 'Standard'),
(2, 0.8, 'Organic Spices - Turmeric & Red Chili Powder', 'Standard'),
(3, 4.5, 'Books - NCERT Textbooks for Class 12', 'Standard'),
(3, 2.1, 'Stationery - Notebooks & Pens', 'Standard'),
(4, 0.9, 'Handicrafts - Wooden Decorative Items', 'Fragile'),
(5, 2.8, 'Clothing - Cotton Kurtas and Sarees', 'Standard'),
(5, 1.2, 'Jewelry - Artificial Earrings Set', 'Fragile'),
(6, 5.5, 'Food Items - Gujarati Snacks (Dhokla Mix, Khakhra)', 'Perishable'),
(6, 1.8, 'Tea - Assam Tea Leaves Premium Quality', 'Standard'),
(7, 3.5, 'Electronics - Bluetooth Headphones', 'Fragile'),
(7, 0.5, 'Gift Items - Decorative Photo Frame', 'Standard'),
(8, 2.3, 'Books - Bengali Literature Collection', 'Standard'),
(8, 1.1, 'Traditional Items - Handwoven Silk Scarf', 'Delicate'),
-- Additional packages for new shipments
(9, 2.0, 'Electronics - Tablet Computer', 'Fragile'),
(9, 0.3, 'Accessories - Tablet Cover', 'Standard'),
(10, 1.8, 'Cosmetics - Skincare Products', 'Standard'),
(11, 3.0, 'Books - Engineering Textbooks', 'Standard'),
(11, 1.5, 'Electronics - Calculator', 'Standard'),
(12, 4.2, 'Traditional Items - Brass Handicrafts', 'Fragile'),
(13, 2.5, 'Clothing - Designer Sarees', 'Standard'),
(14, 1.0, 'Documents - Legal Papers', 'Standard'),
(14, 0.5, 'Stationery - Pens & Files', 'Standard'),
(15, 0.8, 'Medicine - Prescription Drugs', 'Standard');

-- Insert sample status logs with Indian agent names
INSERT INTO status_logs (shipment_id, status_update, location_id, agent_name, notes) VALUES
-- Shipment 1: Harshit to Viraj (in_transit)
(1, 'pending', 1, 'System', 'Shipment created at Mumbai Central Hub'),
(1, 'in_transit', 1, 'Rajesh Kumar Singh', 'Package picked up from Mumbai Central Hub'),
(1, 'in_transit', 2, 'Rajesh Kumar Singh', 'Package arrived at Delhi North Hub'),
-- Shipment 2: Rohan to Pratham (pending)
(2, 'pending', 5, 'System', 'Shipment created at Hyderabad HITEC Hub'),
-- Shipment 3: Ankit to Tejas (delivered)
(3, 'pending', 2, 'System', 'Shipment created at Delhi North Hub'),
(3, 'in_transit', 2, 'Sunil Yadav', 'Out for delivery from Delhi North Hub'),
(3, 'in_transit', 4, 'Sunil Yadav', 'Package arrived at Pune Delivery Center'),
(3, 'delivered', 4, 'Sunil Yadav', 'Package delivered successfully'),
-- Shipment 4: Saswat to Piyush (in_transit)
(4, 'pending', 6, 'System', 'Shipment created at Chennai Express Point'),
(4, 'in_transit', 6, 'Vinod Gupta', 'Package picked up from Chennai'),
(4, 'in_transit', 7, 'Vinod Gupta', 'Package in transit to Kolkata'),
-- Shipment 5: Priya to Harshit (delivered)
(5, 'pending', 8, 'System', 'Shipment created at Ahmedabad Commercial Hub'),
(5, 'in_transit', 8, 'Ramesh Agarwal', 'Package collected from Ahmedabad'),
(5, 'in_transit', 4, 'Ramesh Agarwal', 'Package reached Pune Delivery Center'),
(5, 'delivered', 4, 'Ramesh Agarwal', 'Package delivered to recipient'),
-- Shipment 6: Viraj to Rohan (delayed)
(6, 'pending', 3, 'System', 'Shipment created at Bangalore Tech Hub'),
(6, 'in_transit', 3, 'Manoj Verma', 'Package picked up from Bangalore'),
(6, 'in_transit', 1, 'Manoj Verma', 'Package reached Mumbai Central Hub - delayed due to traffic'),
-- Shipment 7: Pratham to Ankit (pending)
(7, 'pending', 1, 'System', 'Shipment created at Mumbai Central Hub'),
-- Shipment 8: Tejas to Saswat (delivered)
(8, 'pending', 7, 'System', 'Shipment created at Kolkata East Hub'),
(8, 'in_transit', 7, 'Deepak Mishra', 'Package picked up from Kolkata'),
(8, 'in_transit', 6, 'Deepak Mishra', 'Package arrived at Chennai Express Point'),
(8, 'delivered', 6, 'Deepak Mishra', 'Package delivered successfully'),
-- Additional status logs for new shipments
(9, 'pending', 2, 'System', 'Shipment created at Delhi North Hub'),
(9, 'in_transit', 2, 'Pradeep Sharma', 'Package picked up'),
(9, 'delivered', 5, 'Pradeep Sharma', 'Package delivered to Hyderabad'),
(10, 'pending', 8, 'System', 'Shipment created at Ahmedabad Commercial Hub'),
(10, 'delivered', 4, 'Ramesh Agarwal', 'Package delivered to Pune'),
(11, 'pending', 5, 'System', 'Shipment created at Hyderabad HITEC Hub'),
(12, 'pending', 6, 'System', 'Shipment created at Chennai Express Point'),
(12, 'delivered', 7, 'Vinod Gupta', 'Package delivered to Kolkata'),
(13, 'pending', 2, 'System', 'Shipment created at Delhi North Hub'),
(13, 'delivered', 3, 'Manoj Verma', 'Package delivered to Bangalore'),
(14, 'pending', 1, 'System', 'Shipment created at Mumbai Central Hub'),
(14, 'delivered', 7, 'Rajesh Kumar Singh', 'Package delivered to Kolkata'),
(15, 'pending', 2, 'System', 'Shipment created at Delhi North Hub'),
(15, 'returned', 2, 'Sunil Yadav', 'Package returned - recipient unavailable');

-- Insert sample driver assignments with Indian drivers
INSERT INTO driver_assignments (driver_id, shipment_id, estimated_delivery, status) VALUES
(1, 1, DATE_ADD(NOW(), INTERVAL 2 DAY), 'active'),      -- Rajesh Kumar Singh assigned to Harshit to Viraj shipment
(5, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), 'active'),      -- Ashok Pandey assigned to Rohan to Pratham shipment
(4, 3, DATE_SUB(NOW(), INTERVAL 1 DAY), 'completed'),   -- Sunil Yadav assigned to Ankit to Tejas shipment (delivered)
(6, 4, DATE_ADD(NOW(), INTERVAL 2 DAY), 'active'),      -- Vinod Gupta assigned to Saswat to Piyush shipment
(8, 5, DATE_SUB(NOW(), INTERVAL 2 DAY), 'completed'),   -- Ramesh Agarwal assigned to Priya to Harshit shipment (delivered)
(3, 6, DATE_SUB(NOW(), INTERVAL 3 DAY), 'active'),      -- Manoj Verma assigned to Viraj to Rohan shipment (delayed)
(2, 7, DATE_ADD(NOW(), INTERVAL 2 DAY), 'active'),      -- Pradeep Sharma assigned to Pratham to Ankit shipment
(7, 8, DATE_SUB(NOW(), INTERVAL 1 DAY), 'completed'),   -- Deepak Mishra assigned to Tejas to Saswat shipment (delivered)
-- Additional driver assignments for new shipments
(2, 9, DATE_SUB(NOW(), INTERVAL 5 DAY), 'completed'),   -- Pradeep Sharma (completed)
(8, 10, DATE_SUB(NOW(), INTERVAL 2 DAY), 'completed'),  -- Ramesh Agarwal (completed)
(5, 11, DATE_ADD(NOW(), INTERVAL 1 DAY), 'active'),     -- Ashok Pandey (pending)
(6, 12, DATE_SUB(NOW(), INTERVAL 6 DAY), 'completed'),  -- Vinod Gupta (completed)
(3, 13, DATE_SUB(NOW(), INTERVAL 4 DAY), 'completed'),  -- Manoj Verma (completed)
(1, 14, DATE_SUB(NOW(), INTERVAL 8 DAY), 'completed'),  -- Rajesh Kumar Singh (completed)
(4, 15, DATE_SUB(NOW(), INTERVAL 3 DAY), 'completed');  -- Sunil Yadav (returned)

SELECT 'Database setup completed successfully!' as Status;

-- Verification queries to check data integrity
SELECT 'Checking shipment IDs...' as Check_Status;
SELECT shipment_id, sender_name, recipient_name FROM shipments ORDER BY shipment_id;

SELECT 'Checking packages with shipment references...' as Check_Status;
SELECT p.package_id, p.shipment_id, s.sender_name, p.content_description 
FROM packages p 
LEFT JOIN shipments s ON p.shipment_id = s.shipment_id 
ORDER BY p.shipment_id;
