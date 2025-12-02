// Main Application Logic
let currentPage = 'dashboard';
let allProducts = [];
let allOrders = [];
let allUsers = [];
let allCategories = [];

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
        return;
    }

    tbody.innerHTML = products.map(product => {
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
                image: document.getElementById('productImage').value || undefined,
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
        return;
    }

    tbody.innerHTML = orders.map(order => {
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
}

function filterOrders() {
    const status = document.getElementById('orderStatusFilter').value;
    let filtered = allOrders;
    
    if (status) {
        filtered = allOrders.filter(o => o.status === status);
    }
    
    renderOrders(filtered);
}

function viewOrder(id) {
    // TODO: Implement order detail view
    alert('Xem chi tiết đơn hàng: ' + id);
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
        allUsers = Array.isArray(response.data) ? response.data : (response.data?.users || []);
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
        return;
    }

    tbody.innerHTML = users.map(user => {
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
                        <button class="action-btn action-btn-delete" onclick="deleteUser('${user._id}')">
                            <i class="fas fa-trash"></i> Xóa
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function openAddUserModal() {
    // TODO: Implement add user modal
    alert('Chức năng thêm người dùng đang được phát triển');
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
});

// ===== Statistics =====
function loadStatistics() {
    // TODO: Implement statistics charts
    console.log('Loading statistics...');
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
        'cancelled': 'Đã hủy'
    };
    return statusMap[status] || status;
}

function showNotification(message, type = 'info') {
    // Simple alert for now, can be enhanced with toast notifications
    alert(message);
}

