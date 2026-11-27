    let allFolders = [];
    let currentFolderId = null;
    let currentFiles = [];

    document.addEventListener("DOMContentLoaded", () => {
        // Kiểm tra URL xem có truyền folderId không
        const urlParams = new URLSearchParams(window.location.search);
        const fId = urlParams.get('folderId');
        if (fId) {
            loadFolders().then(() => {
                const found = allFolders.find(f => f.id == fId);
                if (found) {
                    openFolder(found.id, found.name, found.description);
                } else {
                    renderFolders();
                }
            });
        } else {
            loadFolders();
        }
    });

    // 1. TẢI DANH SÁCH THƯ MỤC CHỦ ĐỀ
    async function loadFolders() {
        try {
            const res = await fetch('/api/folders');
            if (!res.ok) throw new Error("Không thể tải danh sách thư mục chủ đề");
            allFolders = await res.json();
            renderFolders();
        } catch (err) {
            console.error(err);
            document.getElementById('folder-list').innerHTML = `
                <div class="col-12 text-center py-5 text-danger">
                    <i class="bi bi-exclamation-triangle fs-2 d-block mb-2"></i>
                    <strong>${err.message}</strong>
                </div>
            `;
        }
    }

    // 2. HIỂN THỊ DANH SÁCH THƯ MỤC
    function renderFolders(filteredList = null) {
        const folders = filteredList || allFolders;
        const listEl = document.getElementById('folder-list');

        if (!folders || folders.length === 0) {
            listEl.innerHTML = `
                <div class="col-12 text-center py-5 text-muted">
                    <i class="bi bi-folder-x fs-1 d-block mb-3 text-secondary opacity-50"></i>
                    Chưa có thư mục chủ đề nào được tạo.
                </div>
            `;
            return;
        }

        let html = '';
        folders.forEach(f => {
            const fileCount = f.fileCount || 0;
            const fileBadgeText = fileCount === 1 ? '1 file từ vựng' : `${fileCount} file từ vựng`;
            const color = f.color || '#4f46e5';

            html += `
                <div class="col-md-6 col-lg-4">
                    <div class="card folder-card h-100 p-4" onclick="openFolder(${f.id}, '${escapeJs(f.name)}', '${escapeJs(f.description || '')}')">
                        <div class="d-flex align-items-center justify-content-between mb-3">
                            <div class="folder-icon-wrapper" style="color: ${color};">
                                <i class="bi bi-folder-fill"></i>
                            </div>
                            <span class="badge-count">
                                <i class="bi bi-file-earmark-text me-1"></i>${fileBadgeText}
                            </span>
                        </div>
                        <h4 class="fw-bold text-dark mb-2">${f.name}</h4>
                        <p class="text-muted small mb-4 flex-grow-1">${f.description || 'Chưa có mô tả cho chủ đề này'}</p>
                        <div class="d-flex align-items-center justify-content-between pt-2 border-top">
                            <span class="text-primary fw-semibold small">Mở thư mục học</span>
                            <i class="bi bi-arrow-right-circle-fill fs-5 text-primary"></i>
                        </div>
                    </div>
                </div>
            `;
        });
        listEl.innerHTML = html;
    }

    // 3. MỞ THƯ MỤC -> HIỂN THỊ DANH SÁCH FILE TỪ VỰNG
    async function openFolder(folderId, folderName, folderDesc) {
        currentFolderId = folderId;

        // Cập nhật URL trình duyệt (không reload trang)
        const newUrl = window.location.pathname + `?folderId=${folderId}`;
        window.history.pushState({ folderId: folderId }, '', newUrl);

        // Đổi trạng thái giao diện sang Xem File
        document.getElementById('folder-list').classList.add('d-none');
        document.getElementById('file-list').classList.remove('d-none');
        document.getElementById('breadcrumb-bar').classList.remove('d-none');
        document.getElementById('btn-back-folder').classList.remove('d-none');

        document.getElementById('current-folder-breadcrumb').textContent = folderName;
        document.getElementById('section-title').innerHTML = `<i class="bi bi-folder2-open me-2 text-primary"></i>${folderName}`;
        document.getElementById('section-subtitle').textContent = folderDesc || "Danh sách các file bài học từ vựng trong chủ đề này";

        const fileContainer = document.getElementById('file-list');
        fileContainer.innerHTML = `
            <div class="col-12 text-center py-5 text-muted">
                <div class="spinner-border text-primary me-2" role="status"></div>
                <span>Đang tải các file từ vựng trong chủ đề...</span>
            </div>
        `;

        try {
            const res = await fetch(`/api/topics?folderId=${folderId}`);
            if (!res.ok) throw new Error("Không thể tải danh sách file từ vựng");
            currentFiles = await res.json();
            renderFiles();
        } catch (err) {
            fileContainer.innerHTML = `
                <div class="col-12 text-center py-5 text-danger">
                    <i class="bi bi-exclamation-triangle fs-2 d-block mb-2"></i>
                    <strong>${err.message}</strong>
                </div>
            `;
        }
    }

    // 4. HIỂN THỊ CÁC FILE TỪ VỰNG TRONG FOLDER
    function renderFiles(filteredList = null) {
        const files = filteredList || currentFiles;
        const fileContainer = document.getElementById('file-list');

        if (!files || files.length === 0) {
            fileContainer.innerHTML = `
                <div class="col-12 text-center py-5 text-muted bg-white rounded-4 border p-5">
                    <i class="bi bi-file-earmark-x fs-1 d-block mb-3 text-secondary opacity-50"></i>
                    <h5 class="fw-bold text-dark mb-1">Chưa có file từ vựng nào trong chủ đề này</h5>
                    <p class="small text-muted mb-4">Các file học từ vựng được tạo hoặc nhập từ Excel sẽ xuất hiện tại đây.</p>
                    <button class="btn btn-primary rounded-pill px-4" onclick="backToFolders()">
                        <i class="bi bi-arrow-left me-1"></i> Quay lại chọn chủ đề khác
                    </button>
                </div>
            `;
            return;
        }

        let html = '';
        files.forEach(f => {
            const vocabCount = f.vocabCount || 0;
            const vocabText = `${vocabCount} từ vựng`;

            html += `
                <div class="col-md-6 col-lg-4">
                    <div class="card file-card h-100 p-4">
                        <div class="d-flex align-items-center justify-content-between mb-3">
                            <div class="file-icon-wrapper">
                                <i class="bi bi-file-earmark-spreadsheet-fill"></i>
                            </div>
                            <span class="badge-words">
                                <i class="bi bi-spellcheck me-1"></i>${vocabText}
                            </span>
                        </div>
                        <h5 class="fw-bold text-dark mb-2">${f.title}</h5>
                        <p class="text-muted small mb-4 flex-grow-1">${f.description || 'File học từ vựng'}</p>
                        <div class="d-flex gap-2">
                            <a href="/flashcard.html?topicId=${f.id}" class="btn btn-warning flex-fill fw-semibold rounded-3 text-dark btn-sm py-2" title="Học thẻ Flashcard">
                                <i class="bi bi-layers-fill me-1"></i> Flashcard
                            </a>
                            <a href="/vocabulary.html?topicId=${f.id}" class="btn btn-primary flex-fill fw-semibold rounded-3 btn-sm py-2" title="Xem danh sách từ vựng">
                                <i class="bi bi-book me-1"></i> Từ Vựng
                            </a>
                            <a href="/quiz.html?topicId=${f.id}" class="btn btn-outline-success flex-fill fw-semibold rounded-3 btn-sm py-2" title="Làm bài tập trắc nghiệm">
                                <i class="bi bi-controller me-1"></i> Bài Tập
                            </a>
                        </div>
                    </div>
                </div>
            `;
        });
        fileContainer.innerHTML = html;
    }

    // 5. QUAY LẠI DANH SÁCH THƯ MỤC
    function backToFolders() {
        currentFolderId = null;
        currentFiles = [];

        // Xóa query folderId khỏi URL
        window.history.pushState({}, '', window.location.pathname);

        document.getElementById('file-list').classList.add('d-none');
        document.getElementById('folder-list').classList.remove('d-none');
        document.getElementById('breadcrumb-bar').classList.add('d-none');
        document.getElementById('btn-back-folder').classList.add('d-none');

        document.getElementById('section-title').innerHTML = `<i class="bi bi-folder-fill me-2 text-primary"></i>Thư Mục Chủ Đề`;
        document.getElementById('section-subtitle').textContent = "Chọn một chủ đề lớn để xem các file bài học từ vựng chi tiết";

        document.getElementById('search-input').value = '';
        renderFolders();
    }

    // 6. XỬ LÝ TÌM KIẾM
    function handleSearch() {
        const keyword = document.getElementById('search-input').value.toLowerCase().trim();
        if (currentFolderId) {
            // Đang ở trong folder -> tìm kiếm file
            const filtered = currentFiles.filter(f =>
                (f.title && f.title.toLowerCase().includes(keyword)) ||
                (f.description && f.description.toLowerCase().includes(keyword))
            );
            renderFiles(filtered);
        } else {
            // Đang ở ngoài -> tìm kiếm folder
            const filtered = allFolders.filter(f =>
                (f.name && f.name.toLowerCase().includes(keyword)) ||
                (f.description && f.description.toLowerCase().includes(keyword))
            );
            renderFolders(filtered);
        }
    }

    // Hỗ trợ browser back/forward
    window.addEventListener('popstate', (e) => {
        if (e.state && e.state.folderId) {
            const found = allFolders.find(f => f.id == e.state.folderId);
            if (found) openFolder(found.id, found.name, found.description);
        } else {
            backToFolders();
        }
    });

    function escapeJs(str) {
        return (str || '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
    }
