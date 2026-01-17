const API_BASE_URL = 'http://localhost:8080/api';

// Utility for making API requests
async function request(endpoint, method = 'GET', body = null) {
    const token = localStorage.getItem('accessToken');
    const headers = {
        'Content-Type': 'application/json'
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        method,
        headers
    };

    if (body) {
        config.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

        // Handle Unauthorized Access (e.g., expired token)
        if (response.status === 401) {
            logout();
            return null;
        }

        // For delete requests or empty responses
        if (response.status === 204) return true;

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Something went wrong');
        }

        return data;
    } catch (error) {
        console.error('API Request failed:', error);
        alert(error.message);
        return null;
    }
}

// Auth Helpers
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        return null;
    }
}

function getCurrentUser() {
    const token = localStorage.getItem('accessToken');
    if (!token) return null;
    const decoded = parseJwt(token);
    return decoded ? decoded.name : null; // Return 'name' claim
}

function getUserRole() {
    const token = localStorage.getItem('accessToken');
    if (!token) return null;
    const decoded = parseJwt(token);
    return decoded ? decoded.role : null;
}

function getCurrentUserId() {
    const token = localStorage.getItem('accessToken');
    if (!token) return null;
    const decoded = parseJwt(token);
    return decoded ? decoded.id : null;
}

function isAdmin() {
    return getUserRole() === 'ROLE_ADMIN';
}

function isAuthenticated() {
    return !!localStorage.getItem('accessToken');
}

function logout() {
    localStorage.removeItem('accessToken');
    window.location.href = 'login.html';
}

function checkAuthProtection() {
    if (!isAuthenticated()) {
        window.location.href = 'login.html';
    }
}

function checkAdminProtection() {
    if (!isAdmin()) {
        window.location.href = 'index.html';
    }
}

// UI Helpers
function updateNavbar() {
    const navLinks = document.getElementById('nav-links');
    if (!navLinks) return;

    const user = getCurrentUser();
    const adminMode = isAdmin();

    if (user) {
        navLinks.innerHTML = `
            <div style="display: flex; align-items: center; gap: 1rem;">
                ${adminMode ? '<a href="admin.html" class="nav-btn btn-outline" style="border-color: var(--primary-color); color: var(--primary-color);">Dashboard</a>' : ''}
                <a href="profile.html" style="text-decoration: none; display: flex; align-items: center; gap: 0.5rem;">
                    <span style="font-weight: 500; color: var(--text-primary);">
                        <span style="opacity: 0.7;">Hello,</span> ${user}
                    </span>
                    <div style="width: 35px; height: 35px; background: var(--primary-color); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold;">
                        ${user.charAt(0).toUpperCase()}
                    </div>
                </a>
                <a href="create-post.html" class="nav-btn btn-primary">Create Post</a>
                <a href="#" onclick="logout()" class="nav-btn btn-outline" style="border: none; color: var(--text-secondary);">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                </a>
            </div>
        `;
    } else {
        navLinks.innerHTML = `
            <a href="login.html" class="nav-btn btn-outline">Login</a>
            <a href="register.html" class="nav-btn btn-primary">Sign Up</a>
        `;
    }
}
