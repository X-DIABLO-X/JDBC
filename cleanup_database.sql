-- Quick Database Reset Script
-- Use this if you want to quickly reset the database without running the full setup

USE courier_service;

-- Drop all tables (in reverse order due to foreign key constraints)
DROP TABLE IF EXISTS driver_assignments;
DROP TABLE IF EXISTS status_logs;
DROP TABLE IF EXISTS packages;
DROP TABLE IF EXISTS shipments;
DROP TABLE IF EXISTS drivers;
DROP TABLE IF EXISTS locations;

-- Optionally drop and recreate the entire database
-- DROP DATABASE IF EXISTS courier_service;
-- CREATE DATABASE courier_service;

SELECT 'All tables dropped successfully! Run setup_database.sql to recreate.' as Status;
