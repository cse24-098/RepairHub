-- Demo data. Every demo account has the password: password123
-- SHA2(..., 256) here must match PasswordUtil.sha256() in the Java code.
USE ems_db;

INSERT INTO schools (name, address, contact_email)
VALUES ('Demo Secondary School', '1 Example Road', 'office@demo-school.test');

INSERT INTO users (school_id, full_name, email, password_hash, role) VALUES
 (1, 'Tina Teacher',    'teacher@school.test', SHA2('password123', 256), 'TEACHER'),
 (1, 'Tom Technician',  'tech@school.test',    SHA2('password123', 256), 'TECHNICIAN'),
 (1, 'Ada Admin',       'admin@school.test',   SHA2('password123', 256), 'SCHOOL_ADMIN');

INSERT INTO categories (name) VALUES
 ('Computers'), ('Projectors'), ('Printers'), ('Laboratory'), ('Networking'), ('Furniture'), ('Other');

INSERT INTO locations (school_id, building, room, location_type) VALUES
 (1, 'Science Block', 'Lab 2',    'LABORATORY'),
 (1, 'Admin Block',   'Room 101', 'CLASSROOM');

INSERT INTO equipment (school_id, category_id, location_id, asset_tag, name, condition_state, status, qr_code_value) VALUES
 (1, 1, 1, 'ASSET-0001', 'Dell Desktop PC',     'GOOD', 'AVAILABLE', 'ASSET-0001'),
 (1, 2, 2, 'ASSET-0002', 'Epson Projector',     'GOOD', 'AVAILABLE', 'ASSET-0002'),
 (1, 3, 2, 'ASSET-0003', 'HP LaserJet Printer', 'GOOD', 'AVAILABLE', 'ASSET-0003');
