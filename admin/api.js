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

    if (endpoint === '/users/create' && (config.method || 'GET').toUpperCase() === 'POST') {
        try {
            const preview = typeof config.body === 'string' ? JSON.parse(config.body) : config.body;
            console.log('DEBUG /users/create payload:', preview);
        } catch (e) {
            console.log('DEBUG /users/create body (raw):', config.body);
        }
    }

    try {
        const response = await fetch(url, config);
        
        // Check if response is ok
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Lỗi kết nối server' }));
            throw new Error(errorData.message || `HTTP ${response.status}: ${response.statusText}`);
        }
        
        const data = await response.json();

        if (data && typeof data === 'object') {
            const hasSuccess = Object.prototype.hasOwnProperty.call(data, 'success');
            if (hasSuccess && data.success === false) {
                throw new Error(data.message || 'Có lỗi xảy ra');
            }
        }

        return data;
    } catch (error) {
        console.error('API Error:', error);
        
        // Handle network errors
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            throw new Error('Không thể kết nối đến server. Vui lòng kiểm tra:\n1. Backend server đang chạy tại http://localhost:3000\n2. Không có firewall chặn kết nối\n3. CORS đã được cấu hình đúng');
        }
        
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

    async getDetail(id) {
        return apiRequest(`/orders/detail/${id}`);
    },

    async getTopCustomers({ limit = 5, status = 'completed', start, end } = {}) {
        const queryParams = new URLSearchParams();
        if (limit) queryParams.append('limit', limit);
        if (status) queryParams.append('status', status);
        if (start) queryParams.append('start', start);
        if (end) queryParams.append('end', end);
        const query = queryParams.toString();
        return apiRequest(`/orders/top-customers${query ? '?' + query : ''}`);
    },

    async getTopProducts({ limit = 5, status = 'completed', start, end } = {}) {
        const queryParams = new URLSearchParams();
        if (limit) queryParams.append('limit', limit);
        if (status) queryParams.append('status', status);
        if (start) queryParams.append('start', start);
        if (end) queryParams.append('end', end);
        const query = queryParams.toString();
        return apiRequest(`/orders/top-products${query ? '?' + query : ''}`);
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
        return apiRequest('/users/list');
    },

    async getById(id) {
        return apiRequest(`/users/${id}`);
    },

    async create(userData) {
        return apiRequest('/users/create', {
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

