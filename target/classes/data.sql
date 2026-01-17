-- Insert Roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_USER');

-- Insert Users
-- Password is 'password' encoded with BCrypt ($2a$10$U.M/cw./iY8... is a valid hash for 'password')
INSERT INTO users (name, username, email, password) VALUES ('Admin User', 'admin', 'admin@gmail.com', '$2a$10$U.M/cw./iY8zG/yqH/t.UO.H.y.H.y.H.y.H.y.H.y.H.y.H.y.H.y');
INSERT INTO users (name, username, email, password) VALUES ('Normal User', 'user', 'user@gmail.com', '$2a$10$U.M/cw./iY8zG/yqH/t.UO.H.y.H.y.H.y.H.y.H.y.H.y.H.y.H.y');

-- Assign Roles
INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO users_roles (user_id, role_id) VALUES (1, 2);
INSERT INTO users_roles (user_id, role_id) VALUES (2, 2);

-- Insert Categories
INSERT INTO categories (name, description) VALUES ('Technology', 'All things tech');
INSERT INTO categories (name, description) VALUES ('Lifestyle', 'Health, Travel, and more');

-- Insert Posts
INSERT INTO posts (title, description, content, created_date, updated_date, category_id, user_id) VALUES 
('Introduction to Spring Boot', 'Learn basics of Spring Boot', 'Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);

INSERT INTO posts (title, description, content, created_date, updated_date, category_id, user_id) VALUES 
('Healthy Living Tips', 'Tips for a healthy life', 'Drink water, sleep well, and exercise daily.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 1);

-- Insert Comments
INSERT INTO comments (name, email, body, post_id) VALUES ('John Doe', 'john@gmail.com', 'Great post! Very helpful.', 1);
INSERT INTO comments (name, email, body, post_id) VALUES ('Jane Smith', 'jane@gmail.com', 'Thanks for the tips!', 2);
