SET @refresh_sessions_table_exists = (
    SELECT COUNT(*)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'refresh_token_sessions'
);

SET @add_previous_token_columns_sql = IF(
    @refresh_sessions_table_exists = 1,
    'ALTER TABLE refresh_token_sessions
        ADD COLUMN previous_token_hash CHAR(64) NULL AFTER current_jti,
        ADD COLUMN previous_jti VARCHAR(36) NULL AFTER previous_token_hash,
        ADD COLUMN previous_consumed_at DATETIME(6) NULL AFTER previous_jti',
    'SELECT 1'
);
PREPARE add_previous_token_columns_statement FROM @add_previous_token_columns_sql;
EXECUTE add_previous_token_columns_statement;
DEALLOCATE PREPARE add_previous_token_columns_statement;
