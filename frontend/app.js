const API_BASE_URL = 'http://localhost:8080/api';
// const API_BASE_URL = 'https://1xwr637t-8080.inc1.devtunnels.ms/api';


// Utility for making API requests
// Utility for making API requests
async function request(endpoint, method = 'GET', body = null) {
    const token = localStorage.getItem('accessToken');
    const headers = {};

    // Only set JSON content type if NOT sending FormData
    if (!(body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        method,
        headers
    };

    if (body) {
        config.body = (body instanceof FormData) ? body : JSON.stringify(body);
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

        const contentType = response.headers.get("content-type");
        let data;
        if (contentType && contentType.indexOf("application/json") !== -1) {
            data = await response.json();
        } else {
            data = await response.text();
        }

        if (!response.ok) {
            const errorMsg = (typeof data === 'object' && data.message) ? data.message : (data || 'Something went wrong');
            throw new Error(errorMsg);
        }

        return data;
    } catch (error) {
        console.error('API Request failed:', error);
        showToast(error.message, 'error');
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
            <div style="display: flex; align-items: center; gap: 0.5rem;">
                <a href="index.html" class="nav-link">Home</a>
                ${adminMode ? '<a href="admin.html" class="nav-link">Dashboard</a>' : ''}
                
                <div class="nav-separator"></div>

                <a href="create-post.html" class="nav-btn btn-primary" style="margin-right: 0.5rem;">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right: 0.5rem;"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
                    Create
                </a>

                <a href="profile.html" class="user-profile-link" title="My Profile">
                    <div class="user-avatar-sm">
                        ${user.charAt(0).toUpperCase()}
                    </div>
                    <span style="font-weight: 500; font-size: 0.9rem; color: var(--text-primary);">
                        ${user.split(' ')[0]}
                    </span>
                </a>

                <a href="#" onclick="logout()" class="btn-icon-only" title="Logout">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
                </a>
            </div>
        `;
    } else {
        navLinks.innerHTML = `
            <a href="index.html" class="nav-btn btn-outline" style="border: none; color: var(--text-primary);">Home</a>
            <a href="login.html" class="nav-btn btn-outline">Login</a>
            <a href="register.html" class="nav-btn btn-primary">Sign Up</a>
        `;
    }
}
// UI Initialization
// We can expose this but we'll also lazy-init
function ensureGlobalUI() {
    if (!document.querySelector('.toast-container')) {
        const toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container';
        document.body.appendChild(toastContainer);
    }

    if (!document.querySelector('#global-confirm-modal')) {
        const modal = document.createElement('div');
        modal.id = 'global-confirm-modal';
        modal.className = 'modal-overlay'; // Standard class
        modal.innerHTML = `
            <div class="modal-content">
                <h3 class="modal-title" id="confirm-title">Confirm Action</h3>
                <p class="modal-text" id="confirm-message">Are you sure you want to proceed?</p>
                <div class="modal-actions">
                    <button class="nav-btn btn-outline" id="confirm-cancel">Cancel</button>
                    <button class="nav-btn btn-primary" id="confirm-yes">Confirm</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    }
}

function showToast(message, type = 'info') {
    ensureGlobalUI(); // Ensure container exists
    const container = document.querySelector('.toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <div class="toast-message">${message}</div>
    `;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'fadeOut 0.3s ease-out forwards';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Promise-based Confirm Modal
function showConfirm(message, title = 'Confirm Action') {
    ensureGlobalUI(); // Ensure modal exists
    return new Promise((resolve) => {
        const modal = document.getElementById('global-confirm-modal');
        const titleEl = document.getElementById('confirm-title');
        const msgEl = document.getElementById('confirm-message');
        const yesBtn = document.getElementById('confirm-yes');
        const cancelBtn = document.getElementById('confirm-cancel');

        titleEl.textContent = title;
        msgEl.textContent = message;
        modal.classList.add('open');

        // New cleaner listener handling to avoid duplicates
        // Use separate functions so references match for removeEventListener
        const handleYes = () => {
            cleanup();
            resolve(true);
        };

        const handleNo = () => {
            cleanup();
            resolve(false);
        };

        function cleanup() {
            yesBtn.removeEventListener('click', handleYes);
            cancelBtn.removeEventListener('click', handleNo);
            modal.classList.remove('open');
        }

        yesBtn.addEventListener('click', handleYes);
        cancelBtn.addEventListener('click', handleNo);
    });
}

function updateFooter() {
    const footer = document.getElementById('main-footer');
    if (!footer) return;

    footer.className = 'footer';
    footer.innerHTML = `
        <div class="container">
            <div class="footer-content">
                <a href="index.html" class="footer-brand">
                    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.24 12.24a6 6 0 0 0-8.49-8.49L5 10.5V19h8.5z"></path><line x1="16" y1="8" x2="2" y2="22"></line><line x1="17.5" y1="15" x2="9" y2="15"></line></svg>
                    BlogApp
                </a>
                <div class="footer-links">
                    <a href="index.html" class="footer-link">Home</a>
                    <a href="#" class="footer-link">About</a>
                    <a href="#" class="footer-link">Privacy Policy</a>
                    <a href="#" class="footer-link">Terms of Service</a>
                </div>
                <div class="footer-social">
                    <a href="#" class="btn-icon-only"><svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"></path></svg></a>
                    <a href="#" class="btn-icon-only"><svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="20" rx="5" ry="5"></rect><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"></path><line x1="17.5" y1="6.5" x2="17.51" y2="6.5"></line></svg></a>
                    <a href="#" class="btn-icon-only"><svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 3a10.9 10.9 0 0 1-3.14 1.53 4.48 4.48 0 0 0-7.86 3v1A10.66 10.66 0 0 1 3 4s-4 9 5 13a11.64 11.64 0 0 1-7 2c9 5 20 0 20-11.5a4.5 4.5 0 0 0-.08-.83A7.72 7.72 0 0 0 23 3z"></path></svg></a>
                </div>
            </div>
            <div class="footer-copyright">
                &copy; ${new Date().getFullYear()} BlogApp. All rights reserved.
            </div>
        </div>
    `;
}
