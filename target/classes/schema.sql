CREATE TABLE IF NOT EXISTS users (
  username VARCHAR(32) PRIMARY KEY,
  password_hash VARCHAR(512) NOT NULL,
  password_salt VARCHAR(512) NOT NULL
);
