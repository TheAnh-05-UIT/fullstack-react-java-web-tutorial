CREATE TABLE IF NOT EXISTS roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NULL,
    email VARCHAR(255) NULL,
    password VARCHAR(255) NULL,
    avatar VARCHAR(255) NULL,
    created_at DATETIME(6) NULL,
    create_by VARCHAR(255) NULL,
    updated_at DATETIME(6) NULL,
    update_by VARCHAR(255) NULL,
    role_id BIGINT NULL,
    PRIMARY KEY (id),
    INDEX idx_users_role (role_id),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS authors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NULL,
    avatar VARCHAR(255) NULL,
    bio VARCHAR(255) NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    is_deleted BIT(1) NOT NULL,
    user_id BIGINT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_authors_user (user_id),
    CONSTRAINT fk_authors_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categores (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NULL,
    slug VARCHAR(255) NULL,
    description VARCHAR(255) NULL,
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tutorials (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NULL,
    slug VARCHAR(255) NULL,
    description LONGTEXT NULL,
    content LONGTEXT NULL,
    cover_image VARCHAR(255) NULL,
    read_time INT NULL,
    views BIGINT NULL,
    status VARCHAR(255) NULL,
    published_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    create_by VARCHAR(255) NULL,
    updated_at DATETIME(6) NULL,
    update_by VARCHAR(255) NULL,
    is_deleted BIT(1) NOT NULL,
    category_id BIGINT NULL,
    author_id BIGINT NULL,
    PRIMARY KEY (id),
    INDEX idx_tutorials_public (status, is_deleted, created_at),
    INDEX idx_tutorials_category (category_id),
    INDEX idx_tutorials_author (author_id),
    CONSTRAINT fk_tutorials_category FOREIGN KEY (category_id) REFERENCES categores (id),
    CONSTRAINT fk_tutorials_author FOREIGN KEY (author_id) REFERENCES authors (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NULL,
    slug VARCHAR(255) NULL,
    description MEDIUMTEXT NULL,
    content MEDIUMTEXT NULL,
    cover_image VARCHAR(255) NULL,
    difficulty VARCHAR(255) NULL,
    github_url VARCHAR(255) NULL,
    demo_url VARCHAR(255) NULL,
    views BIGINT NULL,
    status VARCHAR(255) NULL,
    published_at DATETIME(6) NULL,
    created_at DATETIME(6) NULL,
    create_by VARCHAR(255) NULL,
    updated_at DATETIME(6) NULL,
    update_by VARCHAR(255) NULL,
    is_deleted BIT(1) NOT NULL,
    author_id BIGINT NULL,
    category_id BIGINT NULL,
    PRIMARY KEY (id),
    INDEX idx_projects_public (status, is_deleted, created_at),
    INDEX idx_projects_author (author_id),
    INDEX idx_projects_category (category_id),
    CONSTRAINT fk_projects_author FOREIGN KEY (author_id) REFERENCES authors (id),
    CONSTRAINT fk_projects_category FOREIGN KEY (category_id) REFERENCES categores (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS project_tags (
    project_id BIGINT NOT NULL,
    tags VARCHAR(255) NULL,
    INDEX idx_project_tags_project (project_id),
    CONSTRAINT fk_project_tags_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roadmaps (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NULL,
    slug VARCHAR(255) NULL,
    description MEDIUMTEXT NULL,
    content LONGTEXT NULL,
    cover_image VARCHAR(255) NULL,
    difficulty TINYINT NULL,
    icon VARCHAR(255) NULL,
    color VARCHAR(255) NULL,
    created_at DATETIME(6) NULL,
    create_by VARCHAR(255) NULL,
    updated_at DATETIME(6) NULL,
    update_by VARCHAR(255) NULL,
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_roadmaps_visibility (is_deleted),
    INDEX idx_roadmaps_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roadmapsteps (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NULL,
    description VARCHAR(255) NULL,
    step_order INT NULL,
    content_type VARCHAR(255) NULL,
    roadmap_id BIGINT NULL,
    tutorial_id BIGINT NULL,
    project_id BIGINT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_roadmapsteps_roadmap (roadmap_id),
    INDEX idx_roadmapsteps_tutorial (tutorial_id),
    INDEX idx_roadmapsteps_project (project_id),
    CONSTRAINT fk_roadmapsteps_roadmap FOREIGN KEY (roadmap_id) REFERENCES roadmaps (id),
    CONSTRAINT fk_roadmapsteps_tutorial FOREIGN KEY (tutorial_id) REFERENCES tutorials (id),
    CONSTRAINT fk_roadmapsteps_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS userprojectprogress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    project_id BIGINT NULL,
    status VARCHAR(255) NULL,
    started_at DATETIME(6) NULL,
    finished_at DATETIME(6) NULL,
    is_deleted BIT(1) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_userprojectprogress_user (user_id),
    INDEX idx_userprojectprogress_project (project_id),
    CONSTRAINT fk_userprojectprogress_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_userprojectprogress_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_learning_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    content_key VARCHAR(190) NOT NULL,
    status VARCHAR(50) NOT NULL,
    progress_percent INT NOT NULL,
    last_accessed_at DATETIME(6) NULL,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_learning_content (user_id, content_type, content_key),
    INDEX idx_user_progress_status (user_id, status),
    INDEX idx_user_progress_accessed (user_id, last_accessed_at),
    INDEX idx_user_progress_type (user_id, content_type),
    CONSTRAINT fk_user_learning_progress_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS devops_phases (
    id BIGINT NOT NULL AUTO_INCREMENT,
    phase_key VARCHAR(255) NOT NULL,
    title VARCHAR(255) NULL,
    name VARCHAR(255) NULL,
    tagline TEXT NULL,
    summary TEXT NULL,
    hero_snippet_title VARCHAR(255) NULL,
    hero_snippet TEXT NULL,
    icon_name VARCHAR(255) NULL,
    color_gradient VARCHAR(255) NULL,
    display_order INT NOT NULL,
    active BIT(1) NOT NULL,
    theme_json LONGTEXT NULL,
    curriculum_json LONGTEXT NULL,
    tools_json LONGTEXT NULL,
    learning_path_json LONGTEXT NULL,
    quiz_json LONGTEXT NULL,
    hands_on_labs_json LONGTEXT NULL,
    created_at DATETIME(6) NULL,
    created_by VARCHAR(255) NULL,
    updated_at DATETIME(6) NULL,
    updated_by VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_devops_phases_phase_key (phase_key),
    INDEX idx_devops_phases_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS refresh_token_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    family_id VARCHAR(36) NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    current_jti VARCHAR(36) NOT NULL,
    previous_token_hash VARCHAR(64) NULL,
    previous_jti VARCHAR(36) NULL,
    previous_consumed_at DATETIME(6) NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    revoke_reason VARCHAR(32) NULL,
    replaced_by_jti VARCHAR(36) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_token_sessions_family (family_id),
    UNIQUE KEY uk_refresh_token_sessions_hash (token_hash),
    INDEX idx_refresh_token_sessions_user (user_id),
    INDEX idx_refresh_token_sessions_expires (expires_at),
    INDEX idx_refresh_token_sessions_revoked (revoked_at),
    CONSTRAINT fk_refresh_token_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
