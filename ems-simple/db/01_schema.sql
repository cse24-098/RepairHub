-- Creates all tables. Run it in MySQL Workbench after 00_setup_user.sql.
CREATE DATABASE IF NOT EXISTS ems_db;
USE ems_db;

CREATE TABLE schools (
    school_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(150) NOT NULL UNIQUE,
    address       VARCHAR(255),
    contact_email VARCHAR(150),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE users (
    user_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id     BIGINT NOT NULL,
    full_name     VARCHAR(120) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(64)  NOT NULL,
    role          ENUM('TEACHER','TECHNICIAN','SCHOOL_ADMIN') NOT NULL,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (school_id) REFERENCES schools(school_id)
);

CREATE TABLE categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE locations (
    location_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id     BIGINT NOT NULL,
    building      VARCHAR(100) NOT NULL,
    room          VARCHAR(50)  NOT NULL,
    location_type ENUM('CLASSROOM','LABORATORY','OFFICE','STORAGE','OTHER') NOT NULL DEFAULT 'CLASSROOM',
    FOREIGN KEY (school_id) REFERENCES schools(school_id)
);

CREATE TABLE equipment (
    equipment_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id       BIGINT NOT NULL,
    category_id     BIGINT NOT NULL,
    location_id     BIGINT NULL,
    asset_tag       VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    serial_number   VARCHAR(100),
    condition_state ENUM('NEW','GOOD','FAIR','POOR') NOT NULL DEFAULT 'GOOD',
    status          ENUM('AVAILABLE','IN_USE','DAMAGED','UNDER_MAINTENANCE','RETIRED') NOT NULL DEFAULT 'AVAILABLE',
    qr_code_value   VARCHAR(100) NOT NULL UNIQUE,
    FOREIGN KEY (school_id)   REFERENCES schools(school_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (location_id) REFERENCES locations(location_id)
);

CREATE TABLE maintenance_reports (
    report_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_no     VARCHAR(30) NOT NULL UNIQUE,
    school_id        BIGINT NOT NULL,
    teacher_id       BIGINT NOT NULL,
    equipment_id     BIGINT NOT NULL,
    location_id      BIGINT NULL,
    issue_type       ENUM('HARDWARE_FAULT','SOFTWARE','ELECTRICAL','DAMAGE','OTHER') NOT NULL,
    description      TEXT NOT NULL,
    priority         ENUM('LOW','MEDIUM','HIGH','URGENT') NOT NULL DEFAULT 'MEDIUM',
    status           ENUM('PENDING','IN_PROGRESS','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    location_details VARCHAR(255),
    created_at       DATETIME NOT NULL,
    resolved_at      DATETIME NULL,
    FOREIGN KEY (school_id)    REFERENCES schools(school_id),
    FOREIGN KEY (teacher_id)   REFERENCES users(user_id),
    FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id),
    FOREIGN KEY (location_id)  REFERENCES locations(location_id)
);

CREATE TABLE report_status_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id  BIGINT NOT NULL,
    changed_by BIGINT NOT NULL,
    old_status ENUM('PENDING','IN_PROGRESS','RESOLVED','REJECTED') NULL,
    new_status ENUM('PENDING','IN_PROGRESS','RESOLVED','REJECTED') NOT NULL,
    comment    TEXT NOT NULL,
    changed_at DATETIME NOT NULL,
    FOREIGN KEY (report_id)  REFERENCES maintenance_reports(report_id),
    FOREIGN KEY (changed_by) REFERENCES users(user_id)
);

-- ===== Tables below are NOT used by the code yet: they are TODOs for the team =====

CREATE TABLE report_attachments (          -- RQ02 photos / videos
    attachment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id     BIGINT NOT NULL,
    file_url      VARCHAR(500) NOT NULL,
    file_type     ENUM('IMAGE','VIDEO') NOT NULL,
    size_kb       INT,
    FOREIGN KEY (report_id) REFERENCES maintenance_reports(report_id)
);

CREATE TABLE maintenance_records (         -- work done on a report
    record_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id        BIGINT NOT NULL,
    performed_by     BIGINT NOT NULL,
    maintenance_type ENUM('REPAIR','INSPECTION','REPLACEMENT','CLEANING','OTHER') NOT NULL,
    notes            TEXT,
    started_at       DATETIME NOT NULL,
    completed_at     DATETIME NULL,
    FOREIGN KEY (report_id)    REFERENCES maintenance_reports(report_id),
    FOREIGN KEY (performed_by) REFERENCES users(user_id)
);

CREATE TABLE borrowings (                  -- RQ13 borrowing and returning
    borrowing_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id    BIGINT NOT NULL,
    borrower_id     BIGINT NOT NULL,
    issued_by       BIGINT NOT NULL,
    borrowed_at     DATETIME NOT NULL,
    expected_return DATE NOT NULL,
    returned_at     DATETIME NULL,
    condition_out   ENUM('NEW','GOOD','FAIR','POOR') NOT NULL,
    condition_in    ENUM('NEW','GOOD','FAIR','POOR') NULL,
    FOREIGN KEY (equipment_id) REFERENCES equipment(equipment_id),
    FOREIGN KEY (borrower_id)  REFERENCES users(user_id),
    FOREIGN KEY (issued_by)    REFERENCES users(user_id)
);

CREATE TABLE equipment_transfers (         -- RQ15 moving equipment between rooms
    transfer_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id     BIGINT NOT NULL,
    from_location_id BIGINT NULL,
    to_location_id   BIGINT NOT NULL,
    moved_by         BIGINT NOT NULL,
    moved_at         DATETIME NOT NULL,
    reason           VARCHAR(255),
    FOREIGN KEY (equipment_id)     REFERENCES equipment(equipment_id),
    FOREIGN KEY (from_location_id) REFERENCES locations(location_id),
    FOREIGN KEY (to_location_id)   REFERENCES locations(location_id),
    FOREIGN KEY (moved_by)         REFERENCES users(user_id)
);

CREATE TABLE notifications (               -- RQ07 notifications
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    report_id       BIGINT NULL,
    message         VARCHAR(500) NOT NULL,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      DATETIME NOT NULL,
    FOREIGN KEY (user_id)   REFERENCES users(user_id),
    FOREIGN KEY (report_id) REFERENCES maintenance_reports(report_id)
);
