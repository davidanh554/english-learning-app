let subtotal = 0;
let shippingFee = 0;
let finalTotal = 0;
let cartItems = [];
let selectedProvinceName = '';
let selectedDistrictName = '';
let selectedWardName = '';

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
}

// 1. TẢI DỮ LIỆU ĐƠN HÀNG TỪ GIỎ HÀNG
function loadCheckoutItems() {
    fetch('/api/cart')
        .then(res => res.json())
        .then(async cart => {
            let rawItems = cart.itemList || [];

            // Nếu Session trên server bị trống (do khởi động lại server hoặc phiên hết hạn)
            // Tự động khôi phục dữ liệu từ localStorage đã lưu ở bước giỏ hàng / Mua ngay
            if (rawItems.length === 0) {
                let savedItems = null;
                const savedCheckoutJson = localStorage.getItem('checkout_items');
                if (savedCheckoutJson) {
                    try { savedItems = JSON.parse(savedCheckoutJson); } catch(e) {}
                }
                if (!savedItems || savedItems.length === 0) {
                    const savedCartJson = localStorage.getItem('shopping_cart_items');
                    if (savedCartJson) {
                        try { savedItems = JSON.parse(savedCartJson); } catch(e) {}
                    }
                }

                // Nếu tìm thấy sản phẩm trong bộ nhớ máy client -> Tự động nạp lại vào Session server!
                if (savedItems && Array.isArray(savedItems) && savedItems.length > 0) {
                    try {
                        for (const itm of savedItems) {
                            await fetch('/api/cart/add', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ productId: itm.productId, quantity: itm.quantity || 1 })
                            });
                        }
                        const resyncRes = await fetch('/api/cart');
                        const resyncCart = await resyncRes.json();
                        rawItems = resyncCart.itemList || savedItems;
                    } catch (syncErr) {
                        console.warn("Không thể đồng bộ giỏ hàng lên server:", syncErr);
                        rawItems = savedItems;
                    }
                }
            }

            // Nếu thực sự không có sản phẩm nào được chọn
            if (rawItems.length === 0) {
                const container = document.getElementById('checkout-items-list');
                if (container) {
                    container.innerHTML = `
                        <div class="alert alert-warning text-center p-3 mb-0 rounded-3">
                            <i class="bi bi-cart-x fs-3 d-block mb-2 text-warning"></i>
                            <div class="fw-bold">Giỏ hàng của bạn đang trống!</div>
                            <small class="text-muted d-block mb-3">Vui lòng chọn sản phẩm trước khi tính phí giao hàng.</small>
                            <a href="/shop.html" class="btn btn-sm btn-primary rounded-pill px-4 fw-bold">
                                <i class="bi bi-shop me-1"></i> Khám phá Cửa Hàng
                            </a>
                        </div>
                    `;
                }
                const btnSubmit = document.getElementById('btn-submit-order');
                if (btnSubmit) {
                    btnSubmit.disabled = true;
                    btnSubmit.className = 'btn btn-secondary btn-lg w-100 rounded-pill fw-bold py-3 shadow-none opacity-50';
                    btnSubmit.innerHTML = '<i class="bi bi-exclamation-circle me-1"></i> Giỏ hàng trống';
                }
                return;
            }

            // Kích hoạt lại nút Đặt hàng nếu đã có sản phẩm
            const btnSubmit = document.getElementById('btn-submit-order');
            if (btnSubmit) {
                btnSubmit.disabled = false;
                btnSubmit.className = 'btn btn-warning btn-lg w-100 rounded-pill fw-bold text-dark py-3 shadow-sm';
                btnSubmit.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i> Xác Nhận Đặt Hàng';
            }

            // Lấy danh sách ID đã chọn từ localStorage (nếu có)
            const selectedIdsJson = localStorage.getItem('checkout_selected_ids');
            let selectedIds = null;
            if (selectedIdsJson) {
                try { selectedIds = JSON.parse(selectedIdsJson); } catch (e) {}
            }

            if (selectedIds && Array.isArray(selectedIds) && selectedIds.length > 0) {
                cartItems = rawItems.filter(i => selectedIds.includes(i.productId));
            } else {
                cartItems = rawItems;
            }

            if (cartItems.length === 0) cartItems = rawItems;

            // Render danh sách món hàng
            subtotal = 0;
            const container = document.getElementById('checkout-items-list');
            container.innerHTML = cartItems.map(item => {
                subtotal += item.subtotal;
                return `
                    <div class="d-flex align-items-center justify-content-between mb-3 pb-2 border-bottom">
                        <div class="d-flex align-items-center gap-2">
                            <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=100'}" class="product-mini-thumb" alt="${item.name}">
                            <div>
                                <h6 class="fw-bold mb-0 text-dark small" style="max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                    ${item.name}
                                </h6>
                                <small class="text-muted">SL: <strong>${item.quantity}</strong> x ${formatCurrency(item.price)}</small>
                            </div>
                        </div>
                        <span class="fw-bold text-success small">${formatCurrency(item.subtotal)}</span>
                    </div>
                `;
            }).join('');

            document.getElementById('summary-subtotal').innerText = formatCurrency(subtotal);
            updateTotals();
        })
        .catch(err => {
            console.error("Lỗi tải giỏ hàng:", err);
        });
}

// 2. TẢI DANH MỤC HÀNH CHÍNH GHN (TỈNH -> HUYỆN -> XÃ)
function loadProvinces() {
    const provinceSelect = document.getElementById('province-select');
    provinceSelect.innerHTML = '<option value="">-- Đang tải Tỉnh/Thành... --</option>';

    fetch('/api/shipping/provinces')
        .then(res => res.json())
        .then(res => {
            if (res.data && Array.isArray(res.data)) {
                let options = '<option value="">-- Chọn Tỉnh / Thành phố --</option>';
                res.data.forEach(p => {
                    options += `<option value="${p.ProvinceID}" data-name="${p.ProvinceName}">${p.ProvinceName}</option>`;
                });
                provinceSelect.innerHTML = options;
            } else {
                provinceSelect.innerHTML = '<option value="">-- Không thể tải danh sách tỉnh --</option>';
            }
        })
        .catch(err => {
            console.error("Lỗi load tỉnh thành:", err);
            provinceSelect.innerHTML = '<option value="">-- Lỗi kết nối GHN --</option>';
        });
}

function initLocationListeners() {
    const provinceSelect = document.getElementById('province-select');
    const districtSelect = document.getElementById('district-select');
    const wardSelect = document.getElementById('ward-select');

    // Khi CHỌN TỈNH -> Tải Quận / Huyện tương ứng
    provinceSelect.addEventListener('change', function() {
        const provinceId = this.value;
        const selectedOption = this.options[this.selectedIndex];
        selectedProvinceName = selectedOption ? (selectedOption.getAttribute('data-name') || '') : '';

        districtSelect.innerHTML = '<option value="">-- Đang tải Quận/Huyện... --</option>';
        districtSelect.disabled = true;
        wardSelect.innerHTML = '<option value="">-- Chọn Quận/Huyện trước --</option>';
        wardSelect.disabled = true;
        shippingFee = 0;
        updateTotals();

        if (!provinceId) return;

        fetch(`/api/shipping/districts/${provinceId}`)
            .then(res => res.json())
            .then(res => {
                if (res.data && Array.isArray(res.data)) {
                    let options = '<option value="">-- Chọn Quận / Huyện --</option>';
                    res.data.forEach(d => {
                        options += `<option value="${d.DistrictID}" data-name="${d.DistrictName}">${d.DistrictName}</option>`;
                    });
                    districtSelect.innerHTML = options;
                    districtSelect.disabled = false;
                } else {
                    districtSelect.innerHTML = '<option value="">-- Không tải được quận huyện --</option>';
                }
            })
            .catch(err => {
                console.error("Lỗi tải quận huyện:", err);
                districtSelect.innerHTML = '<option value="">-- Lỗi kết nối GHN --</option>';
            });
    });

    // Khi CHỌN QUẬN -> Tải Phường / Xã tương ứng
    districtSelect.addEventListener('change', function() {
        const districtId = this.value;
        const selectedOption = this.options[this.selectedIndex];
        selectedDistrictName = selectedOption ? (selectedOption.getAttribute('data-name') || '') : '';

        wardSelect.innerHTML = '<option value="">-- Đang tải Phường/Xã... --</option>';
        wardSelect.disabled = true;
        shippingFee = 0;
        updateTotals();

        if (!districtId) return;

        fetch(`/api/shipping/wards/${districtId}`)
            .then(res => res.json())
            .then(res => {
                if (res.data && Array.isArray(res.data)) {
                    let options = '<option value="">-- Chọn Phường / Xã --</option>';
                    res.data.forEach(w => {
                        options += `<option value="${w.WardCode}" data-name="${w.WardName}">${w.WardName}</option>`;
                    });
                    wardSelect.innerHTML = options;
                    wardSelect.disabled = false;
                } else {
                    wardSelect.innerHTML = '<option value="">-- Không tải được phường xã --</option>';
                }
            })
            .catch(err => {
                console.error("Lỗi tải phường xã:", err);
                wardSelect.innerHTML = '<option value="">-- Lỗi kết nối GHN --</option>';
            });
    });

    // Khi CHỌN PHƯỜNG/XÃ -> Kích hoạt API tính cước GHN thực tế!
    wardSelect.addEventListener('change', function() {
        const wardCode = this.value;
        const selectedOption = this.options[this.selectedIndex];
        selectedWardName = selectedOption ? (selectedOption.getAttribute('data-name') || '') : '';

        const districtId = districtSelect.value;
        if (!wardCode || !districtId) return;

        const feeElement = document.getElementById('summary-shipping-fee');
        feeElement.innerHTML = '<span class="spinner-border spinner-border-sm text-warning me-1"></span> Đang tính cước GHN...';

        const totalWeight = cartItems.reduce((acc, itm) => acc + (300 * (itm.quantity || 1)), 0);

        fetch('/api/shipping/calculate-fee', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                toDistrictId: parseInt(districtId),
                toWardCode: wardCode,
                weight: totalWeight > 0 ? totalWeight : 300
            })
        })
        .then(res => res.json())
        .then(res => {
            shippingFee = res.shippingFee || 0;
            feeElement.className = 'fw-bold text-dark';
            feeElement.innerText = formatCurrency(shippingFee);
            updateTotals();
        })
        .catch(err => {
            console.error("Lỗi tính cước GHN:", err);
            shippingFee = 30000;
            feeElement.innerText = formatCurrency(shippingFee) + ' (Ước tính)';
            updateTotals();
        });
    });
}

function updateTotals() {
    finalTotal = subtotal + shippingFee;
    document.getElementById('summary-final-total').innerText = formatCurrency(finalTotal);
}

// 3. XỬ LÝ SUBMIT ĐẶT HÀNG (LAB 05)
function submitOrder() {
    const customerName = document.getElementById('customer-name').value.trim();
    const phone = document.getElementById('customer-phone').value.trim();
    const address = document.getElementById('customer-address').value.trim();

    const provinceSelect = document.getElementById('province-select');
    const districtSelect = document.getElementById('district-select');
    const wardSelect = document.getElementById('ward-select');

    if (!customerName || !phone || !address || !provinceSelect.value || !districtSelect.value || !wardSelect.value) {
        alert("Vui lòng điền đầy đủ thông tin nhận hàng và chọn địa chỉ GHN!");
        return;
    }

    const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
    const selectedProductIds = cartItems.map(i => i.productId);
    const items = cartItems.map(i => ({ productId: i.productId, quantity: i.quantity || 1 }));

    const orderData = {
        customerName: customerName,
        phone: phone,
        address: address,
        provinceId: parseInt(provinceSelect.value),
        provinceName: selectedProvinceName,
        toDistrictId: parseInt(districtSelect.value),
        districtName: selectedDistrictName,
        toWardCode: wardSelect.value,
        wardName: selectedWardName,
        shippingFee: shippingFee,
        paymentMethod: paymentMethod,
        selectedProductIds: selectedProductIds,
        items: items
    };

    const btn = document.getElementById('btn-submit-order');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span> Đang tạo đơn hàng...';

    fetch('/api/orders/checkout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(orderData)
    })
    .then(async res => {
        if (!res.ok) {
            const text = await res.text();
            let msg = "Lỗi khi tạo đơn hàng!";
            try {
                const json = JSON.parse(text);
                msg = json.message || msg;
            } catch(e) {
                if (text) msg = text;
            }
            throw new Error(msg);
        }
        return res.json();
    })
    .then(order => {
        // Dọn dẹp dữ liệu giỏ hàng sau khi đặt thành công
        localStorage.removeItem('checkout_selected_ids');
        localStorage.removeItem('checkout_items');
        localStorage.removeItem('shopping_cart_items');
        // Chuyển hướng sang trang thông báo đặt hàng thành công
        window.location.href = `/order-success.html?orderId=${order.id}`;
    })
    .catch(err => {
        alert("Có lỗi khi tạo đơn hàng: " + err.message);
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i> Xác Nhận Đặt Hàng';
    });
}

// Khởi chạy khi DOM sẵn sàng
document.addEventListener('DOMContentLoaded', () => {
    // Tự động điền tên nếu người dùng đã đăng nhập
    try {
        const currentUser = JSON.parse(localStorage.getItem('currentUser'));
        if (currentUser) {
            const nameInput = document.getElementById('customer-name');
            if (nameInput && !nameInput.value) nameInput.value = currentUser.fullName || currentUser.username || '';
        }
    } catch(e) {}

    loadCheckoutItems();
    loadProvinces();
    initLocationListeners();
});
