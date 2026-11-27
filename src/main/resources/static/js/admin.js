    // 1. KIỂM TRA QUYỀN ADMIN
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (!currentUser || currentUser.role !== 'ADMIN') {
        alert("Bạn cần đăng nhập bằng tài khoản ADMIN!");
        window.location.href = '/login.html';
    }

    let globalFolders = [];
    let globalTopics = [];
    let globalVocabs = [];
    let globalUsers = [];
    let vocabModalInstance = null;
    let userModalInstance = null;

    document.addEventListener("DOMContentLoaded", () => {
        vocabModalInstance = new bootstrap.Modal(document.getElementById('vocabModal'));
        userModalInstance = new bootstrap.Modal(document.getElementById('userModal'));
        refreshAllData();
    });

    function showAlert(id, msg, type) {
        const el = document.getElementById(id);
        if (el) el.innerHTML = `<div class="alert alert-${type} border-0 shadow-sm mt-2">${msg}</div>`;
    }

    async function refreshAllData() {
        await loadFoldersData();
        await loadTopicsData();
        updateFolderSelectOptions();
        renderFileManagementTable();
    }

    // 2. TẢI DỮ LIỆU FOLDERS & TOPICS
    async function loadFoldersData() {
        try {
            const res = await fetch('/api/folders');
            globalFolders = await res.json();
            renderFolderBadges();
        } catch (e) {
            console.error(e);
        }
    }

    async function loadTopicsData() {
        try {
            const res = await fetch('/api/topics');
            globalTopics = await res.json();
        } catch (e) {
            console.error(e);
        }
    }

    function renderFolderBadges() {
        const container = document.getElementById('folder-badge-list');
        if (!globalFolders || globalFolders.length === 0) {
            container.innerHTML = '<span class="text-muted small">Chưa có thư mục nào</span>';
            return;
        }
        container.innerHTML = globalFolders.map(f => `
            <span class="badge bg-white text-dark border p-2 rounded-3 shadow-sm d-inline-flex align-items-center gap-2">
                <i class="bi bi-folder-fill text-primary"></i>
                <strong>${f.name}</strong>
                <span class="badge bg-light text-muted">${f.fileCount || 0} file</span>
                <button class="btn btn-link btn-sm text-danger p-0 ms-1" onclick="deleteFolder(${f.id}, '${f.name}')" title="Xóa thư mục">
                    <i class="bi bi-x-circle"></i>
                </button>
            </span>
        `).join('');
    }

    function updateFolderSelectOptions() {
        // Cập nhật tất cả các dropdown Folder
        const selects = ['importFolderId', 'filterFolderSelect', 'v-folderId', 'filterFileListFolderSelect', 'edit-topic-folder-id'];
        selects.forEach(id => {
            const sel = document.getElementById(id);
            if (!sel) return;
            const currentVal = sel.value;
            let optionsHtml = (id === 'filterFolderSelect' || id === 'filterFileListFolderSelect') 
                ? '<option value="">-- Tất cả Thư Mục --</option>' 
                : (id === 'edit-topic-folder-id' ? '' : '<option value="">-- Chọn Thư Mục Chủ Đề --</option>');
            globalFolders.forEach(f => {
                optionsHtml += `<option value="${f.id}">${f.name}</option>`;
            });
            sel.innerHTML = optionsHtml;
            if (currentVal) sel.value = currentVal;
        });

        // Trigger cascade cập nhật danh sách gợi ý file cho import
        onImportFolderChange();
    }

    // HIỂN THỊ DANH SÁCH FILE TỪ VỰNG THEO TỪNG CHỦ ĐỀ (FOLDER)
    function renderFileManagementTable() {
        const container = document.getElementById('file-management-container');
        if (!container) return;

        const selectedFolder = document.getElementById('filterFileListFolderSelect') ? document.getElementById('filterFileListFolderSelect').value : '';
        const keyword = document.getElementById('searchFileManagementInput') ? document.getElementById('searchFileManagementInput').value.toLowerCase().trim() : '';

        // Lọc danh sách folder cần hiển thị
        let foldersToDisplay = globalFolders || [];
        if (selectedFolder) {
            foldersToDisplay = foldersToDisplay.filter(f => f.id == selectedFolder);
        }

        if (foldersToDisplay.length === 0 && (!globalTopics || globalTopics.length === 0)) {
            container.innerHTML = `
                <div class="text-center py-5 text-muted">
                    <i class="bi bi-folder-x fs-1 text-secondary d-block mb-2"></i>
                    <p class="mb-0">Chưa có Thư mục Chủ đề nào!</p>
                </div>`;
            return;
        }

        let html = '';
        let totalMatchedFiles = 0;

        // Render từng Thư Mục Chủ Đề
        foldersToDisplay.forEach(folder => {
            // Lấy các file thuộc folder này
            let filesInFolder = (globalTopics || []).filter(t => t.folderId == folder.id);
            if (keyword) {
                filesInFolder = filesInFolder.filter(t => t.title && t.title.toLowerCase().includes(keyword));
            }

            // Nếu đang tìm kiếm và thư mục này không có file nào khớp thì bỏ qua
            if (keyword && filesInFolder.length === 0) {
                return;
            }

            totalMatchedFiles += filesInFolder.length;
            const totalVocabsInFolder = filesInFolder.reduce((sum, t) => sum + (t.vocabCount || 0), 0);

            html += `
                <div class="card mb-4 border rounded-4 overflow-hidden shadow-sm">
                    <!-- Header Chủ Đề -->
                    <div class="card-header bg-light-subtle border-bottom py-3 px-4 d-flex flex-wrap justify-content-between align-items-center gap-2">
                        <div class="d-flex align-items-center gap-3">
                            <span class="p-2 bg-primary bg-opacity-10 text-primary rounded-3 d-inline-flex align-items-center justify-content-center" style="width: 42px; height: 42px;">
                                <i class="bi bi-folder-fill fs-4"></i>
                            </span>
                            <div>
                                <h5 class="fw-bold mb-0 text-dark d-flex align-items-center gap-2 flex-wrap">
                                    <span>${folder.name}</span>
                                    <span class="badge bg-primary-subtle text-primary rounded-pill px-2 py-1 fs-6 fw-normal">${filesInFolder.length} file</span>
                                    <span class="badge bg-success-subtle text-success rounded-pill px-2 py-1 fs-6 fw-normal">${totalVocabsInFolder} từ</span>
                                </h5>
                                <span class="text-muted small">${folder.description || 'Chủ đề học từ vựng'}</span>
                            </div>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            ${folder.id !== 1 ? `
                                <button class="btn btn-outline-danger btn-sm rounded-pill px-3 py-1" onclick="deleteFolder(${folder.id}, '${folder.name}')" title="Xóa toàn bộ thư mục này">
                                    <i class="bi bi-trash me-1"></i>Xóa Thư Mục
                                </button>
                            ` : ''}
                        </div>
                    </div>

                    <!-- Bảng các File trong Chủ Đề -->
                    <div class="card-body p-0">
                        ${filesInFolder.length > 0 ? `
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th style="width: 80px;" class="ps-4">ID File</th>
                                            <th>Tên File Từ Vựng</th>
                                            <th class="text-center" style="width: 140px;">Số Từ Vựng</th>
                                            <th class="text-end pe-4" style="width: 320px;">Thao Tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        ${filesInFolder.map(t => `
                                            <tr>
                                                <td class="text-muted ps-4 fw-semibold">${t.id}</td>
                                                <td>
                                                    <div class="d-flex align-items-center">
                                                        <i class="bi bi-file-earmark-spreadsheet-fill text-success fs-5 me-2"></i>
                                                        <a href="javascript:void(0)" class="text-decoration-none fw-bold text-dark" onclick="viewFileVocabularies(${t.id}, ${t.folderId})" title="Nhấn để xem từ vựng của file này">
                                                            ${t.title}
                                                        </a>
                                                    </div>
                                                </td>
                                                <td class="text-center">
                                                    <span class="badge bg-success-subtle text-success rounded-pill px-3 py-2 fw-semibold">
                                                        ${t.vocabCount || 0} từ
                                                    </span>
                                                </td>
                                                <td class="text-end pe-4 text-nowrap">
                                                    <button class="btn btn-sm btn-primary rounded-pill px-3 me-2 shadow-sm" onclick="viewFileVocabularies(${t.id}, ${t.folderId})" title="Xem danh sách từ vựng của file này">
                                                        <i class="bi bi-journal-text me-1"></i> Xem từ
                                                    </button>
                                                    <button class="btn btn-sm btn-outline-warning text-dark rounded-pill px-3 me-2 shadow-sm" onclick="openEditTopicNameModal(${t.id})" title="Đổi tên file từ vựng này">
                                                        <i class="bi bi-pencil-square me-1"></i> Sửa Tên
                                                    </button>
                                                    <button class="btn btn-sm btn-outline-danger rounded-pill px-3" onclick="deleteTopicFile(${t.id})" title="Xóa toàn bộ file và từ vựng của file này">
                                                        <i class="bi bi-trash-fill me-1"></i> Xóa File
                                                    </button>
                                                </td>
                                            </tr>
                                        `).join('')}
                                    </tbody>
                                </table>
                            </div>
                        ` : `
                            <div class="text-center py-4 text-muted small">
                                <i class="bi bi-inbox me-1"></i> Chưa có file từ vựng nào trong chủ đề này. Hãy nhập tên ở Bước 2 ở trên để tạo!
                            </div>
                        `}
                    </div>
                </div>
            `;
        });

        // Kiểm tra xem có file nào chưa được gán folderId hợp lệ không
        const unassignedTopics = (globalTopics || []).filter(t => !t.folderId || !globalFolders.some(f => f.id == t.folderId));
        if (unassignedTopics.length > 0 && !selectedFolder) {
            let files = unassignedTopics;
            if (keyword) files = files.filter(t => t.title && t.title.toLowerCase().includes(keyword));
            if (files.length > 0) {
                totalMatchedFiles += files.length;
                html += `
                    <div class="card mb-4 border rounded-4 overflow-hidden shadow-sm border-warning">
                        <div class="card-header bg-warning-subtle py-3 px-4">
                            <h5 class="fw-bold mb-0 text-dark"><i class="bi bi-folder me-2"></i>File Chưa Phân Loại</h5>
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th style="width: 80px;" class="ps-4">ID File</th>
                                            <th>Tên File Từ Vựng</th>
                                            <th class="text-center" style="width: 140px;">Số Từ Vựng</th>
                                            <th class="text-end pe-4" style="width: 320px;">Thao Tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        ${files.map(t => `
                                            <tr>
                                                <td class="text-muted ps-4 fw-semibold">${t.id}</td>
                                                <td>
                                                    <i class="bi bi-file-earmark-spreadsheet-fill text-success fs-5 me-2"></i>
                                                    <a href="javascript:void(0)" class="text-decoration-none fw-bold text-dark" onclick="viewFileVocabularies(${t.id}, ${t.folderId})">${t.title}</a>
                                                </td>
                                                <td class="text-center">
                                                    <span class="badge bg-success-subtle text-success rounded-pill px-3 py-2 fw-semibold">${t.vocabCount || 0} từ</span>
                                                </td>
                                                <td class="text-end pe-4 text-nowrap">
                                                    <button class="btn btn-sm btn-primary rounded-pill px-3 me-2 shadow-sm" onclick="viewFileVocabularies(${t.id}, ${t.folderId})">
                                                        <i class="bi bi-journal-text me-1"></i> Xem từ
                                                    </button>
                                                    <button class="btn btn-sm btn-outline-warning text-dark rounded-pill px-3 me-2 shadow-sm" onclick="openEditTopicNameModal(${t.id})" title="Đổi tên file từ vựng này">
                                                        <i class="bi bi-pencil-square me-1"></i> Sửa Tên
                                                    </button>
                                                    <button class="btn btn-sm btn-outline-danger rounded-pill px-3" onclick="deleteTopicFile(${t.id})">
                                                        <i class="bi bi-trash-fill me-1"></i> Xóa File
                                                    </button>
                                                </td>
                                            </tr>
                                        `).join('')}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                `;
            }
        }

        if (keyword && totalMatchedFiles === 0) {
            html = `
                <div class="text-center py-5 text-muted">
                    <i class="bi bi-search fs-2 d-block mb-2 text-secondary"></i>
                    Không tìm thấy file nào khớp với từ khóa "<strong>${keyword}</strong>"
                </div>`;
        }

        container.innerHTML = html;
    }

    function viewFileVocabularies(topicId, folderId) {
        // 1. Kích hoạt Tab 2: Quản lý từ vựng
        const vocabTabBtn = document.querySelector('[data-bs-target="#tab-vocab"]');
        if (vocabTabBtn) {
            const tab = bootstrap.Tab.getOrCreateInstance(vocabTabBtn);
            tab.show();
        }

        // 2. Tìm topic và folderId nếu chưa có
        let resolvedFolderId = folderId;
        if (!resolvedFolderId) {
            const topic = (globalTopics || []).find(t => t.id == topicId);
            if (topic && topic.folderId) resolvedFolderId = topic.folderId;
        }

        if (resolvedFolderId) {
            const folderSelect = document.getElementById('filterFolderSelect');
            if (folderSelect) folderSelect.value = resolvedFolderId;
        }

        // 3. Cập nhật dropdown topic của folder này và chọn đúng topicId, tải đúng từ vựng
        onFilterFolderChange(topicId);

        // 4. Cuộn màn hình lên vị trí bảng từ vựng
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    // 3. TẠO THƯ MỤC CHỦ ĐỀ MỚI (FOLDER)
    document.getElementById('folder-form').addEventListener('submit', async function(e) {
        e.preventDefault();
        const name = document.getElementById('folderName').value.trim();
        const description = document.getElementById('folderDesc').value.trim();

        if (!name) return alert("Vui lòng nhập tên thư mục chủ đề!");

        try {
            const res = await fetch('/api/folders', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, description })
            });
            if (res.ok) {
                showAlert('folder-alert', "✅ Đã tạo Thư mục Chủ đề thành công!", "success");
                document.getElementById('folder-form').reset();
                await refreshAllData();
            } else {
                showAlert('folder-alert', "❌ Lỗi khi tạo thư mục!", "danger");
            }
        } catch (err) {
            showAlert('folder-alert', "❌ Lỗi: " + err.message, "danger");
        }
    });

    async function deleteFolder(id, name) {
        if (confirm(`Bạn có chắc chắn muốn xóa thư mục "${name}"?\n(Các file bên trong sẽ được chuyển sang thư mục mặc định)`)) {
            const res = await fetch(`/api/folders/${id}`, { method: 'DELETE' });
            if (res.ok) {
                await refreshAllData();
            }
        }
    }

    // 4. XỬ LÝ NHẬP LIỆU EXCEL / TEXT VÀ TỰ ĐỘNG TẠO FILE TỪ VỰNG
    function onImportFolderChange() {
        const folderId = document.getElementById('importFolderId').value;
        const dataList = document.getElementById('folderFilesList');
        const filesBox = document.getElementById('folder-files-box');
        const filesBadges = document.getElementById('folder-files-badges');
        const filesCount = document.getElementById('folder-files-count');

        if (dataList) dataList.innerHTML = '';
        if (!folderId) {
            if (filesBox) filesBox.classList.add('d-none');
            return;
        }

        const filesInFolder = globalTopics.filter(t => t.folderId == folderId);
        if (filesInFolder.length > 0) {
            filesBox.classList.remove('d-none');
            filesCount.innerText = `${filesInFolder.length} file`;
            filesBadges.innerHTML = filesInFolder.map(t => `
                <span class="badge bg-white text-dark border p-2 rounded-3 shadow-sm d-inline-flex align-items-center gap-2">
                    <i class="bi bi-file-earmark-spreadsheet-fill text-success"></i>
                    <span>${t.title}</span>
                    <span class="badge bg-light text-muted">${t.vocabCount || 0} từ</span>
                    <button class="btn btn-link btn-sm text-danger p-0 ms-1" onclick="deleteTopicFile(${t.id})" title="Xóa file từ vựng này">
                        <i class="bi bi-trash"></i>
                    </button>
                </span>
            `).join('');
        } else {
            filesBox.classList.remove('d-none');
            filesCount.innerText = `0 file`;
            filesBadges.innerHTML = '<span class="text-muted small">Chưa có file từ vựng nào trong thư mục này. Hãy nhập tên ở Bước 2 để tạo!</span>';
        }

        filesInFolder.forEach(t => {
            const opt = document.createElement('option');
            opt.value = t.title;
            dataList.appendChild(opt);
        });
    }

    async function deleteTopicFile(id, customTitle = null) {
        const topic = globalTopics.find(t => t.id == id);
        const title = customTitle || (topic ? topic.title : `ID: ${id}`);
        if (!confirm(`⚠️ BẠN CÓ CHẮC CHẮN MUỐN XÓA FILE NÀY?\n\nFile: "${title}"\n\nToàn bộ từ vựng, flashcard và bài tập thuộc file này sẽ bị xóa hoàn toàn khỏi hệ thống!`)) {
            return;
        }
        try {
            const res = await fetch(`/api/topics/${id}`, { method: 'DELETE' });
            if (res.ok) {
                alert(`Đã xóa file từ vựng "${title}" thành công!`);
                await refreshAllData();
                onFilterFolderChange();
                onImportFolderChange();
            } else {
                alert("Lỗi! Không thể xóa file từ vựng này.");
            }
        } catch (e) {
            alert("Lỗi kết nối máy chủ: " + e.message);
        }
    }

    async function deleteCurrentFilteredFile() {
        const topicId = document.getElementById('filterTopicSelect').value;
        if (!topicId) return;
        const topic = globalTopics.find(t => t.id == topicId);
        const title = topic ? topic.title : `ID: ${topicId}`;
        await deleteTopicFile(topicId, title);
    }

    let editTopicModalInstance = null;

    function openEditTopicNameModal(topicId) {
        const topic = (globalTopics || []).find(t => t.id == topicId);
        if (!topic) return alert("Không tìm thấy file từ vựng!");

        document.getElementById('edit-topic-id').value = topic.id;
        document.getElementById('edit-topic-title').value = topic.title;
        document.getElementById('edit-topic-alert').innerHTML = '';

        // Đồng bộ dropdown Folder
        const folderSelect = document.getElementById('edit-topic-folder-id');
        if (folderSelect) {
            let optionsHtml = '';
            (globalFolders || []).forEach(f => {
                optionsHtml += `<option value="${f.id}">${f.name}</option>`;
            });
            folderSelect.innerHTML = optionsHtml;
            if (topic.folderId) {
                folderSelect.value = topic.folderId;
            }
        }

        if (!editTopicModalInstance) {
            editTopicModalInstance = new bootstrap.Modal(document.getElementById('editTopicModal'));
        }
        editTopicModalInstance.show();

        // Tự động focus vào ô nhập tên file
        setTimeout(() => {
            const input = document.getElementById('edit-topic-title');
            if (input) {
                input.focus();
                input.select();
            }
        }, 400);
    }

    async function saveEditTopic() {
        const id = document.getElementById('edit-topic-id').value;
        const newTitle = document.getElementById('edit-topic-title').value.trim();
        const folderId = document.getElementById('edit-topic-folder-id').value;

        if (!newTitle) {
            document.getElementById('edit-topic-alert').innerHTML = '<div class="alert alert-danger py-2 mt-2">Vui lòng nhập tên file từ vựng!</div>';
            return;
        }

        try {
            const res = await fetch(`/api/topics/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    title: newTitle,
                    folderId: folderId ? parseInt(folderId) : null
                })
            });

            if (res.ok) {
                if (editTopicModalInstance) editTopicModalInstance.hide();
                await refreshAllData();
                onFilterFolderChange();
                alert(`✅ Đã đổi tên file thành "${newTitle}" thành công!`);
            } else {
                document.getElementById('edit-topic-alert').innerHTML = '<div class="alert alert-danger py-2 mt-2">❌ Lỗi: Không thể lưu tên file!</div>';
            }
        } catch (e) {
            document.getElementById('edit-topic-alert').innerHTML = `<div class="alert alert-danger py-2 mt-2">❌ Lỗi kết nối: ${e.message}</div>`;
        }
    }

    function editCurrentFilteredFile() {
        const topicId = document.getElementById('filterTopicSelect').value;
        if (topicId) {
            openEditTopicNameModal(topicId);
        }
    }

    function onExcelFileSelected(input) {
        if (input.files && input.files[0]) {
            const file = input.files[0];
            const nameInput = document.getElementById('importFileName');
            // Nếu ô tên file đang trống, tự động lấy tên file Excel tải lên
            if (!nameInput.value.trim()) {
                nameInput.value = file.name.replace(/\.[^/.]+$/, "");
            }
        }
    }

    async function processImport() {
        const folderId = document.getElementById('importFolderId').value;
        let fileName = document.getElementById('importFileName').value.trim();
        const fileInput = document.getElementById('excelFile');
        const rawText = document.getElementById('bulkText').value.trim();

        if (!folderId) return alert("Vui lòng chọn Thư mục Chủ đề!");

        // Nếu người dùng chưa gõ tên file mà đã chọn file Excel
        if (!fileName && fileInput.files && fileInput.files[0]) {
            fileName = fileInput.files[0].name.replace(/\.[^/.]+$/, "");
            document.getElementById('importFileName').value = fileName;
        }

        if (!fileName) return alert("Vui lòng nhập Tên File Từ Vựng!");

        // Kiểm tra xem file này đã có trong folder chưa
        let topicId = null;
        const existing = globalTopics.find(t => t.folderId == folderId && t.title.trim().toLowerCase() === fileName.toLowerCase());
        if (existing) {
            topicId = existing.id;
        } else {
            // Tự động tạo file mới ngay tại đây
            try {
                showAlert('bulk-alert', `⏳ Đang tạo file từ vựng "${fileName}"...`, "info");
                const resTopic = await fetch('/api/topics', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ folderId: parseInt(folderId), title: fileName })
                });
                if (!resTopic.ok) throw new Error("Không thể tạo file từ vựng mới");
                const createdTopic = await resTopic.json();
                topicId = createdTopic.id;
                await refreshAllData();
            } catch (e) {
                return showAlert('bulk-alert', "❌ Lỗi tạo file từ vựng mới: " + e.message, "danger");
            }
        }

        if (fileInput.files.length > 0) {
            readExcelFile(fileInput.files[0], topicId);
        } else if (rawText) {
            readTextData(rawText, topicId);
        } else {
            alert("Vui lòng chọn File Excel HOẶC dán dữ liệu từ vựng!");
        }
    }

    function readExcelFile(file, topicId) {
        showAlert('bulk-alert', "⏳ Đang đọc file Excel...", "info");
        const reader = new FileReader();
        reader.onload = function(e) {
            try {
                const data = new Uint8Array(e.target.result);
                const workbook = XLSX.read(data, { type: 'array' });
                const worksheet = workbook.Sheets[workbook.SheetNames[0]];
                const rows = XLSX.utils.sheet_to_json(worksheet, { header: 1 });
                const vocabList = [];

                rows.forEach((row, index) => {
                    if (index === 0 && row[0] && (row[0].toString().toLowerCase().includes('word') || row[0].toString().toLowerCase().includes('từ'))) return;
                    if (row && row[0]) {
                        vocabList.push({
                            topicId: parseInt(topicId),
                            word: (row[0] || '').toString().trim(),
                            loaiTu: (row[1] || '').toString().trim(),
                            phienAm: (row[2] || '').toString().trim(),
                            meaning: (row[3] || '').toString().trim(),
                            wordForms: (row[4] || '').toString().trim(),
                            paraphrase: (row[5] || '').toString().trim(),
                            collocationExample: (row[6] || '').toString().trim()
                        });
                    }
                });
                sendBulkToBackend(vocabList);
            } catch (err) {
                showAlert('bulk-alert', "❌ Lỗi đọc Excel: " + err.message, "danger");
            }
        };
        reader.readAsArrayBuffer(file);
    }

    function readTextData(rawText, topicId) {
        const lines = rawText.split('\n');
        const vocabList = [];
        lines.forEach(line => {
            if (line.trim()) {
                const delimiter = line.includes('\t') ? '\t' : '|';
                const parts = line.split(delimiter).map(p => p.trim());
                if (parts[0]) {
                    vocabList.push({
                        topicId: parseInt(topicId),
                        word: parts[0] || '',
                        loaiTu: parts[1] || '',
                        phienAm: parts[2] || '',
                        meaning: parts[3] || '',
                        wordForms: parts[4] || '',
                        paraphrase: parts[5] || '',
                        collocationExample: parts[6] || ''
                    });
                }
            }
        });
        sendBulkToBackend(vocabList);
    }

    function sendBulkToBackend(vocabList) {
        if (vocabList.length === 0) return showAlert('bulk-alert', "Không có dữ liệu hợp lệ!", "danger");
        showAlert('bulk-alert', `⏳ Đang lưu ${vocabList.length} từ vựng vào hệ thống...`, "info");

        fetch('/api/vocabularies/bulk', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(vocabList)
        })
        .then(async res => {
            if (!res.ok) throw new Error("Lỗi lưu dữ liệu!");
            return res.json();
        })
        .then(data => {
            showAlert('bulk-alert', `🎉 ${data.message || 'Nhập thành công ' + vocabList.length + ' từ vựng!'}`, "success");
            document.getElementById('bulkText').value = '';
            document.getElementById('excelFile').value = '';
            refreshAllData();
        })
        .catch(err => showAlert('bulk-alert', "❌ " + err.message, "danger"));
    }

    // ==================== 6. QUẢN LÝ TỪ VỰNG ====================
    function loadVocabFiltersAndList() {
        const currentTopic = document.getElementById('filterTopicSelect').value;
        if (!currentTopic) {
            onFilterFolderChange();
        } else {
            loadVocabList();
        }
    }

    function onFilterFolderChange(selectedTopicId = null) {
        const folderId = document.getElementById('filterFolderSelect').value;
        const topicSelect = document.getElementById('filterTopicSelect');

        let filteredTopics = globalTopics || [];
        if (folderId) {
            filteredTopics = filteredTopics.filter(t => t.folderId == folderId);
        }

        let html = '<option value="">-- Tất cả File Từ Vựng --</option>';
        filteredTopics.forEach(t => {
            html += `<option value="${t.id}">${t.title}</option>`;
        });
        topicSelect.innerHTML = html;

        if (selectedTopicId) {
            topicSelect.value = String(selectedTopicId);
        }
        loadVocabList();
    }

    async function loadVocabList() {
        const topicId = document.getElementById('filterTopicSelect').value;
        const deleteFileBtn = document.getElementById('btn-delete-file');
        const editFileBtn = document.getElementById('btn-edit-file');
        const activeBanner = document.getElementById('vocab-active-file-banner');
        const activeTitle = document.getElementById('active-file-title');
        const activeFolder = document.getElementById('active-folder-title');
        const activeCount = document.getElementById('active-vocab-count');

        const currTopic = topicId ? (globalTopics || []).find(t => t.id == topicId) : null;
        const currFolder = currTopic ? (globalFolders || []).find(f => f.id == currTopic.folderId) : null;

        if (editFileBtn) {
            if (currTopic) {
                editFileBtn.classList.remove('d-none');
                editFileBtn.title = `Sửa tên file "${currTopic.title}"`;
            } else {
                editFileBtn.classList.add('d-none');
            }
        }

        if (deleteFileBtn) {
            if (currTopic) {
                deleteFileBtn.classList.remove('d-none');
                deleteFileBtn.title = `Xóa file "${currTopic.title}"`;
            } else {
                deleteFileBtn.classList.add('d-none');
            }
        }

        if (activeBanner) {
            if (currTopic) {
                activeBanner.classList.remove('d-none');
                if (activeTitle) activeTitle.textContent = currTopic.title;
                if (activeFolder) activeFolder.textContent = currFolder ? currFolder.name : (currTopic.folderName || 'Chủ đề chung');
                if (activeCount) activeCount.textContent = `${currTopic.vocabCount || 0} từ`;
            } else {
                activeBanner.classList.add('d-none');
            }
        }

        const tbody = document.getElementById('vocab-table-body');
        const checkAllBox = document.getElementById('check-all');
        if (checkAllBox) checkAllBox.checked = false;
        updateBulkDeleteButton();

        tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted"><span class="spinner-border spinner-border-sm me-2"></span>Đang tải dữ liệu từ vựng...</td></tr>`;

        try {
            let url;
            if (topicId) {
                url = `/api/vocabularies/topic/${topicId}`;
            } else {
                url = `/api/vocabularies/game?limit=100`;
            }
            const res = await fetch(url);
            globalVocabs = await res.json();

            if (activeCount && currTopic) {
                activeCount.textContent = `${globalVocabs.length} từ`;
            }

            if (!globalVocabs || globalVocabs.length === 0) {
                tbody.innerHTML = `<tr><td colspan="7" class="text-center py-5 text-muted">
                    <i class="bi bi-inbox fs-2 d-block mb-2 text-secondary"></i>
                    Chưa có từ vựng nào trong file này!
                </td></tr>`;
                return;
            }

            tbody.innerHTML = globalVocabs.map(v => `
                <tr>
                    <td class="col-check">
                        <input class="form-check-input vocab-checkbox" type="checkbox" value="${v.id}" onchange="updateBulkDeleteButton()">
                    </td>
                    <td class="col-id text-muted fw-semibold">${v.id}</td>
                    <td class="col-word text-primary fw-bold">${v.word}</td>
                    <td class="col-ipa"><span class="badge bg-light text-dark border font-monospace">${v.phienAm || ''}</span></td>
                    <td class="col-pos"><span class="badge bg-secondary-subtle text-secondary border">${v.loaiTu || 'Noun'}</span></td>
                    <td class="col-meaning fw-medium text-dark">${v.meaning}</td>
                    <td class="col-action text-nowrap">
                        <button class="btn btn-sm btn-outline-primary me-1 rounded-pill px-2" onclick="openEditVocabModal(${v.id})" title="Sửa">
                            <i class="bi bi-pencil-square"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger rounded-pill px-2" onclick="deleteSingleVocab(${v.id})" title="Xóa">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `).join('');
        } catch (e) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-danger">Không thể tải dữ liệu: ${e.message}</td></tr>`;
        }
    }

    function toggleSelectAll(master) {
        const checkboxes = document.querySelectorAll('.vocab-checkbox');
        checkboxes.forEach(cb => cb.checked = master.checked);
        updateBulkDeleteButton();
    }

    function updateBulkDeleteButton() {
        const selected = document.querySelectorAll('.vocab-checkbox:checked');
        const deleteBtn = document.getElementById('btn-delete-bulk');
        const countSpan = document.getElementById('selected-count');

        if (selected.length > 0) {
            deleteBtn.classList.remove('d-none');
            countSpan.innerText = selected.length;
        } else {
            deleteBtn.classList.add('d-none');
        }
    }

    async function deleteSelectedVocabs() {
        const selectedBoxes = document.querySelectorAll('.vocab-checkbox:checked');
        const ids = Array.from(selectedBoxes).map(cb => Number(cb.value));
        if (ids.length === 0) return;

        if (confirm(`Bạn có chắc chắn muốn xóa ${ids.length} từ vựng đã chọn khỏi CSDL?`)) {
            try {
                const res = await fetch('/api/vocabularies/bulk', {
                    method: 'DELETE',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(ids)
                });
                if (res.ok) {
                    loadVocabList();
                    refreshAllData();
                } else {
                    alert("Lỗi khi thực hiện xóa hàng loạt!");
                }
            } catch (err) {
                alert("Lỗi kết nối máy chủ: " + err.message);
            }
        }
    }

    function onModalFolderChange() {
        const folderId = document.getElementById('v-folderId').value;
        const topicSelect = document.getElementById('v-topicId');
        const filesInFolder = globalTopics.filter(t => t.folderId == folderId);

        let html = '<option value="">-- Chọn File Từ Vựng --</option>';
        filesInFolder.forEach(t => {
            html += `<option value="${t.id}">${t.title}</option>`;
        });
        topicSelect.innerHTML = html;
    }

    function openAddVocabModal() {
        document.getElementById('vocabForm').reset();
        document.getElementById('v-id').value = '';
        document.getElementById('vocabModalTitle').innerText = "Thêm Từ Vựng Mới";

        // Đồng bộ dropdown Folder & Topic
        const currentFolder = document.getElementById('filterFolderSelect').value || (globalFolders[0] ? globalFolders[0].id : '');
        document.getElementById('v-folderId').value = currentFolder;
        onModalFolderChange();

        const currentTopic = document.getElementById('filterTopicSelect').value;
        if (currentTopic) document.getElementById('v-topicId').value = currentTopic;

        vocabModalInstance.show();
    }

    function openEditVocabModal(id) {
        const item = globalVocabs.find(v => v.id === id);
        if (!item) return;

        document.getElementById('v-id').value = item.id;
        document.getElementById('v-word').value = item.word;
        document.getElementById('v-loaiTu').value = item.loaiTu || '';
        document.getElementById('v-phienAm').value = item.phienAm || '';
        document.getElementById('v-meaning').value = item.meaning;

        const foundTopic = globalTopics.find(t => t.id === item.topicId);
        if (foundTopic) {
            document.getElementById('v-folderId').value = foundTopic.folderId || '';
            onModalFolderChange();
            document.getElementById('v-topicId').value = foundTopic.id;
        }

        document.getElementById('vocabModalTitle').innerText = `Sửa Từ Vựng (ID: ${item.id})`;
        vocabModalInstance.show();
    }

    async function saveVocabulary() {
        const id = document.getElementById('v-id').value;
        const word = document.getElementById('v-word').value.trim();
        const meaning = document.getElementById('v-meaning').value.trim();
        const loaiTu = document.getElementById('v-loaiTu').value.trim();
        const phienAm = document.getElementById('v-phienAm').value.trim();
        const topicId = document.getElementById('v-topicId').value;

        if (!word || !meaning || !topicId) return alert("Vui lòng nhập đầy đủ thông tin!");

        const payload = { word, meaning, loaiTu, phienAm, topicId: parseInt(topicId) };
        const url = id ? `/api/vocabularies/${id}` : `/api/vocabularies`;
        const method = id ? 'PUT' : 'POST';

        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            vocabModalInstance.hide();
            loadVocabList();
            refreshAllData();
        } else {
            alert("Lỗi khi lưu từ vựng!");
        }
    }

    async function deleteSingleVocab(id) {
        if (confirm("Bạn có chắc muốn xóa từ vựng này?")) {
            const res = await fetch(`/api/vocabularies/${id}`, { method: 'DELETE' });
            if (res.ok) {
                loadVocabList();
                refreshAllData();
            }
        }
    }

    // ==================== 7. QUẢN LÝ TÀI KHOẢN ====================
    async function loadUserList() {
        const tbody = document.getElementById('user-table-body');
        tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted"><span class="spinner-border spinner-border-sm me-2"></span>Đang tải...</td></tr>`;

        try {
            const res = await fetch('/api/users');
            globalUsers = await res.json();

            tbody.innerHTML = globalUsers.map(u => `
                <tr>
                    <td class="text-muted">${u.id}</td>
                    <td class="fw-semibold">${u.fullName}</td>
                    <td>${u.email}</td>
                    <td><span class="badge ${u.role === 'ADMIN' ? 'bg-danger' : 'bg-primary'}">${u.role}</span></td>
                    <td>${u.createdAt ? new Date(u.createdAt).toLocaleDateString('vi-VN') : '--'}</td>
                    <td class="text-center">
                        <button class="btn btn-sm btn-outline-primary me-1" onclick="openEditUserModal(${u.id})" title="Sửa">
                            <i class="bi bi-pencil-square"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteUserAccount(${u.id})" title="Xóa">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `).join('');
        } catch (e) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-danger">Không thể tải danh sách tài khoản!</td></tr>`;
        }
    }

    function openAddUserModal() {
        document.getElementById('userForm').reset();
        document.getElementById('u-id').value = '';
        document.getElementById('u-email').disabled = false;
        document.getElementById('u-password').required = true;
        document.getElementById('userModalTitle').innerText = "Tạo Tài Khoản Mới";
        userModalInstance.show();
    }

    function openEditUserModal(id) {
        const user = globalUsers.find(u => u.id === id);
        if (!user) return;

        document.getElementById('u-id').value = user.id;
        document.getElementById('u-fullname').value = user.fullName;
        document.getElementById('u-email').value = user.email;
        document.getElementById('u-email').disabled = true;
        document.getElementById('u-password').value = '';
        document.getElementById('u-password').required = false;
        document.getElementById('u-role').value = user.role;

        document.getElementById('userModalTitle').innerText = `Sửa Tài Khoản (${user.email})`;
        userModalInstance.show();
    }

    async function saveUser() {
        const id = document.getElementById('u-id').value;
        const fullName = document.getElementById('u-fullname').value.trim();
        const email = document.getElementById('u-email').value.trim();
        const password = document.getElementById('u-password').value.trim();
        const role = document.getElementById('u-role').value;

        if (!fullName || !email) return alert("Vui lòng điền đủ thông tin!");
        if (!id && !password) return alert("Vui lòng nhập mật khẩu cho tài khoản mới!");

        const payload = { fullName, email, role };
        if (password) payload.password = password;

        const url = id ? `/api/users/${id}` : `/api/users`;
        const method = id ? 'PUT' : 'POST';

        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            userModalInstance.hide();
            loadUserList();
        } else {
            const err = await res.json();
            alert(err.message || "Lỗi lưu tài khoản!");
        }
    }

    async function deleteUserAccount(id) {
        if (confirm("Bạn có chắc chắn muốn xóa tài khoản này?")) {
            const res = await fetch(`/api/users/${id}`, { method: 'DELETE' });
            if (res.ok) loadUserList();
        }
    }

    function escapeJs(str) {
        return (str || '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
    }
