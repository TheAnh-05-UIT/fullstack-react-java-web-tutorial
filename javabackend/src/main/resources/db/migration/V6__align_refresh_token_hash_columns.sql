ALTER TABLE refresh_token_sessions
    MODIFY COLUMN token_hash VARCHAR(64) NOT NULL,
    MODIFY COLUMN previous_token_hash VARCHAR(64) NULL;
