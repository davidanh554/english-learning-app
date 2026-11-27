// TỰ ĐỘNG ĐÍNH KÈM JWT TOKEN VÀO MỌI YÊU CẦU FETCH CỦA NGƯỜI DÙNG
(function() {
    const originalFetch = window.fetch;
    window.fetch = async function(...args) {
        let [resource, config] = args;
        config = config || {};
        if (!config.credentials) {
            config.credentials = 'include';
        }

        try {
            const currentUser = JSON.parse(localStorage.getItem('currentUser') || 'null');
            if (currentUser && currentUser.token) {
                if (!config.headers) {
                    config.headers = {};
                }
                if (config.headers instanceof Headers) {
                    if (!config.headers.has('Authorization')) {
                        config.headers.set('Authorization', 'Bearer ' + currentUser.token);
                    }
                } else if (Array.isArray(config.headers)) {
                    config.headers.push(['Authorization', 'Bearer ' + currentUser.token]);
                } else {
                    if (!config.headers['Authorization']) {
                        config.headers['Authorization'] = 'Bearer ' + currentUser.token;
                    }
                }
            }
        } catch (e) {
            console.error('Lỗi khi đọc token từ localStorage:', e);
        }

        const response = await originalFetch(resource, config);

        // Nếu token hết hạn hoặc không hợp lệ (401)
        if (response.status === 401 && typeof resource === 'string' && !resource.includes('/api/auth/')) {
            console.warn("Phiên đăng nhập đã hết hạn hoặc không hợp lệ!");
            localStorage.removeItem('currentUser');
            if (window.location.pathname.includes('admin.html')) {
                alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");
                window.location.href = '/login.html';
            }
        }

        return response;
    };
})();

function renderHeader() {
    const headerContainer = document.getElementById("header-container");
    if (!headerContainer) return;

    const currentUser = JSON.parse(localStorage.getItem('currentUser') || 'null');
    const isAdmin = currentUser && currentUser.role === 'ADMIN';

    // Tạo nút bấm chuyển sang /admin.html cho tài khoản ADMIN
    let userDisplayHtml = '';
    if (currentUser) {
        if (isAdmin) {
            userDisplayHtml = `
                <a href="/admin.html" class="btn btn-outline-primary btn-sm fw-bold rounded-pill me-2 shadow-sm">
                    <i class="bi bi-shield-lock-fill me-1"></i> Quản trị viên (ADMIN)
                </a>
            `;
        } else {
            userDisplayHtml = `
                <span class="fw-semibold text-secondary me-3">
                    <i class="bi bi-person-circle me-1"></i>${currentUser.username || currentUser.fullName || 'Học viên'}
                </span>
            `;
        }
    }

    const authAreaHtml = currentUser ? `
        <div class="d-flex align-items-center">
            ${userDisplayHtml}
            <button class="btn btn-outline-danger btn-sm rounded-pill px-3" onclick="logout()">
                <i class="bi bi-box-arrow-right me-1"></i> Đăng xuất
            </button>
        </div>
    ` : `
        <a href="/login.html" class="btn btn-primary rounded-pill px-4 fw-semibold">
            <i class="bi bi-box-arrow-in-right me-1"></i> Đăng nhập
        </a>
    `;

    headerContainer.innerHTML = `
        <nav class="navbar navbar-expand-lg bg-white border-bottom shadow-sm sticky-top">
            <div class="container">
                <a class="navbar-brand fw-bold text-primary fs-4" href="/">
                    <i class="bi bi-mortarboard-fill me-2"></i>EnglishPro
                </a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarNav">
                    <ul class="navbar-nav me-auto mb-2 mb-lg-0 align-items-lg-center">
                        <li class="nav-item">
                            <a class="nav-link fw-semibold text-primary" href="/ai-chat.html">
                                <i class="bi bi-robot me-1"></i>AI Tutor
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link fw-semibold text-success" href="/gametuvung/index.html">
                                <i class="bi bi-controller me-1"></i>Game Từ Vựng
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link fw-semibold text-warning" href="/shop.html">
                                <i class="bi bi-bag-check-fill me-1"></i>Cửa Hàng
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link fw-semibold text-dark position-relative" href="/cart.html">
                                <i class="bi bi-cart3 me-1"></i>Giỏ Hàng
                                <span class="badge bg-danger rounded-pill ms-1" id="nav-cart-badge">0</span>
                            </a>
                        </li>
                    </ul>
                    <div>${authAreaHtml}</div>
                </div>
            </div>
        </nav>
    `;

    // Tự động đồng bộ số lượng giỏ hàng trên Navbar
    fetch('/api/cart')
        .then(r => r.json())
        .then(cart => {
            const badge = document.getElementById('nav-cart-badge');
            if (badge) badge.innerText = cart.totalQuantity || 0;
        })
        .catch(() => {});
}

if (document.readyState === 'loading') {
    document.addEventListener("DOMContentLoaded", renderHeader);
} else {
    renderHeader();
}

function logout() {
    localStorage.removeItem('currentUser');
    window.location.href = '/login.html';
}