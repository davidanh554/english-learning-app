    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }

    // Tải danh sách sản phẩm
    function loadProducts() {
        fetch('/api/products')
            .then(res => res.json())
            .then(products => {
                const grid = document.getElementById('products-grid');
                if (products.length === 0) {
                    grid.innerHTML = '<div class="col-12 text-center py-5 text-muted">Chưa có sản phẩm nào!</div>';
                    return;
                }

                grid.innerHTML = products.map(p => {
                    const badgeClass = p.physical ? 'text-primary' : 'text-danger';
                    const typeIcon = p.physical ? '<i class="bi bi-box-seam me-1"></i>Hàng vật lý' : '<i class="bi bi-lightning-charge-fill me-1"></i>Kích hoạt tức thì';

                    return `
                        <div class="col-md-6 col-lg-3">
                            <div class="product-card position-relative">
                                <span class="badge-category ${badgeClass}">
                                    ${p.category || 'Học liệu'}
                                </span>
                                <div class="product-img-wrapper">
                                    <a href="/product-detail.html?id=${p.id}" title="Bấm xem chi tiết ${p.name}">
                                        <img src="${p.imageUrl}" alt="${p.name}" class="product-img" onerror="this.src='https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=500&auto=format&fit=crop&q=60'">
                                    </a>
                                </div>
                                <div class="p-3 d-flex flex-column flex-grow-1">
                                    <small class="text-muted mb-1">${typeIcon}</small>
                                    <h5 class="fw-bold mb-2" style="font-size: 1.05rem; min-height: 48px;">
                                        <a href="/product-detail.html?id=${p.id}" class="text-dark text-decoration-none" title="Xem chi tiết">
                                            ${p.name}
                                        </a>
                                    </h5>
                                    <p class="text-muted small mb-3 flex-grow-1" style="display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                                        ${p.description}
                                    </p>
                                    <div class="d-flex justify-content-between align-items-center mt-auto pt-2 border-top">
                                        <div class="price-text">${formatCurrency(p.price)}</div>
                                        <small class="text-muted">${p.stockQuantity > 0 ? '<span class="text-success"><i class="bi bi-check-circle me-1"></i>Còn hàng</span>' : '<span class="text-danger">Hết hàng</span>'}</small>
                                    </div>
                                    <div class="row g-2 mt-3">
                                        <div class="col-6">
                                            <button class="btn btn-outline-primary w-100 py-2 fw-bold rounded-pill text-nowrap btn-sm" onclick="addToCart(${p.id}, '${p.name.replace(/'/g, "\\'")}')" title="Thêm vào giỏ hàng">
                                                <i class="bi bi-cart-plus me-1"></i>Thêm giỏ
                                            </button>
                                        </div>
                                        <div class="col-6">
                                            <button class="btn btn-warning w-100 py-2 fw-bold rounded-pill text-dark text-nowrap shadow-sm btn-sm" onclick="buyNow(${p.id}, '${p.name.replace(/'/g, "\\'")}')" title="Mua ngay và tới giỏ hàng">
                                                <i class="bi bi-lightning-charge-fill me-1"></i>Mua ngay
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `;
                }).join('');
            })
            .catch(console.error);
    }

    // Thêm vào giỏ hàng qua API POST /api/cart/add
    function addToCart(productId, productName) {
        fetch('/api/cart/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId: productId, quantity: 1 })
        })
        .then(res => res.json())
        .then(cart => {
            updateCartHeader(cart);
            if (cart && cart.itemList) {
                localStorage.setItem('shopping_cart_items', JSON.stringify(cart.itemList));
            }
            showCartToast(productName);
        })
        .catch(err => {
            alert("Có lỗi khi thêm vào giỏ hàng: " + err.message);
        });
    }

    // Mua ngay: Thêm vào giỏ hàng và chuyển hướng thẳng sang trang Thanh toán (Checkout Lab 05)
    function buyNow(productId, productName) {
        fetch('/api/cart/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId: productId, quantity: 1 })
        })
        .then(res => res.json())
        .then(cart => {
            localStorage.setItem('checkout_selected_ids', JSON.stringify([productId]));
            if (cart && cart.itemList) {
                localStorage.setItem('shopping_cart_items', JSON.stringify(cart.itemList));
                const selected = cart.itemList.filter(i => i.productId === productId);
                localStorage.setItem('checkout_items', JSON.stringify(selected));
            }
            window.location.href = '/checkout.html';
        })
        .catch(err => {
            alert("Có lỗi khi mua ngay: " + err.message);
        });
    }

    function updateCartHeader(cart) {
        const totalQty = cart.totalQuantity || 0;
        document.getElementById('cart-total-qty').innerText = totalQty;
        const navBadge = document.getElementById('nav-cart-badge');
        if (navBadge) navBadge.innerText = totalQty;
    }

    let toastTimer;
    function showCartToast(name) {
        const toast = document.getElementById('cart-toast');
        document.getElementById('toast-title').innerText = "Đã thêm vào giỏ hàng!";
        document.getElementById('toast-desc').innerText = name;
        toast.classList.add('show');

        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => {
            toast.classList.remove('show');
        }, 3000);
    }

    loadProducts();
    // Tải số lượng ban đầu của giỏ
    fetch('/api/cart').then(r => r.json()).then(updateCartHeader).catch(()=>{});
