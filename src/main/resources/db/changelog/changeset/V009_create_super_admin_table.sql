CREATE TABLE IF NOT EXISTS user_schema.super_admin (
    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    language VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100) NOT NULL,
    born_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT NULL,
    last_login_at TIMESTAMP DEFAULT NULL,
    last_logout_at TIMESTAMP DEFAULT NULL
);
