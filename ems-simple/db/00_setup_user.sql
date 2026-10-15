-- Run this FIRST, while connected to MySQL as root (or another admin user).
-- It creates the database and the account the Java application uses
-- (the same name and password as in src/main/resources/application.properties).
CREATE DATABASE IF NOT EXISTS ems_db;
CREATE USER IF NOT EXISTS 'ems_user'@'localhost' IDENTIFIED BY 'ems_pass';
GRANT ALL PRIVILEGES ON ems_db.* TO 'ems_user'@'localhost';
FLUSH PRIVILEGES;
