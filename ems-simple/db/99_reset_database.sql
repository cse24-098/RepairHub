-- DANGER: deletes the whole ems_db database and all its data.
-- Use it when you changed 01_schema.sql and want a fresh start.
-- Afterwards run 01_schema.sql and then 02_seed.sql again.
DROP DATABASE IF EXISTS ems_db;
CREATE DATABASE ems_db;
