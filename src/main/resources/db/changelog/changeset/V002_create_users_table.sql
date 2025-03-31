CREATE TABLE IF NOT EXISTS user_schema.users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50) NOT NULL,
    work_place VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    position VARCHAR(128) NOT NULL,
    pinfl VARCHAR(16) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    role VARCHAR(50) DEFAULT NULL,
    status VARCHAR(50) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login TIMESTAMP DEFAULT NULL
);