CREATE TABLE IF NOT EXISTS user_service.users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    work_place VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    position VARCHAR(128) NOT NULL,
    pinfl VARCHAR(16) NOT NULL UNIQUE,
    role VARCHAR(128) DEFAULT NULL
);