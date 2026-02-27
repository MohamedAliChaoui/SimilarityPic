CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    bio VARCHAR(1000),
    phone_number VARCHAR(255),
    birth_date DATE,
    profile_picture BYTEA,
    profile_picture_format VARCHAR(255)
); 