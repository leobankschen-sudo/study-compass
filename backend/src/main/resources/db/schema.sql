CREATE DATABASE IF NOT EXISTS study_compass
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE study_compass;

CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(30) NOT NULL UNIQUE,
    nickname VARCHAR(40) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NULL COMMENT '实验四加入登录后写入BCrypt密文',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS learning_goal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(80) NOT NULL,
    description VARCHAR(1000),
    start_date DATE NOT NULL,
    target_date DATE NOT NULL,
    weekly_hours INT,
    current_level VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES app_user(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS learning_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goal_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    due_date DATE,
    estimated_minutes INT,
    sort_order INT NOT NULL DEFAULT 0,
    ai_generated BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_goal FOREIGN KEY (goal_id) REFERENCES learning_goal(id),
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    INDEX idx_task_user_status (user_id, status),
    INDEX idx_task_due_date (due_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS study_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    goal_id BIGINT NOT NULL,
    task_id BIGINT,
    study_date DATE NOT NULL,
    duration_minutes INT NOT NULL,
    content VARCHAR(500) NOT NULL,
    reflection VARCHAR(1000),
    focus_score INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_record_goal FOREIGN KEY (goal_id) REFERENCES learning_goal(id),
    CONSTRAINT fk_record_task FOREIGN KEY (task_id) REFERENCES learning_task(id),
    INDEX idx_record_user_date (user_id, study_date),
    INDEX idx_record_goal (goal_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_preference (
    user_id BIGINT PRIMARY KEY,
    weekly_target_hours INT NOT NULL DEFAULT 10,
    preferred_session_minutes INT NOT NULL DEFAULT 45,
    preferred_study_period VARCHAR(20) NOT NULL DEFAULT 'EVENING',
    ai_personalization BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_preference_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ai_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    goal_id BIGINT,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(120) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    content_json LONGTEXT NOT NULL,
    model VARCHAR(60) NOT NULL,
    record_count INT NOT NULL DEFAULT 0,
    total_minutes INT NOT NULL DEFAULT 0,
    completed_tasks INT NOT NULL DEFAULT 0,
    average_focus INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_report_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_report_goal FOREIGN KEY (goal_id) REFERENCES learning_goal(id) ON DELETE SET NULL,
    INDEX idx_ai_report_user_type (user_id, type, created_at)
) ENGINE=InnoDB;
