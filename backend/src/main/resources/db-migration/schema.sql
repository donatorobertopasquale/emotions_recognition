-- Database schema for Emotion Recognition application

-- Drop tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS user_reactions;
DROP TABLE IF EXISTS blacklist_tokens;
DROP TABLE IF EXISTS users;

-- Users table
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nickname NVARCHAR(255),
    email NVARCHAR(255),
    age INT,
    gender NVARCHAR(50),
    nationality NVARCHAR(100),
    google_id NVARCHAR(255) UNIQUE NOT NULL,
    email_verified BIT DEFAULT 1
);

-- User reactions table
CREATE TABLE user_reactions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT,
    image NVARCHAR(255),
    image_description NVARCHAR(MAX),
    image_reaction NVARCHAR(100),
    ai_comment NVARCHAR(MAX)
);

-- Blacklisted JWT tokens table
CREATE TABLE blacklist_tokens (
    jwt NVARCHAR(500) PRIMARY KEY,
    expiration_date DATE
);

-- Create indices for performance
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_google_id ON users(google_id);
CREATE INDEX idx_user_reactions_user_id ON user_reactions(user_id);
CREATE INDEX idx_blacklist_tokens_expiration ON blacklist_tokens(expiration_date);

-- Add foreign key constraints after table creation
ALTER TABLE user_reactions 
ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id);
