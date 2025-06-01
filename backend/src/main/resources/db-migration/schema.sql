-- Database schema for Emotion Recognition application

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    nickname VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    age INTEGER,
    gender VARCHAR(50),
    nationality VARCHAR(100)
);

-- User reactions table
CREATE TABLE IF NOT EXISTS user_reactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    image VARCHAR(255),
    image_description TEXT,
    image_reaction VARCHAR(100),
    ai_comment TEXT,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Blacklisted JWT tokens table
CREATE TABLE IF NOT EXISTS blacklist_tokens (
    jwt VARCHAR(500) PRIMARY KEY,
    expiration_date DATE
);

-- Create indices for performance
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_reactions_user_id ON user_reactions(user_id);
CREATE INDEX IF NOT EXISTS idx_blacklist_tokens_expiration ON blacklist_tokens(expiration_date);
