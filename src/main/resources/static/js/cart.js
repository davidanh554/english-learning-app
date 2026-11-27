    let currentCart = null;
    let selectedProductIds = new Set();
    let hasInitializedSelection = false;

    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
    }

    // Tải và hiển thị giỏ hàng
    function renderCart() {
        fetch('/api/cart')
            .then(res => res.json())
            .then(cart => {
                currentCart = cart;
                const container = document.getElementById('cart-content');
                const items = cart.itemList || [];

                // Nếu giỏ hàng trống (Lab 04 trang 5)
                if (items.length === 0) {
                    selectedProductIds.clear();
                    hasInitializedSelection = false;
                    container.innerHTML = `
                        <div class="card summary-card p-5 text-center">
                            <div class="display-1 text-muted mb-3"><i class="bi bi-cart-x"></i></div>
                            <h4 class="fw-bold text-dark">Giỏ hàng của bạn đang trống!</h4>
                            <p class="text-muted">Bạn chưa chọn sản phẩm hoặc gói học nào vào giỏ.</p>
                            <div class="mt-3">
                                <a href="/shop.html" class="btn btn-primary rounded-pill px-4 py-2 fw-bold">
                                    <i class="bi bi-bag-plus-fill me-1"></i> Khám phá Cửa Hàng Ngay
                                </a>
                            </div>
                        </div>
                    `;
                    updateNavCart(0);
                    return;
                }

                updateNavCart(cart.totalQuantity);

                // Mặc định chọn tất cả sản phẩm trong lần tải đầu
                if (!hasInitializedSelection) {
                    selectedProductIds.clear();
                    items.forEach(i => selectedProductIds.add(i.productId));
                    hasInitializedSelection = true;
                } else {
                    // Loại bỏ ID sản phẩm không còn tồn tại trong giỏ
                    const currentIds = new Set(items.map(i => i.productId));
                    for (let id of selectedProductIds) {
                        if (!currentIds.has(id)) selectedProductIds.delete(id);
                    }
                }

                // Hiển thị bảng chi tiết các món trong giỏ kèm CHECKBOX
                let tableRows = items.map(item => {
                    const isChecked = selectedProductIds.has(item.productId) ? 'checked' : '';
                    return `
                        <tr id="row-${item.productId}">
                            <td class="text-center" style="width: 50px;">
                                <input type="checkbox" class="form-check-input item-checkbox" 
                                       id="check-${item.productId}" 
                                       data-id="${item.productId}" 
                                       data-price="${item.price}" 
                                       data-qty="${item.quantity}"
                                       data-subtotal="${item.subtotal}"
                                       onchange="onItemCheckboxChange()" 
                                       ${isChecked}
                                       title="Tích chọn để mua sản phẩm này">
                            </td>
                            <td>
                                <div class="d-flex align-items-center gap-3">
                                    <a href="/product-detail.html?id=${item.productId}">
                                        <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=100'}" class="product-thumb" alt="${item.name}">
                                    </a>
                                    <div>
                                        <span class="badge bg-light text-secondary border mb-1">${item.category || 'Học liệu'}</span>
                                        <h6 class="fw-bold mb-0">
                                            <a href="/product-detail.html?id=${item.productId}" class="text-dark text-decoration-none" title="Xem chi tiết">
                                                ${item.name}
                                            </a>
                                        </h6>
                                    </div>
                                </div>
                            </td>
                            <td class="fw-semibold text-secondary">${formatCurrency(item.price)}</td>
                            <td>
                                <div class="d-flex align-items-center gap-1">
                                    <button class="qty-step-btn" onclick="stepQty(${item.productId}, -1)" title="Giảm 1">
                                        <i class="bi bi-dash"></i>
                                    </button>
                                    <input type="number" min="1" max="99" value="${item.quantity}" 
                                           id="qty-${item.productId}" class="form-control qty-input" 
                                           onchange="updateQuantity(${item.productId})">
                                    <button class="qty-step-btn" onclick="stepQty(${item.productId}, 1)" title="Tăng 1">
                                        <i class="bi bi-plus"></i>
                                    </button>
                                </div>
                            </td>
                            <td class="fw-bold text-success">${formatCurrency(item.subtotal)}</td>
                            <td class="text-center">
                                <button class="btn btn-sm btn-outline-danger rounded-pill px-3" 
                                        onclick="removeItem(${item.productId})" title="Xóa khỏi giỏ">
                                    <i class="bi bi-trash3-fill"></i>
                                </button>
                            </td>
                        </tr>
                    `;
                }).join('');

                container.innerHTML = `
                    <div class="row g-4">
                        <div class="col-lg-8">
                            <div class="card summary-card overflow-hidden">
                                <div class="table-responsive">
                                    <table class="table cart-table mb-0">
                                        <thead>
                                            <tr>
                                                <th style="width: 50px;" class="text-center">
                                                    <input type="checkbox" id="check-all" class="form-check-input" 
                                                           onchange="toggleSelectAll(this.checked)" 
                                                           title="Chọn tất cả sản phẩm">
                                                </th>
                                                <th>Sản phẩm</th>
                                                <th>Đơn giá</th>
                                                <th>Số lượng</th>
                                                <th>Thành tiền</th>
                                                <th class="text-center">Hành động</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            ${tableRows}
                                        </tbody>
                                    </table>
                                </div>
                                <div class="card-footer bg-white d-flex flex-wrap justify-content-between align-items-center p-3 border-top gap-2">
                                    <div class="d-flex align-items-center gap-3">
                                        <div class="form-check mb-0">
                                            <input class="form-check-input" type="checkbox" id="check-all-footer" onchange="toggleSelectAll(this.checked)">
                                            <label class="form-check-label fw-bold small text-dark" for="check-all-footer">
                                                Chọn tất cả (<span id="total-items-badge">${items.length}</span> món)
                                            </label>
                                        </div>
                                        <button class="btn btn-outline-secondary btn-sm rounded-pill fw-semibold" onclick="removeSelectedItems()">
                                            <i class="bi bi-trash3 me-1"></i> Xóa mục đã chọn
                                        </button>
                                        <button class="btn btn-outline-danger btn-sm rounded-pill fw-semibold" onclick="clearCart()">
                                            <i class="bi bi-x-circle me-1"></i> Xóa toàn bộ
                                        </button>
                                    </div>
                                    <span class="text-muted small">
                                        Đã chọn: <strong class="text-primary fs-6" id="selected-summary-count">0</strong> sản phẩm
                                    </span>
                                </div>
                            </div>
                        </div>

                        <!-- CỘT TỔNG TIỀN VÀ ĐẶT HÀNG (LAB 04) -->
                        <div class="col-lg-4">
                            <div class="card summary-card p-4">
                                <h5 class="fw-bold text-dark mb-3">Tóm tắt thanh toán</h5>
                                <div class="d-flex justify-content-between text-secondary mb-2">
                                    <span>Mặt hàng đã chọn:</span>
                                    <span class="fw-bold text-primary" id="summary-selected-qty">0 món</span>
                                </div>
                                <div class="d-flex justify-content-between text-secondary mb-2">
                                    <span>Tạm tính:</span>
                                    <span class="fw-semibold text-dark" id="summary-subtotal">0 ₫</span>
                                </div>
                                <div class="d-flex justify-content-between text-secondary mb-3">
                                    <span>Phí vận chuyển:</span>
                                    <span class="text-muted small fst-italic">Tính ở bước Checkout (Lab 05)</span>
                                </div>
                                <hr>
                                <div class="d-flex justify-content-between align-items-center mb-4">
                                    <span class="fw-bold fs-5 text-dark">Tổng cộng:</span>
                                    <span class="fw-extrabold fs-4 text-success" id="summary-total">0 ₫</span>
                                </div>

                                <button id="btn-checkout" class="btn btn-success btn-lg w-100 rounded-pill fw-bold shadow-sm py-3" onclick="proceedToCheckout()">
                                    Tiến Hành Đặt Hàng <i class="bi bi-arrow-right ms-1"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                `;

                // Tính toán lại tổng tiền dựa trên các checkbox được chọn
                recalculateSelectedTotal();
            })
            .catch(err => {
                document.getElementById('cart-content').innerHTML = `
                    <div class="alert alert-danger">Không thể tải dữ liệu giỏ hàng: ${err.message}</div>
                `;
            });
    }

    // TÍNH TOÁN LẠI TỔNG TIỀN DỰA THEO CÁC CHECKBOX ĐƯỢC CHỌN
    function recalculateSelectedTotal() {
        const checkboxes = document.querySelectorAll('.item-checkbox');
        let selectedCount = 0;
        let selectedTotalQty = 0;
        let selectedTotalAmount = 0;

        checkboxes.forEach(cb => {
            const pid = parseInt(cb.getAttribute('data-id'));
            if (cb.checked) {
                selectedProductIds.add(pid);
                selectedCount++;
                const qty = parseInt(cb.getAttribute('data-qty')) || 0;
                const subtotal = parseFloat(cb.getAttribute('data-subtotal')) || 0;
                selectedTotalQty += qty;
                selectedTotalAmount += subtotal;
            } else {
                selectedProductIds.delete(pid);
            }
        });

        // Cập nhật số lượng hiển thị
        const selectedCountEl = document.getElementById('selected-summary-count');
        if (selectedCountEl) selectedCountEl.innerText = selectedCount;

        const summaryQtyEl = document.getElementById('summary-selected-qty');
        if (summaryQtyEl) summaryQtyEl.innerText = `${selectedTotalQty} món (${selectedCount} loại)`;

        const subtotalEl = document.getElementById('summary-subtotal');
        if (subtotalEl) subtotalEl.innerText = formatCurrency(selectedTotalAmount);

        const totalEl = document.getElementById('summary-total');
        if (totalEl) totalEl.innerText = formatCurrency(selectedTotalAmount);

        // Cập nhật trạng thái checkbox "Chọn tất cả" ở Header & Footer
        const checkAllHeader = document.getElementById('check-all');
        const checkAllFooter = document.getElementById('check-all-footer');
        const isAllChecked = checkboxes.length > 0 && selectedCount === checkboxes.length;
        if (checkAllHeader) checkAllHeader.checked = isAllChecked;
        if (checkAllFooter) checkAllFooter.checked = isAllChecked;

        // Trạng thái nút Tiến Hành Đặt Hàng
        const btnCheckout = document.getElementById('btn-checkout');
        if (btnCheckout) {
            if (selectedCount === 0) {
                btnCheckout.disabled = true;
                btnCheckout.className = 'btn btn-secondary btn-lg w-100 rounded-pill fw-bold py-3 shadow-none opacity-75';
                btnCheckout.innerHTML = '<i class="bi bi-exclamation-circle me-1"></i> Chọn sản phẩm để đặt hàng';
            } else {
                btnCheckout.disabled = false;
                btnCheckout.className = 'btn btn-success btn-lg w-100 rounded-pill fw-bold shadow-sm py-3';
                btnCheckout.innerHTML = `Tiến Hành Đặt Hàng (${selectedTotalQty}) <i class="bi bi-arrow-right ms-1"></i>`;
            }
        }
    }

    // CHECKBOX CHỌN TẤT CẢ
    function toggleSelectAll(isChecked) {
        const checkboxes = document.querySelectorAll('.item-checkbox');
        checkboxes.forEach(cb => {
            cb.checked = isChecked;
        });
        recalculateSelectedTotal();
    }

    // KHI THAY ĐỔI CHECKBOX TỪNG DÒNG
    function onItemCheckboxChange() {
        recalculateSelectedTotal();
    }

    // TĂNG GIẢM NHANH BẰNG NÚT + / -
    function stepQty(productId, delta) {
        const input = document.getElementById(`qty-${productId}`);
        if (!input) return;
        let qty = parseInt(input.value) || 1;
        qty += delta;
        if (qty < 1) qty = 1;
        input.value = qty;
        updateQuantity(productId);
    }

    // Cập nhật số lượng qua PUT /api/cart/update
    function updateQuantity(productId) {
        const input = document.getElementById(`qty-${productId}`);
        const qty = parseInt(input.value);

        if (isNaN(qty) || qty <= 0) {
            alert("Số lượng phải từ 1 trở lên!");
            return;
        }

        fetch(`/api/cart/update?productId=${productId}&quantity=${qty}`, { method: 'PUT' })
            .then(res => res.json())
            .then(() => renderCart())
            .catch(err => alert("Lỗi khi cập nhật số lượng: " + err.message));
    }

    // Xóa món qua DELETE /api/cart/remove/{id}
    function removeItem(productId) {
        if (confirm("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?")) {
            fetch(`/api/cart/remove/${productId}`, { method: 'DELETE' })
                .then(res => res.json())
                .then(() => {
                    selectedProductIds.delete(productId);
                    renderCart();
                })
                .catch(err => alert("Lỗi khi xóa sản phẩm: " + err.message));
        }
    }

    // Xóa các mục đã được tích chọn
    function removeSelectedItems() {
        const selectedIds = Array.from(selectedProductIds);
        if (selectedIds.length === 0) {
            alert("Vui lòng tích chọn ít nhất 1 sản phẩm cần xóa!");
            return;
        }

        if (confirm(`Bạn có chắc muốn xóa ${selectedIds.length} sản phẩm đã chọn khỏi giỏ hàng?`)) {
            Promise.all(selectedIds.map(id => fetch(`/api/cart/remove/${id}`, { method: 'DELETE' })))
                .then(() => {
                    selectedProductIds.clear();
                    renderCart();
                })
                .catch(err => alert("Lỗi khi xóa sản phẩm: " + err.message));
        }
    }

    // Xóa toàn bộ qua DELETE /api/cart/clear
    function clearCart() {
        if (confirm("Bạn có chắc muốn làm trống giỏ hàng?")) {
            fetch('/api/cart/clear', { method: 'DELETE' })
                .then(res => res.json())
                .then(() => {
                    selectedProductIds.clear();
                    renderCart();
                })
                .catch(err => alert("Lỗi khi xóa giỏ: " + err.message));
        }
    }

    function updateNavCart(qty) {
        const badge = document.getElementById('nav-cart-badge');
        if (badge) badge.innerText = qty || 0;
    }

    function proceedToCheckout() {
        const selectedIds = Array.from(selectedProductIds);
        if (selectedIds.length === 0) {
            alert("Vui lòng tích chọn ít nhất 1 sản phẩm để tiến hành đặt hàng!");
            return;
        }
        alert(`🎉 Bạn đã chọn ${selectedIds.length} sản phẩm để thanh toán!\n\nBước tiếp theo chúng ta sẽ làm Lab 05 (Tính phí Ship theo địa chỉ nhận hàng) và Lab 06 (Thanh toán MoMo / COD).`);
    }

    renderCart();
