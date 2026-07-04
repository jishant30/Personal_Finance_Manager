/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  Jisha
 * Created: 24-Jun-2026
 */






CREATE TABLE IF NOT EXISTS users(
	id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    full_name VARCHAR(100) DEFAULT '',
    avatar BLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
	id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    type ENUM('INCOME', 'EXPENSE') NOT NULL
);

CREATE TABLE IF NOT EXISTS transaction (
	id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    note VARCHAR(255) DEFAULT '',
    receipt BLOB,
    txn_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

INSERT INTO categories (name, type) VALUES
	('Salary', 'INCOME'),
    ('Freelance', 'INCOME'),
    ('Business', 'INCOME'),
    ('Other Income', 'INCOME'),
    ('Food', 'EXPENSE'),
    ('Transport', 'EXPENSE'),
    ('Shopping', 'EXPENSE'),
    ('Utilities', 'EXPENSE'),
    ('Health', 'EXPENSE'),
    ('Entertainment', 'EXPENSE'),
    ('Education', 'EXPENSE'),
    ('Rent', 'EXPENSE'),
    ('Other', 'EXPENSE');
