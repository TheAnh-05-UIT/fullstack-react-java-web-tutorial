-- Existing installations created by Hibernate used VARCHAR(255), which is too
-- small for AUTH-1 refresh JWTs. A new/empty database has no users table yet,
-- so this migration deliberately becomes a no-op until Hibernate creates it
-- with the matching entity mapping.
SET @expand_refresh_token_sql = (
    SELECT IF(
        EXISTS(
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'users'
              AND column_name = 'refresh_token'
              AND character_maximum_length < 2048
        ),
        'ALTER TABLE users MODIFY COLUMN refresh_token VARCHAR(2048) NULL',
        'SELECT 1'
    )
);

PREPARE expand_refresh_token_statement FROM @expand_refresh_token_sql;
EXECUTE expand_refresh_token_statement;
DEALLOCATE PREPARE expand_refresh_token_statement;
