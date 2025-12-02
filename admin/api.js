// API Configuration
const API_BASE_URL = 'http://localhost:3000/api';

// Helper function to get auth token
function getAuthToken() {
    return localStorage.getItem('admin_token');
}

// Helper function to set auth token
function setAuthToken(token) {
    localStorage.setItem('admin_token', token);
}

// Helper function to remove auth token
function removeAuthToken() {
    localStorage.removeItem('admin_token');
}

// Helper function to check if user is authenticated
function isAuthenticated() {
    return getAuthToken() !== null;
}

// Helper function to get headers
function getHeaders() {
    const headers = {
        'Content-Type': 'application/json'
    };
    
    const token = getAuthToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    return headers;
}

// Generic API request function
async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const config = {
        ...options,
        headers: {
            ...getHeaders(),
            ...(options.headers || {})
        }
    };

    try {
        const response = await fetch(url, config);
        const data = await response.json();

        if (!response.ok || !data.success) {
            throw new Error(data.message || 'Có lỗi xảy ra');
        }

        return data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// ===== Auth API =====
const AuthAPI = {
    async login(email, password) {
        return apiRequest('/users/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
    },

    async getCurrentUser() {
        return apiRequest('/users/me');
    }
};

// ===== Products API =====
const ProductsAPI = {
    async getAll(filters = {}) {
        const queryParams = new URLSearchParams();
        if (filters.category) queryParams.append('category', filters.category);
        if (filters.search) queryParams.append('search', filters.search);
        if (filters.page) queryParams.append('page', filters.page);
        if (filters.limit) queryParams.append('limit', filters.limit);

        const query = queryParams.toString();
        return apiRequest(`/products${query ? '?' + query : ''}`);
    },

    async getById(id) {
        return apiRequest(`/products/${id}`);
    },

    async create(productData) {
        return apiRequest('/products', {
            method: 'POST',
            body: JSON.stringify(productData)
        });
    },

    async update(id, productData) {
        return apiRequest(`/products/${id}`, {
            method: 'PUT',
            body: JSON.stringify(productData)
        });
    },

    async delete(id) {
        return apiRequest(`/products/${id}`, {
            method: 'DELETE'
        });
    }
};

// ===== Orders API =====
const OrdersAPI = {
    async getAll(filters = {}) {
        const queryParams = new URLSearchParams();
        if (filters.status) queryParams.append('status', filters.status);
        if (filters.page) queryParams.append('page', filters.page);
        if (filters.limit) queryParams.append('limit', filters.limit);

        const query = queryParams.toString();
        return apiRequest(`/orders${query ? '?' + query : ''}`);
    },

    async getById(id) {
        return apiRequest(`/orders/${id}`);
    },

    async updateStatus(id, status) {
        return apiRequest(`/orders/${id}`, {
            method: 'PUT',
            body: JSON.stringify({ status })
        });
    },

    async delete(id) {
        return apiRequest(`/orders/${id}`, {
            method: 'DELETE'
        });
    }
};

// ===== Users API =====
const UsersAPI = {
    async getAll() {
        return apiRequest('/users');
    },

    async getById(id) {
        return apiRequest(`/users/${id}`);
    },

    async create(userData) {
        return apiRequest('/users/register', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
    },

    async update(id, userData) {
        return apiRequest(`/users/${id}`, {
            method: 'PUT',
            body: JSON.stringify(userData)
        });
    },

    async delete(id) {
        return apiRequest(`/users/${id}`, {
            method: 'DELETE'
        });
    }
};

// ===== Categories API =====
const CategoriesAPI = {
    async getAll() {
        return apiRequest('/categories');
    },

    async getById(id) {
        return apiRequest(`/categories/${id}`);
    },

    async create(categoryData) {
        return apiRequest('/categories', {
            method: 'POST',
            body: JSON.stringify(categoryData)
        });
    },

    async update(id, categoryData) {
        return apiRequest(`/categories/${id}`, {
            method: 'PUT',
            body: JSON.stringify(categoryData)
        });
    },

    async delete(id) {
        return apiRequest(`/categories/${id}`, {
            method: 'DELETE'
        });
    }
};

// Export APIs
window.AuthAPI = AuthAPI;
window.ProductsAPI = ProductsAPI;
window.OrdersAPI = OrdersAPI;
window.UsersAPI = UsersAPI;
window.CategoriesAPI = CategoriesAPI;
window.getAuthToken = getAuthToken;
window.setAuthToken = setAuthToken;
window.removeAuthToken = removeAuthToken;
window.isAuthenticated = isAuthenticated;

