# Walkthrough - Blog Application

I have successfully built the full-stack Blog Application with a Spring Boot API and a modern HTML/CSS/JS Frontend.

## 1. Project Overview
- **Backend**: Spring Boot 3.2.2 (Java 17+)
- **Frontend**: Vanilla HTML, CSS, JavaScript
- **Database**: H2 Database (In-Memory)
- **Security**: Spring Security + JWT
- **Documentation**: Swagger UI

## 2. Key Features
- **User Authentication**: Secure Login and Registration (JWT).
- **Blog Posts**: Create, Read, Update, Delete posts.
- **Comments**: Interactive commenting system.
- **Categories**: Categorized content.
- **UI/UX**: Modern, responsive design with gradient aesthetics.

## 3. How to Run

### Backend
1.  **Open Terminal** in `c:\Mdrive\Dev\Gem`.
2.  **Run**: `mvn spring-boot:run`
    *   API runs on: `http://localhost:8080`
    *   H2 Console: `http://localhost:8080/h2-console`
    *   Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Frontend
1.  **Navigate** to `c:\Mdrive\Dev\Gem\frontend`.
2.  **Open `index.html`** in your browser.
    *   *Tip: Use "Live Server" extension in VS Code for best experience, or just open the file directly.*

## 4. Testing Guide

### Frontend Testing
1.  **Register**: on the Sign Up page.
2.  **Login**: Use your credentials.
3.  **Create Post**: Click "Write Post" in navbar.
4.  **View Feed**: See your new post on the home page.
5.  **Comment**: Click "Read more" and add a comment.

### API Testing (Postman)
See [Postman Guide](#postman-testing-guide) below for direct API testing.

## 5. Postman Testing Guide

### Authentication
**1. Register**
- `POST http://localhost:8080/api/auth/register`
- Body: `{"name":"Test","username":"test","email":"test@test.com","password":"password"}`

**2. Login**
- `POST http://localhost:8080/api/auth/login`
- Body: `{"usernameOrEmail":"test","password":"password"}`
- **Copy Access Token** from response.

### Authorized Requests
Add Header: `Authorization: Bearer <your_token>`

**1. Create Post**
- `POST http://localhost:8080/api/posts`
- Body: `{"title":"My Post","description":"Desc...","content":"Content","categoryId":1}`
