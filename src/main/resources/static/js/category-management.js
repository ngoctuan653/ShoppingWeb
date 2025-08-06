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
        title.textContent = `Add ${type.charAt(0).toUpperCase() + type.slice(1)}`;
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
            showToast('Cannot load category information', 'error');
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

            // 👇 Gán category id
            document.getElementById('subcategoryCategory').value = String(data.category?.id || '');

            // 👇 Gán brand id
            document.getElementById('subcategoryBrand').value = String(data.brand?.id || '');

            openModal('subcategoryModal');
        })
        .catch(() => {
            showToast('Cannot load subcategory information', 'error');
        });
}


function editBrand(id) {
    editingId = id;
    editingType = 'brand';

    fetch(`/admin/brand/${id}`)
        .then(res => res.json())
        .then(data => {
            document.getElementById('brandModalTitle').textContent = 'Edit Brand';
            document.getElementById('brandName').value = data.brandName || '';
            document.getElementById('brandDescription').value = data.description || '';
            document.getElementById('brandStatus').value = (data.status || '').toLowerCase();
            openModal('brandModal');
        })
        .catch(() => {
            showToast('Cannot load brand information', 'error');
        });
}

function deleteCategory(id) {
    if (confirm("Are you sure you want to delete this category?")) {
        fetch(`/admin/category/${id}`, {
            method: 'DELETE'
        })
            .then(res => {
                if (res.ok) {
                    showToast('Delete category successfully', 'success');
                    removeRowById('category', id);
                } else {
                    throw new Error();
                }
            })
            .catch(() => showToast('Cannot delete category', 'error'));
    }
}

function deleteSubcategory(id) {
    if (confirm("Are you sure you want to delete this subcategory?")) {
        fetch(`/admin/subcategory/${id}`, {
            method: 'DELETE'
        })
            .then(res => {
                if (res.ok) {
                    showToast('Delete subcategory successfully', 'success');
                    removeRowById('subcategory', id);
                } else {
                    throw new Error();
                }
            })
            .catch(() => showToast('Cannot delete subcategory', 'error'));
    }
}

function deleteBrand(id) {
    if (confirm("Are you sure you want to delete this brand?")) {
        fetch(`/admin/brand/${id}`, {
            method: 'DELETE'
        })
            .then(res => {
                if (res.ok) {
                    showToast('Delete brand successfully', 'success');
                    removeRowById('brand', id);
                } else {
                    throw new Error();
                }
            })
            .catch(() => showToast('Cannot delete brand', 'error'));
    }
}


// Form submissions
document.getElementById('categoryForm').addEventListener('submit', function (e) {
    e.preventDefault();

    const name = document.getElementById('categoryName').value;
    const description = document.getElementById('categoryDescription').value;
    const status = document.getElementById('categoryStatus').value;

    const payload = {
        categoryName: name,
        description: description,
        status: status
    };

    const isEditing = editingId !== null;
    const url = isEditing ? `/admin/category/${editingId}` : '/admin/category';
    const method = isEditing ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
        .then(res => {
            if (!res.ok) throw new Error(); // handle HTTP errors
            return res.json();
        })
        .then(data => {
            showToast(isEditing ? 'Update category successfully' : 'Add category successfully', 'success');
            closeModal('categoryModal');

            location.reload();


            editingId = null;
            editingType = null;
        })
        .catch(() => showToast('Error when save category', 'error'));
});



document.getElementById('subcategoryForm').addEventListener('submit', function (e) {
    e.preventDefault();

    const name = document.getElementById('subcategoryName').value;
    const description = document.getElementById('subcategoryDescription').value;
    const categoryId = document.getElementById('subcategoryCategory').value;
    const brandId = document.getElementById('subcategoryBrand').value;
    const status = document.getElementById('subcategoryStatus').value;

    const payload = {
        subcategoryName: name,
        description: description,
        status: status,
        category: { id: categoryId },
        brand: { id: brandId } // 👈 THÊM DÒNG NÀY
    };

    const url = editingId ? `/admin/subcategory/${editingId}` : '/admin/subcategory';
    const method = editingId ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
        .then(res => res.json())
        .then(data => {
            showToast(editingId ? 'Update subcategory successfully' : 'Add subcategory successfully', 'success');
            closeModal('subcategoryModal');
            location.reload();
        })
        .catch(() => showToast('Error when save subcategory', 'error'));
});


document.getElementById('brandForm').addEventListener('submit', function (e) {
    e.preventDefault();

    const name = document.getElementById('brandName').value;
    const description = document.getElementById('brandDescription').value;
    const status = document.getElementById('brandStatus').value;

    const payload = {
        brandName: name,
        description: description,
        status: status
    };

    const url = editingId ? `/admin/brand/${editingId}` : '/admin/brand';
    const method = editingId ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    })
        .then(res => res.json())
        .then(data => {
            showToast(editingId ? 'Update brand successfully' : 'Add brand successfully', 'success');
            closeModal('brandModal');
            location.reload();
        })
        .catch(() => showToast('Error when save brand', 'error'));
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

function removeRowById(type, id) {
    const tableId = {
        category: 'categoriesTable',
        subcategory: 'subcategoriesTable',
        brand: 'brandsTable'
    }[type];

    if (!tableId) return;

    const table = document.getElementById(tableId);
    if (!table) return;

    const rows = table.querySelectorAll('tr');
    rows.forEach(row => {
        const cell = row.querySelector('td');
        if (cell && parseInt(cell.textContent) === id) {
            row.remove();
        }
    });
}


