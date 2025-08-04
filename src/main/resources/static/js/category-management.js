// Global variables
const sidebar = document.getElementById('sidebar');
const mainContent = document.getElementById('mainContent');
const sidebarOverlay = document.getElementById('sidebarOverlay');
let isMobile = window.innerWidth <= 768;
let sidebarCollapsed = false;
let editingId = null;
let editingType = null;

// Initialize sidebar state
function initializeSidebar() {
    isMobile = window.innerWidth <= 768;
    if (isMobile) {
        sidebar.classList.add('collapsed');
        mainContent.classList.add('expanded');
        sidebarCollapsed = true;
    } else {
        sidebar.classList.remove('collapsed', 'mobile-open');
        mainContent.classList.remove('expanded');
        sidebarOverlay.classList.remove('show');
        sidebarCollapsed = false;
    }
}

// Toggle sidebar
function toggleSidebar() {
    if (isMobile) {
        if (sidebar.classList.contains('mobile-open')) {
            closeMobileSidebar();
        } else {
            openMobileSidebar();
        }
    } else {
        if (sidebarCollapsed) {
            sidebar.classList.remove('collapsed');
            mainContent.classList.remove('expanded');
            sidebarCollapsed = false;
        } else {
            sidebar.classList.add('collapsed');
            mainContent.classList.add('expanded');
            sidebarCollapsed = true;
        }
    }
    updateToggleIcon();
}

function openMobileSidebar() {
    sidebar.classList.add('mobile-open');
    sidebarOverlay.classList.add('show');
    document.body.style.overflow = 'hidden';
}

function closeMobileSidebar() {
    sidebar.classList.remove('mobile-open');
    sidebarOverlay.classList.remove('show');
    document.body.style.overflow = 'auto';
}

function updateToggleIcon() {
    const toggleBtn = document.getElementById('sidebarToggle');
    const isOpen = isMobile ? sidebar.classList.contains('mobile-open') : !sidebarCollapsed;
    toggleBtn.innerHTML = isOpen ? '✕' : '☰';
}

// Tab switching
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('tab-btn')) {
        const tabName = e.target.dataset.tab;
        switchTab(tabName);
    }
});

function switchTab(tabName) {
    // Remove active from all tabs and contents
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

    // Add active to selected tab and content
    document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');
    document.getElementById(tabName).classList.add('active');
}

// Modal functions
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.add('show');
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';

    // Reset form
    const form = modal.querySelector('form');
    if (form && !editingId) {
        form.reset(); // Chỉ reset nếu đang thêm mới (không phải sửa)
    }

}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.remove('show');
    modal.style.display = 'none';
    document.body.style.overflow = 'auto';

    // Reset modal title
    const title = modal.querySelector('h3');
    if (title) {
        const type = modalId.replace('Modal', '');
        title.textContent = `Thêm ${type.charAt(0).toUpperCase() + type.slice(1)}`;
    }
}

// Toast notifications
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    const icons = {
        success: '✓',
        error: '✕',
        warning: '⚠'
    };

    toast.innerHTML = `
        <span class="toast-icon">${icons[type] || '✓'}</span>
        <span class="toast-message">${message}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3000);
}

function editCategory(id) {
    editingId = id;
    editingType = 'category';
    console.log("Edit category with ID:", id);

    fetch(`/admin/category/${id}`)
        .then(res => res.json())
        .then(data => {
            // Gán dữ liệu
            document.getElementById('categoryName').value = data.categoryName || '';
            document.getElementById('categoryDescription').value = data.description || '';
            document.getElementById('categoryStatus').value = (data.status || '').toLowerCase();

            // Mở modal SAU KHI gán dữ liệu
            document.getElementById('categoryModalTitle').textContent = 'Edit Category';
            openModal('categoryModal');
        })
        .catch(err => {
            console.error('Fetch error:', err);
            showToast('Không thể tải thông tin category', 'error');
        });
}


function editSubcategory(id) {
    editingId = id;
    editingType = 'subcategory';

    fetch(`/admin/subcategory/${id}`)
        .then(res => res.json())
        .then(data => {
            document.getElementById('subcategoryModalTitle').textContent = 'Edit SubCategory';
            document.getElementById('subcategoryName').value = data.subcategoryName || '';
            document.getElementById('subcategoryDescription').value = data.description || '';
            document.getElementById('subcategoryStatus').value = (data.status || '').toLowerCase();

            // ⚠️ đảm bảo là chuỗi
            document.getElementById('subcategoryCategory').value = String(data.category?.id || '');

            openModal('subcategoryModal');
        })
        .catch(() => {
            showToast('Không thể tải subcategory', 'error');
        });
}

function editBrand(id) {
    editingId = id;
    editingType = 'brand';

    fetch(`/admin/brand/${id}`)
        .then(res => res.json())
        .then(data => {
            document.getElementById('brandModalTitle').textContent = 'Sửa Brand';
            document.getElementById('brandName').value = data.brandName || '';
            document.getElementById('brandDescription').value = data.description || '';
            document.getElementById('brandStatus').value = (data.status || '').toLowerCase();
            openModal('brandModal');
        })
        .catch(() => {
            showToast('Không thể tải brand', 'error');
        });
}



function deleteCategory(id) {
    if (confirm('Bạn có chắc chắn muốn xóa category này?')) {
        showToast('Đã xóa category thành công!', 'success');
        // Remove row from table (mock)
        // In real app, make API call here
    }
}

function deleteSubcategory(id) {
    if (confirm('Bạn có chắc chắn muốn xóa subcategory này?')) {
        showToast('Đã xóa subcategory thành công!', 'success');
    }
}

function deleteBrand(id) {
    if (confirm('Bạn có chắc chắn muốn xóa brand này?')) {
        showToast('Đã xóa brand thành công!', 'success');
    }
}

// Form submissions
document.getElementById('categoryForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const name = document.getElementById('categoryName').value;
    const description = document.getElementById('categoryDescription').value;
    const status = document.getElementById('categoryStatus').value;

    if (editingId && editingType === 'category') {
        showToast('Đã cập nhật category thành công!', 'success');
    } else {
        showToast('Đã thêm category thành công!', 'success');
    }

    closeModal('categoryModal');
});

document.getElementById('subcategoryForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const name = document.getElementById('subcategoryName').value;
    const category = document.getElementById('subcategoryCategory').value;
    const description = document.getElementById('subcategoryDescription').value;
    const status = document.getElementById('subcategoryStatus').value;

    if (editingId && editingType === 'subcategory') {
        showToast('Đã cập nhật subcategory thành công!', 'success');
    } else {
        showToast('Đã thêm subcategory thành công!', 'success');
    }

    closeModal('subcategoryModal');
});

document.getElementById('brandForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const name = document.getElementById('brandName').value;
    const logo = document.getElementById('brandLogo').value;
    const description = document.getElementById('brandDescription').value;
    const status = document.getElementById('brandStatus').value;

    if (editingId && editingType === 'brand') {
        showToast('Đã cập nhật brand thành công!', 'success');
    } else {
        showToast('Đã thêm brand thành công!', 'success');
    }

    closeModal('brandModal');
});

// Event listeners
document.getElementById('sidebarToggle').addEventListener('click', toggleSidebar);
sidebarOverlay.addEventListener('click', closeMobileSidebar);

// Close modal when clicking outside
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal')) {
        const modalId = e.target.id;
        closeModal(modalId);
    }
});

// Keyboard shortcuts
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        const openModal = document.querySelector('.modal.show');
        if (openModal) {
            closeModal(openModal.id);
        } else if (isMobile && sidebar.classList.contains('mobile-open')) {
            closeMobileSidebar();
        }
    }
});

// Window resize handler
window.addEventListener('resize', function() {
    const wasMobile = isMobile;
    isMobile = window.innerWidth <= 768;

    if (wasMobile !== isMobile) {
        initializeSidebar();
        updateToggleIcon();
    }
});

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    initializeSidebar();
    updateToggleIcon();
});