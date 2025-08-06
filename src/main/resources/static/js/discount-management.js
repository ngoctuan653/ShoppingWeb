// Global variables
const sidebar = document.getElementById('sidebar');
const mainContent = document.getElementById('mainContent');
const sidebarOverlay = document.getElementById('sidebarOverlay');
const discountModal = document.getElementById('discountModal');
const toast = document.getElementById('toast');
let isMobile = window.innerWidth <= 768;
let sidebarCollapsed = false;
let isEditing = false;
let currentEditId = null;

// Initialize app
document.addEventListener('DOMContentLoaded', function() {
    initializeSidebar();
    updateToggleIcon();
    loadDiscounts();
    loadStats();
    initializeEventListeners();
});


// Sidebar functions
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
    const toggleBtns = document.querySelectorAll('.sidebar-toggle');
    const isOpen = isMobile ? sidebar.classList.contains('mobile-open') : !sidebarCollapsed;

    toggleBtns.forEach(btn => {
        btn.innerHTML = isOpen ? '✕' : '☰';
    });
}

function loadDiscounts(page = 0, searchTerm = '', status = '') {
    let url = `/api/discounts?page=${page}&size=5`;

    if (searchTerm.trim()) {
        url += `&search=${encodeURIComponent(searchTerm.trim())}`;
    }

    if (status) {
        url += `&status=${status}`;
    }

    fetch(url)
        .then(response => response.json())
        .then(data => {
            const discounts = data.content;
            const tbody = document.querySelector('#discountsTable tbody');
            tbody.innerHTML = '';

            if (discounts.length === 0) {
                tbody.innerHTML = `<tr><td colspan="9" style="text-align: center;">No discount code found</td></tr>`;
            } else {
                discounts.forEach(discount => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td><span class="code-badge">${discount.code}</span></td>
                        <td>${discount.description || ''}</td>
                        <td class="discount-value">${discount.discountPercentage}%</td>
                        <td>${discount.availableQuantity}</td>
                        <td>${formatDate(discount.startDate)}</td>
                        <td>${formatDate(discount.endDate)}</td>
                        <td>
                            <span class="status-badge ${discount.status === 'Active' ? 'status-active' : 'status-disabled'}">
                                ${discount.status}
                            </span>
                        </td>
                        <td>
                            <div class="action-buttons">
                                <button class="btn-action btn-edit" onclick="editDiscount(${discount.id})">✏️</button>
                                <button class="btn-action btn-delete" onclick="deleteDiscount(${discount.id})">🗑️</button>
                            </div>
                        </td>
                    `;
                    tbody.appendChild(tr);
                });
            }

            renderPagination(data);
        })
        .catch(err => {
            console.error('Error loading discounts:', err);
            showToast('Error loading discounts', 'error');
        });
}

function renderPagination(data) {
    const container = document.querySelector('.pagination-controls');
    container.innerHTML = '';

    const currentPage = data.number;
    const totalPages = data.totalPages;
    const { search, status } = getCurrentFilters();

    if (totalPages <= 1) return;

    const prevBtn = document.createElement('button');
    prevBtn.className = 'pagination-btn';
    prevBtn.textContent = '‹ Trước';
    prevBtn.disabled = currentPage === 0;
    prevBtn.addEventListener('click', () => loadDiscounts(currentPage - 1, search, status));
    container.appendChild(prevBtn);

    for (let i = 0; i < totalPages; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = 'pagination-btn';
        if (i === currentPage) pageBtn.classList.add('active');
        pageBtn.textContent = i + 1;
        pageBtn.addEventListener('click', () => loadDiscounts(i, search, status));
        container.appendChild(pageBtn);
    }

    const nextBtn = document.createElement('button');
    nextBtn.className = 'pagination-btn';
    nextBtn.textContent = 'Sau ›';
    nextBtn.disabled = currentPage === totalPages - 1;
    nextBtn.addEventListener('click', () => loadDiscounts(currentPage + 1, search, status));
    container.appendChild(nextBtn);
}

function loadStats() {
    fetch('/api/discounts/stats')
        .then(res => res.json())
        .then(data => {
            document.getElementById('totalDiscountCount').textContent = data.total;
            document.getElementById('activeDiscountCount').textContent = data.active;
            document.getElementById('usedDiscountCount').textContent = data.used.toLocaleString('vi-VN');
        })
        .catch(err => {
            console.error('Lỗi khi tải thống kê:', err);
        });
}


// Modal functions
function openModal(title = 'Add new Discount') {
    document.getElementById('modalTitle').textContent = title;
    discountModal.classList.add('show');
    document.body.style.overflow = 'hidden';

    // Focus on first input
    setTimeout(() => {
        document.getElementById('discountCode').focus();
    }, 300);
}

function closeModal() {
    discountModal.classList.remove('show');
    document.body.style.overflow = 'auto';
    resetForm();
    isEditing = false;
    currentEditId = null;
}

function resetForm() {
    document.getElementById('discountForm').reset();
    document.getElementById('valueSuffix').textContent = '%';
    document.getElementById('discountValue').placeholder = 'VD: 20';
}

// Toast functions
function showToast(message, type = 'success') {
    const toastIcon = document.getElementById('toastIcon');
    const toastMessage = document.getElementById('toastMessage');

    // Remove existing type classes
    toast.classList.remove('success', 'error', 'warning', 'info');

    // Set icon and message based on type
    switch(type) {
        case 'success':
            toastIcon.textContent = '✅';
            toast.classList.add('success');
            break;
        case 'error':
            toastIcon.textContent = '❌';
            toast.classList.add('error');
            break;
        case 'warning':
            toastIcon.textContent = '⚠️';
            toast.classList.add('warning');
            break;
        case 'info':
            toastIcon.textContent = 'ℹ️';
            toast.classList.add('info');
            break;
    }

    toastMessage.textContent = message;
    toast.classList.add('show');

    // Auto hide after 3 seconds
    setTimeout(() => {
        hideToast();
    }, 3000);
}

function hideToast() {
    toast.classList.remove('show');
}

// Form validation and submission
function validateForm() {
    const code = document.getElementById('discountCode').value.trim();
    const value = document.getElementById('discountValue').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;

    if (!code) {
        showToast('Vui lòng nhập mã code', 'error');
        return false;
    }

    if (!value) {
        showToast('Vui lòng nhập giá trị giảm giá', 'error');
        return false;
    }

    if (!startDate) {
        showToast('Vui lòng chọn ngày bắt đầu', 'error');
        return false;
    }

    if (!endDate) {
        showToast('Vui lòng chọn ngày kết thúc', 'error');
        return false;
    }

    if (new Date(startDate) >= new Date(endDate)) {
        showToast('Ngày kết thúc phải sau ngày bắt đầu', 'error');
        return false;
    }

    return true;
}


function handleFormSubmit(e) {
    e.preventDefault();
    if (!validateForm()) return;

    const submitBtn = document.getElementById('saveBtn');
    const originalText = submitBtn.textContent;
    submitBtn.textContent = 'Đang lưu...';
    submitBtn.disabled = true;

    const discount = {
        code: document.getElementById('discountCode').value.trim(),
        percentage: document.getElementById('discountValue').value,
        description: document.getElementById('description').value.trim(),
        availableQuantity: document.getElementById('maxUses').value,
        startDate: document.getElementById('startDate').value,
        endDate: document.getElementById('endDate').value
    };

    const method = isEditing ? 'PUT' : 'POST';
    const url = isEditing ? '/api/discounts/edit' : '/api/discounts/add';

    if (isEditing) {
        discount.id = currentEditId;
        discount.status = document.getElementById('isActive').checked ? 'Active' : 'Inactive';
    }

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(discount)
    })
        .then(res => res.json().then(data => ({ status: res.status, body: data })))
        .then(({ status, body }) => {
            if (status === 200) {
                showToast(isEditing ? 'Cập nhật thành công!' : 'Thêm thành công!', 'success');
                loadDiscounts();
                closeModal();
            } else {
                showToast(body.error || 'Đã xảy ra lỗi', 'error');
            }
        })
        .catch(err => {
            console.error(err);
            showToast('Lỗi kết nối máy chủ', 'error');
        })
        .finally(() => {
            submitBtn.textContent = originalText;
            submitBtn.disabled = false;
        });
}


// Discount management functions
function editDiscount(id) {
    isEditing = true;
    currentEditId = id;

    fetch(`/api/discounts/${id}`)
        .then(res => res.json())
        .then(data => {
            document.getElementById('discountCode').value = data.code;
            document.getElementById('discountValue').value = data.percentage;
            document.getElementById('description').value = data.description || '';
            document.getElementById('maxUses').value = data.availableQuantity;
            document.getElementById('startDate').value = data.startDate;
            document.getElementById('endDate').value = data.endDate;
            document.getElementById('isActive').checked = data.status === 'Active';

            updateFormByType('percentage'); // Nếu có trường type thì cập nhật thêm
            openModal('Edit Discount');
        })
        .catch(err => {
            console.error(err);
            showToast('Không thể tải dữ liệu mã giảm giá', 'error');
        });
}


function deleteDiscount(id) {
    if (!confirm('Bạn có chắc chắn muốn xóa mã giảm giá này?')) return;

    fetch(`/api/discounts/${id}`, {
        method: 'DELETE'
    })
        .then(res => res.json().then(data => ({ status: res.status, body: data })))
        .then(({ status, body }) => {
            if (status === 200) {
                showToast('Xóa mã giảm giá thành công!', 'success');
                loadDiscounts();
            } else {
                showToast(body.error || 'Không thể xóa mã giảm giá', 'error');
            }
        })
        .catch(err => {
            console.error(err);
            showToast('Lỗi kết nối máy chủ', 'error');
        });
}


// Update form based on discount type
function updateFormByType(type) {
    const valueInput = document.getElementById('discountValue');
    const valueSuffix = document.getElementById('valueSuffix');

    switch(type) {
        case 'percentage':
            valueSuffix.textContent = '%';
            valueInput.placeholder = 'VD: 20';
            valueInput.disabled = false;
            break;
        case 'fixed':
            valueSuffix.textContent = '₫';
            valueInput.placeholder = 'VD: 100000';
            valueInput.disabled = false;
            break;
        case 'shipping':
            valueSuffix.textContent = '';
            valueInput.placeholder = 'Miễn phí vận chuyển';
            valueInput.disabled = true;
            valueInput.value = '';
            break;
        default:
            valueSuffix.textContent = '';
            valueInput.placeholder = '';
            valueInput.disabled = false;
    }
}

// Search and filter functions
function getCurrentFilters() {
    return {
        search: document.getElementById('searchInput').value.trim(),
        status: document.getElementById('statusFilter').value
    };
}

function handleSearch() {
    const { search, status } = getCurrentFilters();
    loadDiscounts(0, search, status); // Trang đầu tiên
}

function handleStatusFilter() {
    const { search, status } = getCurrentFilters();
    loadDiscounts(0, search, status); // Trang đầu tiên
}


// Select all functionality
function handleSelectAll() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const rowCheckboxes = document.querySelectorAll('.row-checkbox');

    rowCheckboxes.forEach(checkbox => {
        checkbox.checked = selectAllCheckbox.checked;
    });
}

// Event listeners
function initializeEventListeners() {
    // Sidebar toggle
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('sidebar-toggle')) {
            e.preventDefault();
            toggleSidebar();
        }
    });

    // Sidebar overlay click
    sidebarOverlay.addEventListener('click', () => {
        if (isMobile) {
            closeMobileSidebar();
        }
    });

    // Modal events
    document.getElementById('addDiscountBtn').addEventListener('click', () => {
        openModal();
    });

    document.getElementById('closeModal').addEventListener('click', closeModal);
    document.getElementById('cancelBtn').addEventListener('click', closeModal);

    // Close modal when clicking overlay
    discountModal.addEventListener('click', (e) => {
        if (e.target === discountModal) {
            closeModal();
        }
    });

    // Form submission
    document.getElementById('discountForm').addEventListener('submit', handleFormSubmit);

    // Search input
    document.getElementById('searchInput').addEventListener('input', debounce(handleSearch, 300));

    // Filter selects
    document.getElementById('statusFilter').addEventListener('change', handleStatusFilter);

    // Toast close
    document.getElementById('toastClose').addEventListener('click', hideToast);

    // Window resize
    window.addEventListener('resize', () => {
        const wasMobile = isMobile;
        isMobile = window.innerWidth <= 768;

        if (wasMobile !== isMobile) {
            initializeSidebar();
            updateToggleIcon();
        }
    });

    // Keyboard shortcuts
    document.addEventListener('keydown', (e) => {
        // Ctrl+B to toggle sidebar
        if (e.ctrlKey && e.key === 'b') {
            e.preventDefault();
            toggleSidebar();
        }

        // Escape to close modal or sidebar
        if (e.key === 'Escape') {
            if (discountModal.classList.contains('show')) {
                closeModal();
            } else if (isMobile && sidebar.classList.contains('mobile-open')) {
                closeMobileSidebar();
            }
        }

        // Ctrl+N to add new discount
        if (e.ctrlKey && e.key === 'n') {
            e.preventDefault();
            openModal();
        }
    });
}

// Utility functions
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('vi-VN');
}

// Initialize tooltips (if using a tooltip library)
function initializeTooltips() {
    // Initialize tooltips here if needed
}

// Export functions for external use
window.editDiscount = editDiscount;
window.deleteDiscount = deleteDiscount;