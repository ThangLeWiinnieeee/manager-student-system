CREATE TABLE password_recovery (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    flow_hash VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL,
    otp_hash VARCHAR(255),
    password_snapshot VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    verified_until TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_sent_at TIMESTAMP WITH TIME ZONE NOT NULL,
    window_started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    send_count INTEGER NOT NULL DEFAULT 0,
    send_request_id VARCHAR(36) NOT NULL,
    verify_request_id VARCHAR(36)
);
