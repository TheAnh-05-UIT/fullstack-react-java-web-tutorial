-- DB-2 deliberately lets ALTER/constraint creation fail when legacy rows violate
-- a business key. Operators must clean ambiguous duplicates instead of Flyway
-- silently choosing a winning row.

UPDATE tutorials SET views = 0 WHERE views IS NULL;
UPDATE projects SET views = 0 WHERE views IS NULL;

ALTER TABLE roadmaps MODIFY COLUMN difficulty VARCHAR(32) NULL;
UPDATE roadmaps
SET difficulty = CASE difficulty
    WHEN '0' THEN 'BEGINNER'
    WHEN '1' THEN 'INTERMEDIATE'
    WHEN '2' THEN 'ADVANCED'
    ELSE difficulty
END;

ALTER TABLE roles
    MODIFY COLUMN name VARCHAR(255) NOT NULL,
    ADD CONSTRAINT uk_roles_name UNIQUE (name);

ALTER TABLE users
    MODIFY COLUMN username VARCHAR(255) NOT NULL,
    MODIFY COLUMN email VARCHAR(254) NOT NULL,
    MODIFY COLUMN password VARCHAR(255) NOT NULL,
    ADD CONSTRAINT uk_users_email UNIQUE (email);

ALTER TABLE categores
    MODIFY COLUMN name VARCHAR(255) NOT NULL,
    MODIFY COLUMN slug VARCHAR(255) NOT NULL,
    ADD CONSTRAINT uk_categories_name UNIQUE (name),
    ADD CONSTRAINT uk_categories_slug UNIQUE (slug);

ALTER TABLE tutorials
    MODIFY COLUMN title VARCHAR(255) NOT NULL,
    MODIFY COLUMN slug VARCHAR(255) NOT NULL,
    MODIFY COLUMN content LONGTEXT NOT NULL,
    MODIFY COLUMN views BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT uk_tutorials_slug UNIQUE (slug),
    ADD CONSTRAINT ck_tutorials_views_nonnegative CHECK (views >= 0),
    ADD CONSTRAINT ck_tutorials_status CHECK (
        status IS NULL OR status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')
    );

ALTER TABLE projects
    MODIFY COLUMN title VARCHAR(255) NOT NULL,
    MODIFY COLUMN slug VARCHAR(255) NOT NULL,
    MODIFY COLUMN content MEDIUMTEXT NOT NULL,
    MODIFY COLUMN views BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT uk_projects_slug UNIQUE (slug),
    ADD CONSTRAINT ck_projects_views_nonnegative CHECK (views >= 0),
    ADD CONSTRAINT ck_projects_difficulty CHECK (
        difficulty IS NULL OR difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')
    ),
    ADD CONSTRAINT ck_projects_status CHECK (
        status IS NULL OR status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')
    );

ALTER TABLE roadmaps
    DROP INDEX idx_roadmaps_slug,
    MODIFY COLUMN title VARCHAR(255) NOT NULL,
    MODIFY COLUMN slug VARCHAR(255) NOT NULL,
    ADD CONSTRAINT uk_roadmaps_slug UNIQUE (slug),
    ADD CONSTRAINT ck_roadmaps_difficulty CHECK (
        difficulty IS NULL OR difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')
    );

ALTER TABLE project_tags
    MODIFY COLUMN tags VARCHAR(255) NOT NULL,
    ADD CONSTRAINT uk_project_tags_project_tag UNIQUE (project_id, tags);

ALTER TABLE user_learning_progress
    ADD CONSTRAINT ck_learning_progress_percent CHECK (progress_percent BETWEEN 0 AND 100),
    ADD CONSTRAINT ck_learning_progress_content_type CHECK (
        content_type IN ('TUTORIAL', 'PROJECT', 'ROADMAP', 'DEVOPS_PHASE')
    ),
    ADD CONSTRAINT ck_learning_progress_status CHECK (
        status IN ('IN_PROGRESS', 'COMPLETED')
    );

ALTER TABLE user_learning_progress
    DROP FOREIGN KEY fk_user_learning_progress_user;

ALTER TABLE user_learning_progress
    ADD CONSTRAINT fk_user_learning_progress_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE devops_phases
    MODIFY COLUMN title VARCHAR(255) NOT NULL,
    ADD CONSTRAINT ck_devops_phases_display_order CHECK (display_order >= 0);

ALTER TABLE refresh_token_sessions
    ADD CONSTRAINT ck_refresh_token_hash_length CHECK (CHAR_LENGTH(token_hash) = 64),
    ADD CONSTRAINT ck_previous_refresh_token_hash_length CHECK (
        previous_token_hash IS NULL OR CHAR_LENGTH(previous_token_hash) = 64
    );
