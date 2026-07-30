SET @users_table_exists = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
);

SET @create_refresh_sessions_sql = IF(
    @users_table_exists = 1,
    'CREATE TABLE IF NOT EXISTS refresh_token_sessions (
        id BIGINT NOT NULL AUTO_INCREMENT,
        user_id BIGINT NOT NULL,
        family_id VARCHAR(36) NOT NULL,
        token_hash CHAR(64) NOT NULL,
        current_jti VARCHAR(36) NOT NULL,
        expires_at DATETIME(6) NOT NULL,
        revoked_at DATETIME(6) NULL,
        revoke_reason VARCHAR(32) NULL,
        replaced_by_jti VARCHAR(36) NULL,
        created_at DATETIME(6) NOT NULL,
        updated_at DATETIME(6) NOT NULL,
        version BIGINT NOT NULL DEFAULT 0,
        PRIMARY KEY (id),
        CONSTRAINT uk_refresh_token_sessions_family UNIQUE (family_id),
        CONSTRAINT uk_refresh_token_sessions_hash UNIQUE (token_hash),
        CONSTRAINT fk_refresh_token_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        INDEX idx_refresh_token_sessions_user (user_id),
        INDEX idx_refresh_token_sessions_expires (expires_at),
        INDEX idx_refresh_token_sessions_revoked (revoked_at)
    )',
    'SELECT 1'
);
PREPARE create_refresh_sessions_statement FROM @create_refresh_sessions_sql;
EXECUTE create_refresh_sessions_statement;
DEALLOCATE PREPARE create_refresh_sessions_statement;

SET @clear_legacy_tokens_sql = IF(
    @users_table_exists = 1
    AND EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = 'users'
          AND column_name = 'refresh_token'
    ),
    'UPDATE users SET refresh_token = NULL WHERE refresh_token IS NOT NULL',
    'SELECT 1'
);
PREPARE clear_legacy_tokens_statement FROM @clear_legacy_tokens_sql;
EXECUTE clear_legacy_tokens_statement;
DEALLOCATE PREPARE clear_legacy_tokens_statement;
