// Main Application Logic
let currentPage = 'dashboard';
let allProducts = [];
let allOrders = [];
let allUsers = [];
let allCategories = [];

// Pagination
let currentProductPage = 1;
let currentOrderPage = 1;
let currentUserPage = 1;
const itemsPerPage = 10;

// Initialize app
document.addEventListener('DOMContentLoaded', function() {
    // Check authentication
    if (!isAuthenticated()) {
        window.location.href = 'index.html';
        return;
    }

    // Load user info
    loadUserInfo();

    // Setup navigation
    setupNavigation();

    // Load initial data
    loadDashboard();
    loadCategories();

    // Setup modals
    setupModals();
});

// Load user info
function loadUserInfo() {
    const userStr = localStorage.getItem('admin_user');
    if (userStr) {
        const user = JSON.parse(userStr);
        const userNameEl = document.getElementById('userName');
        if (userNameEl) {
            userNameEl.textContent = user.name || 'Admin';
        }
    }
}

// Setup navigation
function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const page = this.getAttribute('data-page');
            navigateToPage(page);
        });
    });
}

// Navigate to page
function navigateToPage(page) {
    // Update active nav item
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
        if (item.getAttribute('data-page') === page) {
            item.classList.add('active');
        }
    });

    // Hide all pages
    document.querySelectorAll('.page').forEach(p => {
        p.classList.remove('active');
    });

    // Show selected page
    const targetPage = document.getElementById(page);
    if (targetPage) {
        targetPage.classList.add('active');
        currentPage = page;

        // Update page title
        const pageTitle = document.getElementById('pageTitle');
        if (pageTitle) {
            const titles = {
                dashboard: 'Dashboard',
                products: 'Sản phẩm',
                orders: 'Đơn hàng',
                users: 'Người dùng',
                categories: 'Danh mục',
                statistics: 'Thống kê'
            };
            pageTitle.textContent = titles[page] || 'Dashboard';
        }

        // Load page data
        switch(page) {
            case 'dashboard':
                loadDashboard();
                break;
            case 'products':
                loadProducts();
                break;
            case 'orders':
                loadOrders();
                break;
            case 'users':
                loadUsers();
                break;
            case 'categories':
                loadCategories();
                break;
            case 'statistics':
                loadStatistics();
                break;
        }
    }
}

// Toggle sidebar (mobile)
function toggleSidebar() {
    document.querySelector('.sidebar').classList.toggle('active');
}

// Logout
function logout() {
    if (confirm('Bạn có chắc chắn muốn đăng xuất?')) {
        removeAuthToken();
        localStorage.removeItem('admin_user');
        window.location.href = 'index.html';
    }
}

// ===== Dashboard =====
async function loadDashboard() {
    try {
        // Load stats
        const [productsRes, ordersRes, usersRes] = await Promise.all([
            ProductsAPI.getAll(),
            OrdersAPI.getAll(),
            UsersAPI.getAll()
        ]);

        // Update stats
        const products = Array.isArray(productsRes.data) ? productsRes.data : (productsRes.data?.products || []);
        const orders = Array.isArray(ordersRes.data) ? ordersRes.data : (ordersRes.data?.orders || []);
        const users = Array.isArray(usersRes.data) ? usersRes.data : (usersRes.data?.users || []);
        
        document.getElementById('totalProducts').textContent = products.length;
        document.getElementById('totalOrders').textContent = orders.length;
        document.getElementById('totalUsers').textContent = users.length;

        // Calculate revenue
        const revenue = orders.reduce((sum, order) => {
            return sum + (order.totalAmount || order.total || 0);
        }, 0);
        document.getElementById('totalRevenue').textContent = formatCurrency(revenue);

        // Load recent orders
        loadRecentOrders(orders.slice(0, 5));

        // Load top products
        loadTopProducts(products);
    } catch (error) {
        console.error('Error loading dashboard:', error);
        showNotification('Không thể tải dữ liệu dashboard', 'error');
    }
}

function loadRecentOrders(orders) {
    const container = document.getElementById('recentOrders');
    if (!container) return;

    if (orders.length === 0) {
        container.innerHTML = '<p class="text-muted">Chưa có đơn hàng nào</p>';
        return;
    }

    const html = `
        <table class="data-table">
            <thead>
                <tr>
                    <th>Mã đơn</th>
                    <th>Khách hàng</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái</th>
                </tr>
            </thead>
            <tbody>
                ${orders.map(order => `
                    <tr>
                        <td>#${order._id?.substring(0, 8) || 'N/A'}</td>
                        <td>${order.user?.name || order.userId || 'N/A'}</td>
                        <td>${formatCurrency(order.totalAmount || order.total || 0)}</td>
                        <td><span class="status-badge status-${order.status || 'pending'}">${getStatusText(order.status)}</span></td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    container.innerHTML = html;
}

function loadTopProducts(products) {
    const container = document.getElementById('topProducts');
    if (!container) return;

    if (products.length === 0) {
        container.innerHTML = '<p class="text-muted">Chưa có sản phẩm nào</p>';
        return;
    }

    const topProducts = products.slice(0, 5);
    const html = `
        <table class="data-table">
            <thead>
                <tr>
                    <th>Tên sản phẩm</th>
                    <th>Giá</th>
                    <th>Tồn kho</th>
                </tr>
            </thead>
            <tbody>
                ${topProducts.map(product => `
                    <tr>
                        <td>${product.name || 'N/A'}</td>
                        <td>${formatCurrency(product.price || 0)}</td>
                        <td>${product.stock || 0}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
    container.innerHTML = html;
}

// ===== Products =====
async function loadProducts() {
    try {
        const response = await ProductsAPI.getAll();
        allProducts = Array.isArray(response.data) ? response.data : (response.data?.products || []);
        currentProductPage = 1; // Reset to first page
        renderProducts(allProducts);
    } catch (error) {
        console.error('Error loading products:', error);
        showNotification('Không thể tải danh sách sản phẩm', 'error');
    }
}

function renderProducts(products) {
    const tbody = document.getElementById('productsTableBody');
    if (!tbody) return;

    if (products.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">Không có sản phẩm nào</td></tr>';
        renderPagination('products', 0);
        return;
    }

    const totalPages = Math.ceil(products.length / itemsPerPage);
    const startIndex = (currentProductPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedProducts = products.slice(startIndex, endIndex);

    tbody.innerHTML = paginatedProducts.map(product => {
        const categoryName = product.category?.name || 'N/A';
        const imageUrl = product.image || 'https://via.placeholder.com/50';
        
        return `
            <tr>
                <td>
                    <img src="${imageUrl}" alt="${product.name}" onerror="this.src='https://via.placeholder.com/50'">
                </td>
                <td>${product.name || 'N/A'}</td>
                <td>${categoryName}</td>
                <td>${formatCurrency(product.price || 0)}</td>
                <td>${product.stock || 0}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn action-btn-edit" onclick="editProduct('${product._id}')">
                            <i class="fas fa-edit"></i> Sửa
                        </button>
                        <button class="action-btn action-btn-delete" onclick="deleteProduct('${product._id}')">
                            <i class="fas fa-trash"></i> Xóa
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    renderPagination('products', totalPages, currentProductPage);
}

function searchProducts() {
    const searchTerm = document.getElementById('productSearch').value.toLowerCase();
    const filtered = allProducts.filter(p => 
        p.name?.toLowerCase().includes(searchTerm)
    );
    renderProducts(filtered);
}

function filterProducts() {
    const categoryId = document.getElementById('categoryFilter').value;
    let filtered = allProducts;
    
    if (categoryId) {
        filtered = allProducts.filter(p => p.category?._id === categoryId || p.category === categoryId);
    }
    
    renderProducts(filtered);
}

function openAddProductModal() {
    document.getElementById('productModalTitle').textContent = 'Thêm sản phẩm mới';
    document.getElementById('productSubmitText').textContent = 'Thêm sản phẩm';
    document.getElementById('productForm').reset();
    document.getElementById('productId').value = '';
    clearImagePreview();
    openModal('productModal');
}

async function editProduct(id) {
    try {
        const response = await ProductsAPI.getById(id);
        const product = response.data;
        
        document.getElementById('productModalTitle').textContent = 'Sửa sản phẩm';
        document.getElementById('productSubmitText').textContent = 'Cập nhật';
        document.getElementById('productId').value = product._id;
        document.getElementById('productName').value = product.name || '';
        document.getElementById('productDescription').value = product.description || '';
        document.getElementById('productPrice').value = product.price || 0;
        document.getElementById('productStock').value = product.stock || 0;
        document.getElementById('productCategory').value = product.category?._id || product.category || '';
        document.getElementById('productImage').value = product.image || '';
        document.getElementById('productRating').value = product.rating || 4.5;
        document.getElementById('productSold').value = product.soldCount || 0;
        document.getElementById('productDiscount').value = product.discount || 0;
        
        // Show image preview if exists
        if (product.image) {
            document.getElementById('previewImg').src = product.image;
            document.getElementById('imagePreview').style.display = 'block';
            uploadedImageUrl = product.image;
        } else {
            clearImagePreview();
        }
        
        openModal('productModal');
    } catch (error) {
        showNotification('Không thể tải thông tin sản phẩm', 'error');
    }
}

async function deleteProduct(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) return;

    try {
        await ProductsAPI.delete(id);
        showNotification('Xóa sản phẩm thành công', 'success');
        loadProducts();
    } catch (error) {
        showNotification('Không thể xóa sản phẩm', 'error');
    }
}

// Product form submit
let uploadedImageUrl = null;

async function handleImageUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
        showNotification('Vui lòng chọn file ảnh', 'warning');
        return;
    }

    if (file.size > 5 * 1024 * 1024) { // 5MB
        showNotification('Kích thước ảnh không được vượt quá 5MB', 'warning');
        return;
    }

    const formData = new FormData();
    formData.append('image', file);

    try {
        showNotification('Đang upload ảnh...', 'info');
        const response = await fetch('http://localhost:3000/api/upload', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error('Upload thất bại');
        }

        const data = await response.json();
        uploadedImageUrl = `http://localhost:3000${data.path}`;
        
        // Show preview
        document.getElementById('previewImg').src = uploadedImageUrl;
        document.getElementById('imagePreview').style.display = 'block';
        document.getElementById('productImage').value = uploadedImageUrl;
        
        showNotification('Upload ảnh thành công', 'success');
    } catch (error) {
        showNotification('Không thể upload ảnh: ' + error.message, 'error');
    }
}

function clearImagePreview() {
    uploadedImageUrl = null;
    document.getElementById('productImageFile').value = '';
    document.getElementById('productImage').value = '';
    document.getElementById('imagePreview').style.display = 'none';
}

document.addEventListener('DOMContentLoaded', function() {
    const productForm = document.getElementById('productForm');
    if (productForm) {
        productForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const productId = document.getElementById('productId').value;
            const productData = {
                name: document.getElementById('productName').value,
                description: document.getElementById('productDescription').value,
                price: parseFloat(document.getElementById('productPrice').value),
                stock: parseInt(document.getElementById('productStock').value),
                category: document.getElementById('productCategory').value,
                image: uploadedImageUrl || document.getElementById('productImage').value || undefined,
                rating: parseFloat(document.getElementById('productRating').value),
                soldCount: parseInt(document.getElementById('productSold').value),
                discount: parseFloat(document.getElementById('productDiscount').value)
            };

            try {
                if (productId) {
                    await ProductsAPI.update(productId, productData);
                    showNotification('Cập nhật sản phẩm thành công', 'success');
                } else {
                    await ProductsAPI.create(productData);
                    showNotification('Thêm sản phẩm thành công', 'success');
                }
                clearImagePreview();
                closeModal('productModal');
                loadProducts();
            } catch (error) {
                showNotification(error.message || 'Có lỗi xảy ra', 'error');
            }
        });
    }
});

// ===== Orders =====
async function loadOrders() {
    try {
        const response = await OrdersAPI.getAll();
        allOrders = Array.isArray(response.data) ? response.data : (response.data?.orders || []);
        currentOrderPage = 1; // Reset to first page
        renderOrders(allOrders);
    } catch (error) {
        console.error('Error loading orders:', error);
        showNotification('Không thể tải danh sách đơn hàng', 'error');
    }
}

function renderOrders(orders) {
    const tbody = document.getElementById('ordersTableBody');
    if (!tbody) return;

    if (orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">Không có đơn hàng nào</td></tr>';
        renderPagination('orders', 0);
        return;
    }

    const totalPages = Math.ceil(orders.length / itemsPerPage);
    const startIndex = (currentOrderPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedOrders = orders.slice(startIndex, endIndex);

    tbody.innerHTML = paginatedOrders.map(order => {
        const orderId = order._id?.substring(0, 8) || 'N/A';
        const customerName = order.user?.name || order.userId || 'N/A';
        const itemCount = order.items?.length || 0;
        const total = order.totalAmount || order.total || 0;
        const status = order.status || 'pending';
        const createdAt = order.createdAt ? new Date(order.createdAt).toLocaleDateString('vi-VN') : 'N/A';

        return `
            <tr>
                <td>#${orderId}</td>
                <td>${customerName}</td>
                <td>${itemCount} sản phẩm</td>
                <td>${formatCurrency(total)}</td>
                <td><span class="status-badge status-${status}">${getStatusText(status)}</span></td>
                <td>${createdAt}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn action-btn-edit" onclick="viewOrder('${order._id}')">
                            <i class="fas fa-eye"></i> Xem
                        </button>
                        <button class="action-btn action-btn-delete" onclick="deleteOrder('${order._id}')">
                            <i class="fas fa-trash"></i> Xóa
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    renderPagination('orders', totalPages, currentOrderPage);
}

function filterOrders() {
    const status = document.getElementById('orderStatusFilter').value;
    let filtered = allOrders;
    
    if (status) {
        filtered = allOrders.filter(o => o.status === status);
    }
    
    renderOrders(filtered);
}

async function viewOrder(id) {
    try {
        const response = await OrdersAPI.getDetail(id);
        const order = response.data?.data || response.data;
        
        const itemsHtml = order.items?.map(item => {
            const product = item.product || {};
            return `
                <div class="order-item">
                    <img src="${product.image || 'https://via.placeholder.com/60'}" alt="${product.name}" onerror="this.src='https://via.placeholder.com/60'">
                    <div class="order-item-info">
                        <h4>${product.name || 'N/A'}</h4>
                        <p>Giá: ${formatCurrency(item.price || 0)} x ${item.quantity || 0}</p>
                    </div>
                    <div class="order-item-total">
                        ${formatCurrency((item.price || 0) * (item.quantity || 0))}
                    </div>
                </div>
            `;
        }).join('') || '<p class="text-muted">Không có sản phẩm</p>';
        
        const content = `
            <div class="order-detail">
                <div class="order-info-section">
                    <h4>Thông tin đơn hàng</h4>
                    <div class="info-row">
                        <span class="info-label">Mã đơn:</span>
                        <span class="info-value">#${order._id?.substring(0, 8) || 'N/A'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Khách hàng:</span>
                        <span class="info-value">${order.user?.name || order.userId || 'N/A'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Email:</span>
                        <span class="info-value">${order.user?.email || 'N/A'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Ngày đặt:</span>
                        <span class="info-value">${order.createdAt ? new Date(order.createdAt).toLocaleString('vi-VN') : 'N/A'}</span>
                    </div>
                    <div class="info-row">
                        <span class="info-label">Trạng thái:</span>
                        <span class="info-value">
                            <span class="status-badge status-${order.status || 'pending'}">${getStatusText(order.status)}</span>
                        </span>
                    </div>
                </div>

                <div class="order-info-section">
                    <h4>Địa chỉ giao hàng</h4>
                    <p>${order.shippingAddress || 'N/A'}</p>
                </div>

                <div class="order-info-section">
                    <h4>Sản phẩm</h4>
                    <div class="order-items-list">
                        ${itemsHtml}
                    </div>
                </div>

                <div class="order-info-section">
                    <div class="order-total">
                        <span class="total-label">Tổng tiền:</span>
                        <span class="total-amount">${formatCurrency(order.totalAmount || order.total || 0)}</span>
                    </div>
                </div>

                <div class="order-info-section">
                    <h4>Cập nhật trạng thái</h4>
                    <select id="orderStatusSelect" class="form-control" style="margin-bottom: 15px;">
                        <option value="pending" ${order.status === 'pending' ? 'selected' : ''}>Chờ xác nhận</option>
                        <option value="confirmed" ${order.status === 'confirmed' ? 'selected' : ''}>Đã xác nhận</option>
                        <option value="shipping" ${order.status === 'shipping' ? 'selected' : ''}>Đang giao</option>
                        <option value="delivered" ${order.status === 'delivered' ? 'selected' : ''}>Đã giao</option>
                        <option value="cancelled" ${order.status === 'cancelled' ? 'selected' : ''}>Đã hủy</option>
                    </select>
                    <button class="btn-primary" onclick="updateOrderStatus('${order._id}')">
                        <i class="fas fa-save"></i> Cập nhật trạng thái
                    </button>
                </div>
            </div>
        `;
        
        document.getElementById('orderDetailContent').innerHTML = content;
        openModal('orderDetailModal');
    } catch (error) {
        showNotification('Không thể tải chi tiết đơn hàng', 'error');
    }
}

async function updateOrderStatus(orderId) {
    const status = document.getElementById('orderStatusSelect').value;
    if (!status) {
        showNotification('Vui lòng chọn trạng thái', 'warning');
        return;
    }

    try {
        await OrdersAPI.updateStatus(orderId, status);
        showNotification('Cập nhật trạng thái đơn hàng thành công', 'success');
        closeModal('orderDetailModal');
        loadOrders();
        if (currentPage === 'dashboard') {
            loadDashboard();
        }
    } catch (error) {
        showNotification('Không thể cập nhật trạng thái', 'error');
    }
}

async function deleteOrder(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa đơn hàng này?')) return;

    try {
        await OrdersAPI.delete(id);
        showNotification('Xóa đơn hàng thành công', 'success');
        loadOrders();
    } catch (error) {
        showNotification('Không thể xóa đơn hàng', 'error');
    }
}

// ===== Users =====
async function loadUsers() {
    try {
        const response = await UsersAPI.getAll();
        let users = [];
        if (Array.isArray(response.data)) users = response.data;
        else if (Array.isArray(response.data?.users)) users = response.data.users;
        else if (Array.isArray(response.data?.items)) users = response.data.items;
        else if (Array.isArray(response.items)) users = response.items;
        else if (response.data && typeof response.data === 'object') {
            const firstArray = Object.values(response.data).find(v => Array.isArray(v));
            users = firstArray || [];
        }
        allUsers = users;
        currentUserPage = 1; // Reset to first page
        renderUsers(allUsers);
    } catch (error) {
        console.error('Error loading users:', error);
        showNotification('Không thể tải danh sách người dùng', 'error');
    }
}

function renderUsers(users) {
    const tbody = document.getElementById('usersTableBody');
    if (!tbody) return;

    if (users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">Không có người dùng nào</td></tr>';
        renderPagination('users', 0);
        return;
    }

    const totalPages = Math.ceil(users.length / itemsPerPage);
    const startIndex = (currentUserPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedUsers = users.slice(startIndex, endIndex);

    tbody.innerHTML = paginatedUsers.map(user => {
        const createdAt = user.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : 'N/A';
        const role = user.role || 'user';
        const roleBadge = role === 'admin' ? '<span class="status-badge status-confirmed">Admin</span>' : '<span class="status-badge status-pending">User</span>';

        return `
            <tr>
                <td>${user.name || 'N/A'}</td>
                <td>${user.email || 'N/A'}</td>
                <td>${roleBadge}</td>
                <td>${createdAt}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn action-btn-delete" onclick="deleteUser('${user._id || user.id}')">
                            <i class="fas fa-trash"></i> Xóa
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    renderPagination('users', totalPages, currentUserPage);
}

function openAddUserModal() {
    document.getElementById('userModalTitle').textContent = 'Thêm người dùng mới';
    document.getElementById('userSubmitText').textContent = 'Thêm người dùng';
    document.getElementById('userForm').reset();
    document.getElementById('userId').value = '';
    document.getElementById('passwordHint').style.display = 'none';
    document.getElementById('userPassword').required = true;
    openModal('userModal');
}

async function editUser(id) {
    try {
        const response = await UsersAPI.getById(id);
        const user = response.data;
        
        document.getElementById('userModalTitle').textContent = 'Sửa người dùng';
        document.getElementById('userSubmitText').textContent = 'Cập nhật';
        document.getElementById('userId').value = user._id || user.id;
        document.getElementById('userNameInput').value = user.name || '';
        document.getElementById('userEmail').value = user.email || '';
        document.getElementById('userRole').value = user.role || 'user';
        document.getElementById('userPassword').value = '';
        document.getElementById('userPassword').required = false;
        document.getElementById('passwordHint').style.display = 'block';
        
        openModal('userModal');
    } catch (error) {
        showNotification('Không thể tải thông tin người dùng', 'error');
    }
}

async function deleteUser(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa người dùng này?')) return;

    try {
        await UsersAPI.delete(id);
        showNotification('Xóa người dùng thành công', 'success');
        loadUsers();
    } catch (error) {
        showNotification('Không thể xóa người dùng', 'error');
    }
}

// ===== Categories =====
async function loadCategories() {
    try {
        const response = await CategoriesAPI.getAll();
        allCategories = Array.isArray(response.data) ? response.data : (response.data?.categories || []);
        
        // Populate category select in product form
        const categorySelect = document.getElementById('productCategory');
        const categoryFilter = document.getElementById('categoryFilter');
        
        if (categorySelect) {
            categorySelect.innerHTML = '<option value="">Chọn danh mục</option>' +
                allCategories.map(cat => 
                    `<option value="${cat._id}">${cat.name}</option>`
                ).join('');
        }
        
        if (categoryFilter) {
            categoryFilter.innerHTML = '<option value="">Tất cả danh mục</option>' +
                allCategories.map(cat => 
                    `<option value="${cat._id}">${cat.name}</option>`
                ).join('');
        }
        
        // Render categories table if on categories page
        if (currentPage === 'categories') {
            renderCategories(allCategories);
        }
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

function renderCategories(categories) {
    const tbody = document.getElementById('categoriesTableBody');
    if (!tbody) return;

    if (categories.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center">Không có danh mục nào</td></tr>';
        return;
    }

    tbody.innerHTML = categories.map(category => {
        const createdAt = category.createdAt ? new Date(category.createdAt).toLocaleDateString('vi-VN') : 'N/A';
        
        return `
            <tr>
                <td>${category.name || 'N/A'}</td>
                <td>${category.description || 'N/A'}</td>
                <td>${createdAt}</td>
                <td>
                    <div class="action-buttons">
                        <button class="action-btn action-btn-edit" onclick="editCategory('${category._id}')">
                            <i class="fas fa-edit"></i> Sửa
                        </button>
                        <button class="action-btn action-btn-delete" onclick="deleteCategory('${category._id}')">
                            <i class="fas fa-trash"></i> Xóa
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function openAddCategoryModal() {
    document.getElementById('categoryForm').reset();
    openModal('categoryModal');
}

async function editCategory(id) {
    try {
        const response = await CategoriesAPI.getById(id);
        const category = response.data;
        
        document.getElementById('categoryName').value = category.name || '';
        document.getElementById('categoryDescription').value = category.description || '';
        
        // Store category ID for update
        document.getElementById('categoryForm').setAttribute('data-category-id', id);
        openModal('categoryModal');
    } catch (error) {
        showNotification('Không thể tải thông tin danh mục', 'error');
    }
}

async function deleteCategory(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa danh mục này?')) return;

    try {
        await CategoriesAPI.delete(id);
        showNotification('Xóa danh mục thành công', 'success');
        loadCategories();
    } catch (error) {
        showNotification('Không thể xóa danh mục', 'error');
    }
}

// Category form submit
document.addEventListener('DOMContentLoaded', function() {
    const categoryForm = document.getElementById('categoryForm');
    if (categoryForm) {
        categoryForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const categoryId = this.getAttribute('data-category-id');
            const categoryData = {
                name: document.getElementById('categoryName').value,
                description: document.getElementById('categoryDescription').value
            };

            try {
                if (categoryId) {
                    await CategoriesAPI.update(categoryId, categoryData);
                    showNotification('Cập nhật danh mục thành công', 'success');
                } else {
                    await CategoriesAPI.create(categoryData);
                    showNotification('Thêm danh mục thành công', 'success');
                }
                this.removeAttribute('data-category-id');
                closeModal('categoryModal');
                loadCategories();
            } catch (error) {
                showNotification(error.message || 'Có lỗi xảy ra', 'error');
            }
        });
    }

    // User form submit
    const userForm = document.getElementById('userForm');
    if (userForm) {
        userForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            if (this.dataset.submitting === 'true') return;
            this.dataset.submitting = 'true';
            
            const userId = document.getElementById('userId').value;
            const nameVal = (document.getElementById('userNameInput').value || '').trim();
            const emailVal = (document.getElementById('userEmail').value || '').trim();
            let roleVal = (document.getElementById('userRole').value || '').trim();
            const passwordVal = (document.getElementById('userPassword').value || '').trim();

            // Normalize role
            roleVal = roleVal ? roleVal.toLowerCase() : 'user';

            // Use native HTML validation first
            if (!this.checkValidity()) {
                showNotification('Vui lòng điền đúng thông tin bắt buộc', 'warning');
                return;
            }

            // Additional create-only check: require password
            if (!userId && !passwordVal) {
                showNotification('Mật khẩu là bắt buộc khi tạo mới', 'warning');
                return;
            }

            const userData = { name: nameVal, email: emailVal, role: roleVal };

            try {
                if (userId) {
                    await UsersAPI.update(userId, userData);
                    showNotification('Cập nhật người dùng thành công', 'success');
                } else {
                    if (passwordVal.length < 6) {
                        showNotification('Mật khẩu tối thiểu 6 ký tự', 'warning');
                        this.dataset.submitting = 'false';
                        return;
                    }
                    const createData = { name: nameVal, email: emailVal, role: roleVal, password: passwordVal };
                    const res = await UsersAPI.create(createData);
                    console.log('Create user response:', res);
                    showNotification('Thêm người dùng thành công', 'success');
                }
                closeModal('userModal');
                loadUsers();
                if (currentPage === 'dashboard') {
                    loadDashboard();
                }
            } catch (error) {
                showNotification(error.message || 'Có lỗi xảy ra', 'error');
            } finally {
                this.dataset.submitting = 'false';
            }
        });
    }
});

// ===== Statistics =====
async function loadStatistics() {
    try {
        const [ordersRes, productsRes] = await Promise.all([
            OrdersAPI.getAll(),
            ProductsAPI.getAll()
        ]);

        const orders = Array.isArray(ordersRes.data) ? ordersRes.data : (ordersRes.data?.orders || []);
        const products = Array.isArray(productsRes.data) ? productsRes.data : (productsRes.data?.products || []);

        // Calculate monthly revenue
        const monthlyRevenue = {};
        orders.forEach(order => {
            if (isOrderCompleted(order.status)) {
                const date = new Date(order.createdAt);
                const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
                monthlyRevenue[monthKey] = (monthlyRevenue[monthKey] || 0) + (order.totalAmount || order.total || 0);
            }
        });

        const statsPage = document.getElementById('statistics');
        const chartContainer = document.getElementById('revenueChart');
        const compareEl = document.getElementById('revenueCompare');
        const viewSelect = document.getElementById('chartViewSelect');
        const yearSelect = document.getElementById('chartYearSelect');
        const monthSelect = document.getElementById('chartMonthSelect');
        const monthStartInput = document.getElementById('monthStartInput');
        const monthEndInput = document.getElementById('monthEndInput');
        const dayStartInput = document.getElementById('dayStartInput');
        const dayEndInput = document.getElementById('dayEndInput');
        const applyRangeBtn = document.getElementById('applyRangeBtn');

        const deliveredOrders = orders.filter(o => isOrderCompleted(o.status));

        const years = Array.from(new Set(orders.map(o => {
            const d = new Date(o.createdAt);
            return isNaN(d.getTime()) ? null : d.getFullYear();
        }).filter(y => y !== null))).sort((a,b)=>b-a);
        if (yearSelect) {
            const options = years.length > 0 ? years.map(y => `<option value="${y}">${y}</option>`).join('') : `<option value="${new Date().getFullYear()}">${new Date().getFullYear()}</option>`;
            yearSelect.innerHTML = options;
        }
        if (monthSelect) {
            monthSelect.innerHTML = Array.from({length:12}, (_,i)=>{
                const m = i+1; const label = `Tháng ${String(m).padStart(2,'0')}`;
                return `<option value="${m}">${label}</option>`;
            }).join('');
        }

        const now = new Date();
        const defaultYear = years[0] || now.getFullYear();
        const defaultMonth = now.getMonth()+1;
        if (yearSelect) yearSelect.value = String(defaultYear);
        if (monthSelect) monthSelect.value = String(defaultMonth);
        if (viewSelect) viewSelect.value = 'month';

        function renderRevenueChart(view, year, month) {
            if (!chartContainer) return;
            let labels = [];
            let values = [];
            let maxRevenue = 1;
            let compareText = '';

            if (view === 'month') {
                const monthTotals = Array.from({length:12}, (_,i)=>0);
                deliveredOrders.forEach(o => {
                    const d = new Date(o.createdAt);
                    if (d.getFullYear() === Number(year)) {
                        const idx = d.getMonth();
                        monthTotals[idx] += (o.totalAmount || o.total || 0);
                    }
                });
                labels = Array.from({length:12}, (_,i)=>new Date(Number(year), i, 1).toLocaleDateString('vi-VN', { month: 'short' }));
                values = monthTotals;
                maxRevenue = Math.max(...values, 1);
                const lastIdx = values.slice().map((v,i)=>({v,i})).reverse().find(x=>x.v>0)?.i ?? null;
                if (lastIdx !== null) {
                    const curr = values[lastIdx];
                    const prev = values[lastIdx-1] ?? 0;
                    const diff = curr - prev;
                    const pct = prev === 0 ? 100 : Math.round((diff/prev)*100);
                    const sign = diff >= 0 ? '+' : '';
                    compareText = `Tháng gần nhất: ${formatCurrency(curr)} (${sign}${pct}% so với tháng trước)`;
                }
            } else if (view === 'day') {
                const daysInMonth = new Date(Number(year), Number(month), 0).getDate();
                const dayTotals = Array.from({length:daysInMonth}, ()=>0);
                deliveredOrders.forEach(o => {
                    const d = new Date(o.createdAt);
                    if (d.getFullYear() === Number(year) && (d.getMonth()+1) === Number(month)) {
                        dayTotals[d.getDate()-1] += (o.totalAmount || o.total || 0);
                    }
                });
                labels = Array.from({length:daysInMonth}, (_,i)=>String(i+1));
                values = dayTotals;
                maxRevenue = Math.max(...values, 1);
                const currTotal = values.reduce((s,v)=>s+v,0);
                const prevMonth = Number(month) - 1 || 12;
                const prevYear = prevMonth === 12 ? Number(year)-1 : Number(year);
                const prevDays = new Date(prevYear, prevMonth, 0).getDate();
                const prevTotals = Array.from({length:prevDays}, ()=>0);
                deliveredOrders.forEach(o => {
                    const d = new Date(o.createdAt);
                    if (d.getFullYear() === prevYear && (d.getMonth()+1) === prevMonth) {
                        prevTotals[d.getDate()-1] += (o.totalAmount || o.total || 0);
                    }
                });
                const prevTotal = prevTotals.reduce((s,v)=>s+v,0);
                const diff = currTotal - prevTotal;
                const pct = prevTotal === 0 ? 100 : Math.round((diff/prevTotal)*100);
                const sign = diff >= 0 ? '+' : '';
                compareText = `Tháng ${String(month).padStart(2,'0')}/${year}: ${formatCurrency(currTotal)} (${sign}${pct}% so với tháng trước)`;
            } else if (view === 'day_range') {
                const start = new Date(dayStartInput?.value || new Date());
                const end = new Date(dayEndInput?.value || new Date());
                if (start > end) { const t = start; start = end; end = t; }
                const days = Math.ceil((end - start) / (24*3600*1000)) + 1;
                const totals = Array.from({length:days}, ()=>0);
                deliveredOrders.forEach(o => {
                    const d = new Date(o.createdAt);
                    if (d >= start && d <= end) {
                        const idx = Math.floor((d - start)/(24*3600*1000));
                        totals[idx] += (o.totalAmount || o.total || 0);
                    }
                });
                labels = Array.from({length:days}, (_,i)=>{
                    const d = new Date(start.getTime() + i*24*3600*1000);
                    return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
                });
                values = totals;
                maxRevenue = Math.max(...values, 1);
                compareText = `${labels[0]} – ${labels[labels.length-1]}: ${formatCurrency(values.reduce((s,v)=>s+v,0))}`;
            } else if (view === 'month_range') {
                const ms = monthStartInput?.value;
                const me = monthEndInput?.value;
                let sY = Number(ms?.split('-')[0]);
                let sM = Number(ms?.split('-')[1]);
                let eY = Number(me?.split('-')[0]);
                let eM = Number(me?.split('-')[1]);
                if (!sY || !sM || !eY || !eM) {
                    const nowM = new Date();
                    sY = nowM.getFullYear(); sM = 1; eY = nowM.getFullYear(); eM = nowM.getMonth()+1;
                }
                const start = new Date(sY, sM-1, 1);
                const end = new Date(eY, eM, 0, 23, 59, 59, 999);
                const months = [];
                let curY = sY, curM = sM;
                while (curY < eY || (curY === eY && curM <= eM)) {
                    months.push({ y: curY, m: curM });
                    curM++; if (curM === 13) { curM = 1; curY++; }
                }
                const monthTotals = months.map(() => 0);
                deliveredOrders.forEach(o => {
                    const d = new Date(o.createdAt);
                    if (d >= start && d <= end) {
                        const idx = months.findIndex(mm => mm.y === d.getFullYear() && mm.m === (d.getMonth()+1));
                        if (idx >= 0) monthTotals[idx] += (o.totalAmount || o.total || 0);
                    }
                });
                labels = months.map(mm => new Date(mm.y, mm.m-1, 1).toLocaleDateString('vi-VN', { month: 'short', year: 'numeric' }));
                values = monthTotals;
                maxRevenue = Math.max(...values, 1);
                compareText = `${labels[0]} – ${labels[labels.length-1]}: ${formatCurrency(values.reduce((s,v)=>s+v,0))}`;
            }

            chartContainer.innerHTML = `
                <div class="chart-container">
                    ${labels.map((label, index) => {
                        const height = (values[index] / maxRevenue) * 100;
                        return `
                            <div class="chart-bar-wrapper">
                                <div class="chart-bar" style="height: ${height}%">
                                    <span class="chart-value">${formatCurrency(values[index])}</span>
                                </div>
                                <span class="chart-label">${label}</span>
                            </div>
                        `;
                    }).join('')}
                </div>
                ${values.every(v=>v===0) ? '<p class="text-muted">Chưa có dữ liệu doanh thu</p>' : ''}
            `;
            if (compareEl) compareEl.textContent = compareText;
        }

        if (viewSelect && yearSelect && monthSelect) {
            function syncVisibility() {
                const v = viewSelect.value;
                yearSelect.style.display = v === 'month' || v === 'day' ? 'inline-block' : 'none';
                monthSelect.style.display = v === 'day' ? 'inline-block' : 'none';
                monthStartInput.style.display = v === 'month_range' ? 'inline-block' : 'none';
                monthEndInput.style.display = v === 'month_range' ? 'inline-block' : 'none';
                dayStartInput.style.display = v === 'day_range' ? 'inline-block' : 'none';
                dayEndInput.style.display = v === 'day_range' ? 'inline-block' : 'none';
                applyRangeBtn.style.display = v === 'day_range' || v === 'month_range' ? 'inline-block' : 'none';
            }
            syncVisibility();
            viewSelect.onchange = () => { syncVisibility(); renderRevenueChart(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value)); renderTopCustomers(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value)); renderTopProducts(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value)); };
            yearSelect.onchange = () => { renderRevenueChart(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value)); renderTopCustomers(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value)); renderTopProducts(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value)); };
            monthSelect.onchange = () => { renderRevenueChart(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value)); renderTopCustomers(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value)); renderTopProducts(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value)); };
            applyRangeBtn.onclick = () => { renderRevenueChart(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value)); renderTopCustomers(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value)); renderTopProducts(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value)); };
        }
        renderRevenueChart(viewSelect ? viewSelect.value : 'month', Number(yearSelect?.value || defaultYear), Number(monthSelect?.value || defaultMonth));

        async function renderTopProducts(view, year, month) {
            const topProductsContainer = document.getElementById('topProductsContainer');
            if (!topProductsContainer) return;
            let start, end;
            if (typeof computeRange === 'function') {
                const r = computeRange(view, year, month); start = r.start; end = r.end;
            }
            try {
                const res = await OrdersAPI.getTopProducts({ limit: 5, status: 'completed', start, end });
                let items = [];
                const d = res.data;
                if (Array.isArray(d)) items = d;
                else if (Array.isArray(d?.items)) items = d.items;
                else if (Array.isArray(d?.data)) items = d.data;
                else if (Array.isArray(res.items)) items = res.items;
                else if (d && typeof d === 'object') {
                    const firstArray = Object.values(d).find(v => Array.isArray(v));
                    items = firstArray || [];
                }

                const rows = items.map(it => {
                    const p = it.product || {};
                    const name = p.name || it.name || 'N/A';
                    const sold = it.sold || it.soldCount || 0;
                    const revenue = it.revenue || ((p.price || 0) * sold);
                    return `
                        <tr>
                            <td>${name}</td>
                            <td>${sold}</td>
                            <td>${formatCurrency(revenue)}</td>
                        </tr>
                    `;
                }).join('');

                const html = `
                    <div class="card">
                        <div class="card-header">
                            <h3>Sản phẩm bán chạy nhất</h3>
                        </div>
                        <div class="card-body">
                            ${items.length > 0 ? `
                                <table class="data-table">
                                    <thead>
                                        <tr>
                                            <th>Tên sản phẩm</th>
                                            <th>Đã bán</th>
                                            <th>Doanh thu</th>
                                        </tr>
                                    </thead>
                                    <tbody>${rows}</tbody>
                                </table>
                            ` : '<p class="text-muted">Chưa có dữ liệu</p>'}
                        </div>
                    </div>
                `;
                topProductsContainer.innerHTML = html;
            } catch (e) {
                topProductsContainer.innerHTML = '<p class="text-muted">Không thể tải Top sản phẩm</p>';
            }
        }

        function computeRange(view, year, month) {
            if (view === 'month') {
                const start = new Date(Number(year), 0, 1);
                const end = new Date(Number(year), 11, 31, 23, 59, 59, 999);
                return { start: start.toISOString(), end: end.toISOString() };
            } else {
                const start = new Date(Number(year), Number(month)-1, 1);
                const end = new Date(Number(year), Number(month), 0, 23, 59, 59, 999);
                return { start: start.toISOString(), end: end.toISOString() };
            }
        }

        async function renderTopCustomers(view, year, month) {
            const { start, end } = computeRange(view, year, month);
            try {
                const res = await OrdersAPI.getTopCustomers({ limit: 5, status: 'completed', start, end });
                let list = [];
                const d = res.data;
                if (Array.isArray(d)) list = d;
                else if (Array.isArray(d?.customers)) list = d.customers;
                else if (Array.isArray(d?.data)) list = d.data;
                else if (Array.isArray(res.customers)) list = res.customers;
                else if (d && typeof d === 'object') {
                    const firstArray = Object.values(d).find(v => Array.isArray(v));
                    list = firstArray || [];
                }
                const rowsHtml = list.map(item => {
                    const user = item.user || {};
                    const name = user.name || item.name || 'N/A';
                    const contact = user.phone || user.email || item.email || 'N/A';
                    const orderCount = item.orderCount || item.totalOrders || 0;
                    const totalSpend = item.totalSpend || 0;
                    const lastOrder = item.lastOrder ? new Date(item.lastOrder).toLocaleDateString('vi-VN') : 'N/A';
                    return `
                        <tr>
                            <td>${name}</td>
                            <td>${contact}</td>
                            <td>${orderCount}</td>
                            <td>${formatCurrency(totalSpend)}</td>
                            <td>${lastOrder}</td>
                        </tr>
                    `;
                }).join('');

                const topCustomersHtml = `
                    <div class="card">
                        <div class="card-header">
                            <h3>Top khách hàng</h3>
                        </div>
                        <div class="card-body">
                            ${list.length > 0 ? `
                                <table class="data-table">
                                    <thead>
                                        <tr>
                                            <th>Khách hàng</th>
                                            <th>Liên hệ</th>
                                            <th>Đơn hàng</th>
                                            <th>Chi tiêu</th>
                                            <th>Gần nhất</th>
                                        </tr>
                                    </thead>
                                    <tbody>${rowsHtml}</tbody>
                                </table>
                            ` : '<p class="text-muted">Chưa có dữ liệu</p>'}
                        </div>
                    </div>
                `;

                const topCustomersContainer = document.getElementById('topCustomersContainer');
                if (topCustomersContainer) topCustomersContainer.innerHTML = topCustomersHtml;
            } catch (e) {
                const topCustomersContainer = document.getElementById('topCustomersContainer');
                if (topCustomersContainer) topCustomersContainer.innerHTML = '<p class="text-muted">Không thể tải Top khách hàng</p>';
            }
        }

        if (viewSelect && yearSelect && monthSelect) {
            renderTopCustomers(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value));
            renderTopProducts(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value));
            viewSelect.addEventListener('change', () => {
                renderTopCustomers(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value));
                renderTopProducts(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value));
            });
            yearSelect.addEventListener('change', () => {
                renderTopCustomers(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value));
                renderTopProducts(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value));
            });
            monthSelect.addEventListener('change', () => {
                renderTopCustomers(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value));
                renderTopProducts(viewSelect.value, Number(yearSelect.value), Number(monthSelect.value));
            });
        } else {
            renderTopCustomers('month', new Date().getFullYear(), new Date().getMonth()+1);
            renderTopProducts('month', new Date().getFullYear(), new Date().getMonth()+1);
        }

        const kpiEl = document.getElementById('statsKPI');
        if (kpiEl) {
            const totalRevenue = deliveredOrders.reduce((s,o)=>s+(o.totalAmount||o.total||0),0);
            const totalOrders = deliveredOrders.length;
            const uniqueCustomers = new Set(deliveredOrders.map(o => o.user?._id || o.userId || o.user?.email)).size;
            const avgOrder = totalOrders ? Math.round(totalRevenue/totalOrders) : 0;
            kpiEl.innerHTML = `
                <div class="stat-card stat-info">
                    <div class="stat-icon"><i class="fas fa-dollar-sign"></i></div>
                    <div class="stat-info"><h3>${formatCurrency(totalRevenue)}</h3><p>Tổng doanh thu</p></div>
                </div>
                <div class="stat-card stat-success">
                    <div class="stat-icon"><i class="fas fa-shopping-cart"></i></div>
                    <div class="stat-info"><h3>${totalOrders}</h3><p>Đơn hàng đã giao</p></div>
                </div>
                <div class="stat-card stat-warning">
                    <div class="stat-icon"><i class="fas fa-users"></i></div>
                    <div class="stat-info"><h3>${uniqueCustomers}</h3><p>Khách hàng</p></div>
                </div>
                <div class="stat-card stat-primary">
                    <div class="stat-icon"><i class="fas fa-receipt"></i></div>
                    <div class="stat-info"><h3>${formatCurrency(avgOrder)}</h3><p>Giá trị TB/đơn</p></div>
                </div>
            `;
        }
    } catch (error) {
        console.error('Error loading statistics:', error);
        showNotification('Không thể tải thống kê', 'error');
    }
}

// ===== Modals =====
function setupModals() {
    // Close modal on background click
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal(this.id);
            }
        });
    });
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.add('active');
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.classList.remove('active');
    }
}

// ===== Utilities =====
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function getStatusText(status) {
    const statusMap = {
        'pending': 'Chờ xác nhận',
        'confirmed': 'Đã xác nhận',
        'shipping': 'Đang giao',
        'delivered': 'Đã giao',
        'completed': 'Đã hoàn tất',
        'cancelled': 'Đã hủy'
    };
    return statusMap[status] || status;
}

function isOrderCompleted(status) {
    return status === 'delivered' || status === 'completed';
}

// ===== Pagination =====
function renderPagination(type, totalPages, currentPageNum) {
    if (totalPages <= 1) {
        // Remove existing pagination if exists
        const existingPagination = document.getElementById(`${type}Pagination`);
        if (existingPagination) {
            existingPagination.remove();
        }
        return;
    }

    let paginationContainer = document.getElementById(`${type}Pagination`);
    if (!paginationContainer) {
        paginationContainer = document.createElement('div');
        paginationContainer.id = `${type}Pagination`;
        paginationContainer.className = 'pagination-container';
        
        // Find the table and insert pagination after it
        const table = document.querySelector(`#${type === 'products' ? 'products' : type === 'orders' ? 'orders' : 'users'}TableBody`)?.closest('.table-responsive')?.parentElement;
        if (table) {
            table.appendChild(paginationContainer);
        }
    }

    let paginationHTML = '<div class="pagination">';
    
    // Previous button
    paginationHTML += `<button class="pagination-btn" onclick="changePage('${type}', ${currentPageNum - 1})" ${currentPageNum === 1 ? 'disabled' : ''}>
        <i class="fas fa-chevron-left"></i>
    </button>`;

    // Page numbers
    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= currentPageNum - 1 && i <= currentPageNum + 1)) {
            paginationHTML += `<button class="pagination-btn ${i === currentPageNum ? 'active' : ''}" onclick="changePage('${type}', ${i})">${i}</button>`;
        } else if (i === currentPageNum - 2 || i === currentPageNum + 2) {
            paginationHTML += `<span class="pagination-dots">...</span>`;
        }
    }

    // Next button
    paginationHTML += `<button class="pagination-btn" onclick="changePage('${type}', ${currentPageNum + 1})" ${currentPageNum === totalPages ? 'disabled' : ''}>
        <i class="fas fa-chevron-right"></i>
    </button>`;

    paginationHTML += '</div>';
    paginationHTML += `<div class="pagination-info">Trang ${currentPageNum} / ${totalPages}</div>`;

    paginationContainer.innerHTML = paginationHTML;
}

function changePage(type, page) {
    if (type === 'products') {
        currentProductPage = page;
        renderProducts(allProducts);
    } else if (type === 'orders') {
        currentOrderPage = page;
        renderOrders(allOrders);
    } else if (type === 'users') {
        currentUserPage = page;
        renderUsers(allUsers);
    }
}

function showNotification(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) {
        // Fallback to alert if container doesn't exist
        alert(message);
        return;
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    const icons = {
        success: 'fa-check-circle',
        error: 'fa-exclamation-circle',
        warning: 'fa-exclamation-triangle',
        info: 'fa-info-circle'
    };
    
    toast.innerHTML = `
        <div class="toast-icon">
            <i class="fas ${icons[type] || icons.info}"></i>
        </div>
        <div class="toast-message">${message}</div>
        <button class="toast-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    container.appendChild(toast);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (toast.parentElement) {
            toast.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }
    }, 5000);
}

