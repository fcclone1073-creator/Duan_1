// Login functionality
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const loginBtn = document.getElementById('loginBtn');
    const errorMessage = document.getElementById('errorMessage');

    // Check if already logged in
    if (isAuthenticated()) {
        window.location.href = 'dashboard.html';
        return;
    }

    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        // Show loading state
        loginBtn.disabled = true;
        loginBtn.querySelector('.btn-text').style.display = 'none';
        loginBtn.querySelector('.btn-loader').style.display = 'inline-block';
        errorMessage.style.display = 'none';

        try {
            const response = await AuthAPI.login(email, password);
            
            // Check if user is admin
            const user = response.data?.user || response.data;
            if (user && user.role === 'admin') {
                // Store token
                if (response.data?.token) {
                    setAuthToken(response.data.token);
                }
                
                // Store user info
                localStorage.setItem('admin_user', JSON.stringify(user));
                
                // Redirect to dashboard
                window.location.href = 'dashboard.html';
            } else {
                throw new Error('Bạn không có quyền truy cập. Chỉ admin mới được đăng nhập.');
            }
        } catch (error) {
            let errorText = error.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại email và mật khẩu.';
            
            // Hiển thị lỗi chi tiết hơn
            if (error.message.includes('Không thể kết nối đến server')) {
                errorText = '❌ Không thể kết nối đến server!\n\nVui lòng:\n1. Kiểm tra backend server đang chạy tại http://localhost:3000\n2. Chạy lệnh: cd sever && npm start\n3. Đảm bảo MongoDB đang chạy';
            }
            
            errorMessage.textContent = errorText;
            errorMessage.style.display = 'block';
            errorMessage.style.whiteSpace = 'pre-line'; // Cho phép xuống dòng
        } finally {
            loginBtn.disabled = false;
            loginBtn.querySelector('.btn-text').style.display = 'inline';
            loginBtn.querySelector('.btn-loader').style.display = 'none';
        }
    });
});

// Toggle password visibility
function togglePassword() {
    const passwordInput = document.getElementById('password');
    const eyeIcon = document.getElementById('eyeIcon');
    
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        eyeIcon.classList.remove('fa-eye');
        eyeIcon.classList.add('fa-eye-slash');
    } else {
        passwordInput.type = 'password';
        eyeIcon.classList.remove('fa-eye-slash');
        eyeIcon.classList.add('fa-eye');
    }
}

