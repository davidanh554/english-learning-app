    let currentProduct = null;

    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }

    // Lấy ID sản phẩm từ query param ?id=...
    function getProductId() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('id');
    }

    // Tải chi tiết sản phẩm từ API GET /api/products/{id}
    function loadProductDetail() {
        const id = getProductId();
        if (!id) {
            showError("Vui lòng cung cấp mã sản phẩm hợp lệ!");
            return;
        }

        fetch(`/api/products/${id}`)
            .then(res => {
                if (!res.ok) throw new Error("Không thể tải thông tin sản phẩm (Mã lỗi: " + res.status + ")");
                return res.json();
            })
            .then(product => {
                currentProduct = product;
                renderProduct(product);
            })
            .catch(err => {
                showError(err.message);
            });
    }

    function renderProduct(p) {
        document.getElementById('loading-state').classList.add('d-none');
        document.getElementById('product-content').classList.remove('d-none');

        // Điền dữ liệu
        document.title = `${p.name} - EnglishPro Cửa Hàng`;
        document.getElementById('breadcrumb-product-name').innerText = p.name;
        document.getElementById('product-name').innerText = p.name;
        document.getElementById('product-price').innerText = formatCurrency(p.price);
        document.getElementById('product-desc').innerText = p.description || 'Không có mô tả chi tiết.';
        document.getElementById('product-img').src = p.imageUrl || 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=800&auto=format&fit=crop&q=80';
        document.getElementById('product-badge-cat').innerText = p.category || 'Học liệu';

        // Badge loại hàng
        const typeBadge = document.getElementById('product-type-badge');
        const shippingTitle = document.getElementById('policy-shipping-title');
        if (p.physical) {
            typeBadge.className = 'badge bg-primary-subtle text-primary fw-bold px-3 py-2 rounded-pill';
            typeBadge.innerHTML = '<i class="bi bi-box-seam me-1"></i>Hàng vật lý đóng gói';
            if (shippingTitle) shippingTitle.innerText = "Giao Toàn Quốc";
        } else {
            typeBadge.className = 'badge bg-warning-subtle text-dark fw-bold px-3 py-2 rounded-pill';
            typeBadge.innerHTML = '<i class="bi bi-lightning-charge-fill me-1"></i>Kích hoạt tức thì';
            if (shippingTitle) shippingTitle.innerText = "Kích Hoạt Ngay";
        }

        // Tình trạng kho
        const stockBadge = document.getElementById('product-stock-badge');
        const stockHint = document.getElementById('stock-hint');
        const stockQty = p.stockQuantity != null ? p.stockQuantity : 100;
        if (stockQty > 0) {
            stockBadge.className = 'badge bg-success-subtle text-success fw-bold px-3 py-2 rounded-pill';
            stockBadge.innerHTML = `<i class="bi bi-check-circle-fill me-1"></i>Còn hàng (${stockQty} sản phẩm)`;
            stockHint.innerText = `Tối đa trong kho: ${stockQty}`;
        } else {
            stockBadge.className = 'badge bg-danger-subtle text-danger fw-bold px-3 py-2 rounded-pill';
            stockBadge.innerHTML = `<i class="bi bi-x-circle-fill me-1"></i>Hết hàng`;
            stockHint.innerText = `Sản phẩm hiện đang tạm hết`;
        }
    }

    function showError(msg) {
        document.getElementById('loading-state').classList.add('d-none');
        document.getElementById('error-state').classList.remove('d-none');
        document.getElementById('error-message').innerText = msg;
    }

    // Thay đổi số lượng mua
    function changeQty(delta) {
        const input = document.getElementById('buy-quantity');
        let val = parseInt(input.value) || 1;
        val += delta;
        const max = currentProduct ? currentProduct.stockQuantity : 999;
        if (val < 1) val = 1;
        if (val > max) val = max;
        input.value = val;
    }

    function validateQty() {
        const input = document.getElementById('buy-quantity');
        let val = parseInt(input.value) || 1;
        const max = currentProduct ? currentProduct.stockQuantity : 999;
        if (val < 1) val = 1;
        if (val > max) val = max;
        input.value = val;
    }

    // 1. NÚT THÊM VÀO GIỎ HÀNG
    function addToCartDetail() {
        if (!currentProduct) return;
        const qty = parseInt(document.getElementById('buy-quantity').value) || 1;

        fetch('/api/cart/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId: currentProduct.id, quantity: qty })
        })
        .then(res => res.json())
        .then(cart => {
            // Cập nhật số lượng trên navbar badge
            const navBadge = document.getElementById('nav-cart-badge');
            if (navBadge) navBadge.innerText = cart.totalQuantity || 0;
            if (cart && cart.itemList) {
                localStorage.setItem('shopping_cart_items', JSON.stringify(cart.itemList));
            }
            showCartToast(currentProduct.name, qty);
        })
        .catch(err => {
            alert("Có lỗi khi thêm vào giỏ hàng: " + err.message);
        });
    }

    // 2. NÚT MUA NGAY: Chuyển hướng thẳng sang trang Thanh toán Checkout (Lab 05)
    function buyNowDetail() {
        if (!currentProduct) return;
        const qty = parseInt(document.getElementById('buy-quantity').value) || 1;

        fetch('/api/cart/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId: currentProduct.id, quantity: qty })
        })
        .then(res => res.json())
        .then(cart => {
            localStorage.setItem('checkout_selected_ids', JSON.stringify([currentProduct.id]));
            if (cart && cart.itemList) {
                localStorage.setItem('shopping_cart_items', JSON.stringify(cart.itemList));
                const selected = cart.itemList.filter(i => i.productId === currentProduct.id);
                localStorage.setItem('checkout_items', JSON.stringify(selected));
            }
            window.location.href = '/checkout.html';
        })
        .catch(err => {
            alert("Có lỗi khi mua ngay: " + err.message);
        });
    }

    let toastTimer;
    function showCartToast(name, qty) {
        const toast = document.getElementById('cart-toast');
        document.getElementById('toast-title').innerText = "Đã thêm vào giỏ hàng thành công!";
        document.getElementById('toast-desc').innerText = `Đã thêm ${qty}x ${name} vào giỏ hàng.`;
        toast.classList.add('show');

        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => {
            toast.classList.remove('show');
        }, 3500);
    }

    // Khởi động
    document.addEventListener("DOMContentLoaded", loadProductDetail);
