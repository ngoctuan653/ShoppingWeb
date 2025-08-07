// Global variables
const sidebar = document.getElementById('sidebar');
const mainContent = document.getElementById('mainContent');
const sidebarOverlay = document.getElementById('sidebarOverlay');
let isMobile = window.innerWidth <= 768;
let sidebarCollapsed = false;

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

// Toggle sidebar function
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

// Open mobile sidebar
function openMobileSidebar() {
    sidebar.classList.add('mobile-open');
    sidebarOverlay.classList.add('show');
    document.body.style.overflow = 'hidden';
}

// Close mobile sidebar
function closeMobileSidebar() {
    sidebar.classList.remove('mobile-open');
    sidebarOverlay.classList.remove('show');
    document.body.style.overflow = 'auto';
}

// Update toggle icon
function updateToggleIcon() {
    const toggleBtns = document.querySelectorAll('.sidebar-toggle');
    const isOpen = isMobile ? sidebar.classList.contains('mobile-open') : !sidebarCollapsed;

    toggleBtns.forEach(btn => {
        btn.innerHTML = isOpen ? '✕' : '☰';
    });
}

// Show modal
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

// Hide modal
function hideModal(modalId) {
    const modal = document.getElementById(modalId);
    modal.classList.remove('show');
    document.body.style.overflow = 'auto';
}

// Show toast message
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastMessage = document.querySelector('.toast-message');
    const toastIcon = document.querySelector('.toast-icon');

    toastMessage.textContent = message;

    if (type === 'error') {
        toast.classList.add('error');
        toastIcon.textContent = '⚠';
    } else {
        toast.classList.remove('error');
        toastIcon.textContent = '✓';
    }

    toast.classList.add('show');

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// View order detail
function viewOrderDetail(orderId) {
    fetch(`/admin/order/${orderId}/detail`)
        .then(response => response.json())
        .then(orderData => {
            const statusOptions = orderData.statusOptions.map(status =>
                `<option value="${status.id}" ${status.name === orderData.status ? 'selected' : ''}>${status.name}</option>`
            ).join('');

            const productsHtml = orderData.products.map(product => `
                <div class="detail-row">
                    <span class="detail-label">${product.name}</span>
                    <span class="detail-value">${product.quantity} x ${formatCurrency(product.price)}</span>
                </div>
            `).join('');

            document.getElementById('modalBody').innerHTML = `
                <div class="order-detail">
                    <div class="detail-section">
                        <h3 class="detail-title">Order Information</h3>
                        <div class="detail-row">
                            <span class="detail-label">ID:</span>
                            <span class="detail-value">${orderData.orderCode}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Created At:</span>
                            <span class="detail-value">${formatDate(orderData.date)}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Status:</span>
                            <select class="status-selector" id="orderStatus">
                                ${statusOptions}
                            </select>
                        </div>
                    </div>

                    <div class="detail-section">
                        <h3 class="detail-title">Customer Information</h3>
                        <div class="detail-row">
                            <span class="detail-label">Customer Name:</span>
                            <span class="detail-value">${orderData.customer}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Phone Number:</span>
                            <span class="detail-value">${orderData.phone}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Email:</span>
                            <span class="detail-value">${orderData.email}</span>
                        </div>
                        <div class="detail-row">
                            <span class="detail-label">Address:</span>
                            <span class="detail-value">${orderData.address}</span>
                        </div>
                    </div>

                    <div class="detail-section">
                        <h3 class="detail-title">Product</h3>
                        ${productsHtml}
                        <div class="detail-row" style="font-weight: 600; border-top: 1px solid #ddd; padding-top: 8px; margin-top: 8px;">
                            <span class="detail-label">Discount:</span>
                            <span class="detail-value">${formatCurrency(orderData.discount)}</span>
                        </div>
                        <div class="detail-row" style="font-weight: 600; border-top: 1px solid #ddd; padding-top: 8px; margin-top: 8px;">
                            <span class="detail-label">Total Amount:</span>
                            <span class="detail-value">${formatCurrency(orderData.total)}</span>
                        </div>
                    </div>
                </div>
            `;

            document.getElementById('updateOrderBtn').onclick = () => updateOrderFromModal(orderId);
            showModal('orderDetailModal');
        })
        .catch(err => {
            alert("Không thể tải thông tin đơn hàng");
            console.error(err);
        });
}

// Update order from modal
function updateOrderFromModal(orderId) {
    const statusSelect = document.getElementById('orderStatus');
    const selectedStatusId = statusSelect?.value;

    if (!selectedStatusId) {
        showToast("Vui lòng chọn trạng thái!", 'error');
        return;
    }

    fetch(`/order/${orderId}/status`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ statusId: selectedStatusId })
    })
        .then(async res => {
            const data = await res.json();

            if (!res.ok) {
                showToast(data.message || "Updated Failed!", 'error');
                throw new Error(data.message);
            }

            showToast(data.message || "Updated Successfully!", 'success');
            updateStats();
            hideModal('orderDetailModal');

            const row = document.querySelector(`.order-checkbox[data-order-id="${orderId}"]`)?.closest('tr');
            if (row) {
                const newStatusText = statusSelect.options[statusSelect.selectedIndex].text;
                const statusSpan = row.querySelector('.status-badge');

                if (statusSpan) {
                    statusSpan.textContent = newStatusText;
                    statusSpan.className = 'status-badge'; // reset all status classes

                    const classMap = {
                        'pending': 'status-pending',
                        'confirmed': 'status-processing',
                        'shipped': 'status-shipping',
                        'delivered': 'status-completed',
                        'cancelled': 'status-cancelled'
                    };

                    const statusKey = newStatusText.toLowerCase(); // ensure lower case
                    const cssClass = classMap[statusKey];
                    if (cssClass) {
                        statusSpan.classList.add(cssClass);
                    }
                }

                updateActionButtons(orderId, newStatusText.toLowerCase());
            }
        })
        .catch(err => {
            console.error("Lỗi cập nhật trạng thái:", err);
            showToast("Cập nhật thất bại!", 'error');
        });
}

// Update order status directly
function updateOrderStatus(orderId, newStatus) {
    const statusMap = {
        'pending': 'Pending',
        'confirmed': 'Confirmed',
        'shipped': 'Shipping',
        'completed': 'Completed',
        'cancelled': 'Cancelled'
    };


    const statusIdMap = {
        'pending': 1,
        'confirmed': 2,
        'shipped': 3,
        'completed': 4,
        'cancelled': 5
    };

    const statusId = statusIdMap[newStatus];
    if (!statusId) {
        showToast("Trạng thái không hợp lệ!", 'error');
        return;
    }

    fetch(`/order/${orderId}/status`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({statusId}) // đúng kiểu Integer
    })
        .then(async res => {
            const data = await res.json();
            if (!res.ok) {
                showToast(data.message || "Updated Failed!", 'error');
                throw new Error(data.message);
            }

            showToast(data.message || "Updated Successfully!", 'success');
            updateStats();

            // ✅ Cập nhật giao diện
            const row = document.querySelector(`.order-checkbox[data-order-id="${orderId}"]`)?.closest('tr');
            if (row) {
                const statusSpan = row.querySelector('.status-badge');
                if (statusSpan) {
                    statusSpan.textContent = statusMap[newStatus];
                    statusSpan.className = 'status-badge';

                    const classMap = {
                        'pending': 'status-pending',
                        'confirmed': 'status-processing',
                        'shipping': 'status-shipping',
                        'completed': 'status-completed',
                        'cancelled': 'status-cancelled'
                    };
                    statusSpan.classList.add(classMap[newStatus]);
                }
                updateActionButtons(orderId, newStatus);

            }

        })
        .catch(err => {
            console.error("Lỗi cập nhật trạng thái:", err);
            showToast("Cập nhật thất bại!", 'error');
        });
}

function updateActionButtons(orderId, newStatus) {
    const row = document.querySelector(`.order-checkbox[data-order-id="${orderId}"]`)?.closest('tr');
    const actionDiv = row?.querySelector('.action-buttons');

    if (!actionDiv) return;

    let buttonsHtml = `
        <button class="btn-sm btn-primary" onclick="viewOrderDetail(${orderId})">View</button>
    `;

    if (newStatus === 'pending') {
        buttonsHtml += `
            <button class="btn-sm btn-success" onclick="updateOrderStatus(${orderId}, 'confirmed')" data-action="confirmed-${orderId}">Handle</button>
            <button class="btn-sm btn-danger" onclick="updateOrderStatus(${orderId}, 'cancelled')" data-action="cancelled-${orderId}">Cancel</button>
        `;
    } else if (newStatus === 'confirmed') {
        buttonsHtml += `
            <button class="btn-sm btn-danger" onclick="updateOrderStatus(${orderId}, 'cancelled')" data-action="cancelled-${orderId}">Cancel</button>
        `;
    }

    actionDiv.innerHTML = buttonsHtml;
}

function loadDashboardStats() {
    fetch('/admin/dashboard-stats')
        .then(res => res.json())
        .then(data => {
            document.getElementById('stat-total-orders').textContent = data.totalOrders;
            document.getElementById('stat-pending-orders').textContent = data.pendingOrders;
            document.getElementById('stat-shipping-orders').textContent = data.shippingOrders;
            document.getElementById('stat-cancelled-orders').textContent = data.cancelledOrders;
        })
        .catch(err => console.error("Lỗi tải thống kê:", err));
}


function updateStats() {
    fetch('/admin/dashboard-stats')
        .then(res => res.json())
        .then(data => {
            console.log('Dashboard stats:', data);

            document.getElementById('stat-total-orders').textContent = data.totalOrders;
            document.getElementById('stat-pending-orders').textContent = data.pendingOrders;
            document.getElementById('stat-shipping-orders').textContent = data.shippingOrders;
            document.getElementById('stat-cancelled-orders').textContent = data.cancelledOrders;
        })
        .catch(err => {
            console.error("Lỗi tải thống kê:", err);
        });
}


// Show bulk action modal
function showBulkActionModal() {
    const selectedCount = document.querySelectorAll('.order-checkbox:checked').length;

    if (selectedCount === 0) {
        showToast('Vui lòng chọn ít nhất 1 đơn hàng', 'error');
        return;
    }

    document.getElementById('bulkSelectedCount').textContent = selectedCount;
    showModal('bulkActionModal');
}

// Execute bulk action
function executeBulkAction(action) {
    const selectedCheckboxes = document.querySelectorAll('.order-checkbox:checked');
    const selectedCount = selectedCheckboxes.length;

    const classMap = {
        'pending': 'status-pending',
        'confirmed': 'status-processing',
        'shipping': 'status-shipping',
        'completed': 'status-completed',
        'cancelled': 'status-cancelled'
    };

    showToast(`Updated ${selectedCount} orders to status "${action}"`);


    // Here you would typically send an API request for bulk update
    console.log(`Bulk updating ${selectedCount} orders to status: ${action}`);

    // Uncheck all checkboxes
    selectedCheckboxes.forEach(checkbox => {
        checkbox.checked = false;
        checkbox.closest('tr').classList.remove('selected');
    });

    // Hide bulk action button
    document.getElementById('bulkActionBtn').style.display = 'none';
    document.getElementById('selectAll').checked = false;

    hideModal('bulkActionModal');
    showToast(`Đã cập nhật ${selectedCount} đơn hàng thành "${statusMap[action]}"`);
}

// Handle select all checkbox
function handleSelectAll() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const orderCheckboxes = document.querySelectorAll('.order-checkbox');
    const bulkActionBtn = document.getElementById('bulkActionBtn');
    const selectedCount = document.getElementById('selectedCount');

    orderCheckboxes.forEach(checkbox => {
        checkbox.checked = selectAllCheckbox.checked;
        const row = checkbox.closest('tr');
        if (selectAllCheckbox.checked) {
            row.classList.add('selected');
        } else {
            row.classList.remove('selected');
        }
    });

    const count = selectAllCheckbox.checked ? orderCheckboxes.length : 0;
    if (count > 0) {
        bulkActionBtn.style.display = 'block';
        selectedCount.textContent = count;
    } else {
        bulkActionBtn.style.display = 'none';
    }
}

// Handle individual checkbox
function handleOrderCheckbox() {
    const orderCheckboxes = document.querySelectorAll('.order-checkbox');
    const checkedBoxes = document.querySelectorAll('.order-checkbox:checked');
    const selectAllCheckbox = document.getElementById('selectAll');
    const bulkActionBtn = document.getElementById('bulkActionBtn');
    const selectedCount = document.getElementById('selectedCount');

    // Update select all checkbox
    selectAllCheckbox.checked = checkedBoxes.length === orderCheckboxes.length;
    selectAllCheckbox.indeterminate = checkedBoxes.length > 0 && checkedBoxes.length < orderCheckboxes.length;

    // Update bulk action button
    if (checkedBoxes.length > 0) {
        bulkActionBtn.style.display = 'block';
        selectedCount.textContent = checkedBoxes.length;
    } else {
        bulkActionBtn.style.display = 'none';
    }

    // Update row styling
    orderCheckboxes.forEach(checkbox => {
        const row = checkbox.closest('tr');
        if (checkbox.checked) {
            row.classList.add('selected');
        } else {
            row.classList.remove('selected');
        }
    });
}

// Utility functions
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString('vi-VN');
}

// Event listeners
document.addEventListener('DOMContentLoaded', function () {
    // Initialize
    initializeSidebar();
    updateToggleIcon();
    loadDashboardStats();

    // Sidebar toggle
    document.getElementById('sidebarToggle').addEventListener('click', (e) => {
        e.preventDefault();
        toggleSidebar();
    });

    // Sidebar overlay
    sidebarOverlay.addEventListener('click', () => {
        if (isMobile) {
            closeMobileSidebar();
        }
    });

    // Select all checkbox
    document.getElementById('selectAll').addEventListener('change', handleSelectAll);

    // Order checkboxes (event delegation)
    document.getElementById('ordersTableBody').addEventListener('change', (e) => {
        if (e.target.classList.contains('order-checkbox')) {
            handleOrderCheckbox();
        }
    });

    // Bulk action button
    document.getElementById('bulkActionBtn').addEventListener('click', showBulkActionModal);

    // Bulk action buttons
    document.querySelectorAll('.bulk-action-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const action = btn.dataset.action;
            executeBulkAction(action);
        });
    });

    // Modal close buttons
    document.getElementById('closeModal').addEventListener('click', () => {
        hideModal('orderDetailModal');
    });

    document.getElementById('cancelModalBtn').addEventListener('click', () => {
        hideModal('orderDetailModal');
    });

    // Modal overlay clicks
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                overlay.classList.remove('show');
                document.body.style.overflow = 'auto';
            }
        });
    });

    // Keyboard shortcuts
    document.addEventListener('keydown', (e) => {
        // Ctrl+B for sidebar toggle
        if (e.ctrlKey && e.key === 'b') {
            e.preventDefault();
            toggleSidebar();
        }

        // Escape to close modals
        if (e.key === 'Escape') {
            const openModals = document.querySelectorAll('.modal-overlay.show');
            openModals.forEach(modal => {
                modal.classList.remove('show');
                document.body.style.overflow = 'auto';
            });

            if (isMobile && sidebar.classList.contains('mobile-open')) {
                closeMobileSidebar();
            }
        }
    });

    // Window resize handler
    window.addEventListener('resize', () => {
        const wasMobile = isMobile;
        isMobile = window.innerWidth <= 768;

        if (wasMobile !== isMobile) {
            initializeSidebar();
            updateToggleIcon();
        }
    });
});

// Global functions for HTML onclick handlers
window.viewOrderDetail = viewOrderDetail;
window.updateOrderStatus = updateOrderStatus;
window.showBulkActionModal = showBulkActionModal;
window.executeBulkAction = executeBulkAction;