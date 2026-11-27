function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
}

function loadOrderDetail() {
    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('orderId');

    if (!orderId) {
        document.getElementById('order-loading').innerHTML = `
            <div class="alert alert-warning text-center rounded-3">
                <i class="bi bi-exclamation-triangle fs-3 d-block mb-2 text-warning"></i>
                <div class="fw-bold">Không tìm thấy mã đơn hàng!</div>
                <small class="text-muted d-block mb-3">Vui lòng kiểm tra lại liên kết hoặc quay lại cửa hàng.</small>
                <a href="/shop.html" class="btn btn-sm btn-primary rounded-pill px-4 fw-bold">Về Cửa Hàng</a>
            </div>
        `;
        return;
    }

    fetch(`/api/orders/${orderId}`)
        .then(res => {
            if (!res.ok) throw new Error("Không thể tải thông tin đơn hàng!");
            return res.json();
        })
        .then(order => {
            document.getElementById('order-loading').classList.add('d-none');
            document.getElementById('order-details').classList.remove('d-none');

            document.getElementById('order-id').innerText = '#' + order.id;
            document.getElementById('order-ghn-code').innerText = order.ghnOrderCode || 'Đang cấp mã...';
            document.getElementById('order-receiver').innerText = order.customerName || 'Khách hàng';
            document.getElementById('order-phone').innerText = order.phone || '';

            const fullAddress = [order.address, order.wardName, order.districtName, order.provinceName]
                .filter(Boolean)
                .join(', ');
            document.getElementById('order-address').innerText = fullAddress || 'Chưa cập nhật';

            document.getElementById('order-subtotal').innerText = formatCurrency(order.subtotal);
            document.getElementById('order-shipping-fee').innerText = formatCurrency(order.ghnTotalFee);
            document.getElementById('order-total-price').innerText = formatCurrency(order.totalPrice);

            const statusEl = document.getElementById('order-shipping-status');
            if (statusEl) {
                if (order.shippingStatus === 'READY_TO_PICK') {
                    statusEl.innerText = 'Sẵn sàng lấy hàng (GHN)';
                    statusEl.className = 'badge bg-success-subtle text-success fw-bold';
                } else if (order.shippingStatus === 'DELIVERING') {
                    statusEl.innerText = 'Đang giao hàng';
                    statusEl.className = 'badge bg-primary-subtle text-primary fw-bold';
                } else if (order.shippingStatus === 'DELIVERED') {
                    statusEl.innerText = 'Đã giao thành công';
                    statusEl.className = 'badge bg-success text-white fw-bold';
                } else {
                    statusEl.innerText = order.shippingStatus || 'Đang xử lý';
                }
            }

            // Render danh sách sản phẩm trong đơn
            const items = order.items || [];
            const container = document.getElementById('order-items-list');
            if (items.length === 0) {
                container.innerHTML = '<div class="text-muted small">Chi tiết đơn hàng đã được lưu.</div>';
            } else {
                container.innerHTML = items.map(item => `
                    <div class="d-flex justify-content-between align-items-center mb-2 pb-2 border-bottom">
                        <div>
                            <span class="fw-semibold text-dark small">${item.productName || 'Sản phẩm'}</span>
                            <div class="text-muted small">Số lượng: <strong>${item.quantity}</strong> x ${formatCurrency(item.price)}</div>
                        </div>
                        <span class="fw-bold text-success small">${formatCurrency(item.subtotal)}</span>
                    </div>
                `).join('');
            }
        })
        .catch(err => {
            document.getElementById('order-loading').innerHTML = `
                <div class="alert alert-danger">${err.message}</div>
            `;
        });
}

document.addEventListener('DOMContentLoaded', loadOrderDetail);
