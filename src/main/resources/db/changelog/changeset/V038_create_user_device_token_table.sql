CREATE TABLE IF NOT EXISTS user_schema.user_device_token (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    user_language VARCHAR(50) NOT NULL,
    fcm_token VARCHAR(255) NOT NULL,
    device_type VARCHAR(50),
    app_version VARCHAR(50),
    device_info TEXT,
    is_active BOOLEAN DEFAULT true,
    last_seen TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
)