# BlogApp - Full Stack Social Blogging Platform

A robust, feature-rich blogging application built with **Spring Boot** (Backend) and **Vanilla HTML/CSS/JS** (Frontend). This application supports user authentication, rich text posting, commenting, liking, user profiles, and a comprehensive Admin Dashboard.

---

## 🚀 Technology Stack

### Backend
- **Core**: Java 17, Spring Boot 3.x
- **Database**: MySQL (Hibernate/Spring Data JPA)
- **Security**: Spring Security 6, JWT (JSON Web Tokens)
- **Tools**: Maven, Lombok

### Frontend
- **Structure**: Semantic HTML5
- **Styling**: Vanilla CSS (Responsive Design, Flexbox/Grid)
- **Logic**: Vanilla JavaScript (ES6+), Fetch API

---

## 🛠️ Setup Guide

### 1. Database Setup
Ensure you have MySQL installed and running.
1. Log in to MySQL: `mysql -u root -p`
2. Create the database:
   ```sql
   CREATE DATABASE blog_app;
   ```
   *(Note: The application is configured to automatically update the schema via `spring.jpa.hibernate.ddl-auto=update`)*

### 2. Backend Configuration
1. Open `src/main/resources/application.properties`.
2. Update your database credentials if different from default:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/blog_app?useSSL=false
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD
   ```

### 3. Running the Backend
1. Open the project in your IDE (IntelliJ, Eclipse, or VS Code).
2. Run `BlogAppApplication.java` as a Java Application.
3. The server will start at `http://localhost:8080`.

### 4. Frontend Setup
1. Navigate to the `frontend/` directory.
2. Open `index.html` in your browser.
   - *Recommended*: Use **Live Server** (VS Code Extension) or a simple Python http server (`python -m http.server`) to avoid file protocol issues, though the app is designed to handle basic file opening if CORS is allowed.

---

## 🔑 Key Features

### User Features
- **Authentication**: Sign Up and Login (JWT-based).
- **CRUD Posts**: Create, Read, Update, and Delete blog posts. 
- **Interactions**: Like posts and comment on them.
- **Profiles**: View public profiles of authors; manage your own "My Profile" (Edit Name, Username, Email).
- **Security**: Passwords are hashed (BCrypt); APIs are protected.

### Admin Features
- **Dashboard**: View all users, manage roles.
- **Role Management**: Promote users to ADMIN or revoke privileges.
- **User Management**: Deactivate (Soft Delete) or Activate users.
- **Content Moderation**: Delete any post or comment.

---

## 📡 API Documentation

### Auth Controller (`/api/auth`)
| Method | Endpoint | Description | Body |
| :--- | :--- | :--- | :--- |
| `POST` | `/login` | Authenticate user & get Token | `{ usernameOrEmail, password }` |
| `POST` | `/register` | Register a new user | `{ name, username, email, password }` |

### Post Controller (`/api/posts`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | Get all posts (supports paging) | No |
| `GET` | `/{id}` | Get post by ID | No |
| `POST` | `/` | Create a new post | **Yes** |
| `PUT` | `/{id}` | Update a post | **Yes** (Author/Admin) |
| `DELETE` | `/{id}` | Delete a post | **Yes** (Author/Admin) |
| `GET` | `/user/{id}` | Get posts by user ID | No |

### Comment Controller (`/api/posts/{postId}/comments`)
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | Get comments for a post | No |
| `POST` | `/` | Add a comment | **Yes** |
| `DELETE` | `/{id}` | Delete a comment | **Yes** (Author/Admin) |

### User Controller (`/api/users`)
| Method | Endpoint | Description | Role |
| :--- | :--- | :--- | :--- |
| `GET` | `/` | List all users | **ADMIN** |
| `DELETE` | `/{id}` | Deactivate User | **ADMIN** |
| `PUT` | `/{id}/activate` | Activate User | **ADMIN** |
| `PUT` | `/{id}/role` | Promote to Admin | **ADMIN** |
| `PUT` | `/{id}/role/revoke`| Revoke Admin | **ADMIN** |
| `GET` | `/me` | Get current user details | **User** |
| `PUT` | `/me` | Update current user details | **User** |
| `GET` | `/{id}/profile` | Get public user profile | **Any** |

### Like Controller (`/api/likes`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/post/{postId}` | Toggle Like (Like/Unlike) |

---

## 🖥️ Frontend Architecture

The frontend is a **Single Page Application (SPA)-like** multi-page experience that relies heavily on `app.js` for state management and API communication.

### Centralized API Handler
The `request()` function in `app.js` handles all network traffic:
1. **Token Injection**: Automatically attaches the `Authorization: Bearer <token>` header if a token exists in `localStorage`.
2. **Configuration**: Sets proper `Content-Type: application/json`.
3. **Response Handling**:
   - Parses JSON responses.
   - Handles `204 No Content` separately (returns `true`).
   - Handles `401 Unauthorized` by logging the user out instantly.

### Security Logic
- **`checkAuthProtection()`**: Runs on page load for protected pages (e.g., `create-post.html`, `profile.html`). Redirects to login if no token is found.
- **`checkAdminProtection()`**: Runs on `admin.html`. Decodes the JWT to check for `ROLE_ADMIN` claim. Redirects if unauthorized.

### Visual States
- **Navbar**: Dynamically renders "Login/Signup" VS "User Profile/Logout" based on auth state (`updateNavbar()`).
- **Badges**: 
  - **Blue Checkmark**: Indicates an Admin user.
  - **(Deactivated)**: Indicates a user who has been soft-deleted by an admin.
- **Admin Dashboard**:
  - **Active/Inactive Badges**: Shows direct database status of users.
  - **Dynamic Actions**: Switch between "Activate" and "Deactivate" buttons based on user state.
