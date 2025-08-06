// Global variables
const sidebar = document.getElementById('sidebar');
const mainContent = document.getElementById('mainContent');
const sidebarOverlay = document.getElementById('sidebarOverlay');
const addUserModal = document.getElementById('addUserModal');
const editUserModal = document.getElementById('editUserModal');
const notification = document.getElementById('notification');

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

// Show notification
function showNotification(message, type = 'info') {
    const notificationContent = notification.querySelector('.notification-content');
    const messageEl = notification.querySelector('.notification-message');
    const iconEl = notification.querySelector('.notification-icon');

    // Reset classes
    notification.className = 'notification';
    notification.classList.add(type);

    // Set icon based on type
    const icons = {
        success: '✅',
        error: '❌',
        warning: '⚠️',
        info: 'ℹ️'
    };

    iconEl.textContent = icons[type] || icons.info;
    messageEl.textContent = message;

    // Show notification
    notification.classList.add('show');

    // Auto hide after 3 seconds
    setTimeout(() => {
        notification.classList.remove('show');
    }, 3000);
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

// Add user
function addUser() {
    showModal('addUserModal');
}

// Edit user - placeholder function
function editUser(userId) {
    fetch(`/admin/users/get/${userId}`)
        .then(res => res.json())
        .then(user => {
            document.getElementById('editUserId').value = user.id;
            document.getElementById('editFullName').value = user.fullName || '';
            document.getElementById('editEmail').value = user.email || '';
            document.getElementById('editPhone').value = user.phoneNumber || '';
            document.getElementById('editStatus').value = user.status?.toLowerCase() || 'active';

            loadRoles(user.roleId); // <-- Load role select và chọn đúng vai trò
            showModal('editUserModal');
        })
        .catch(err => {
            console.error(err);
            showNotification("Không thể tải thông tin người dùng!", "error");
        });
}


function loadRoles(selectedRoleId) {
    fetch('/admin/users/roles')
        .then(res => res.json())
        .then(roles => {
            const roleSelect = document.getElementById('editRole');
            roleSelect.innerHTML = ''; // Clear current options

            roles.forEach(role => {
                const option = document.createElement('option');
                option.value = role.id;
                option.textContent = role.roleName;

                if (role.id === selectedRoleId) {
                    option.selected = true;
                }

                roleSelect.appendChild(option);
            });
        })
        .catch(err => {
            console.error("Không thể load danh sách vai trò:", err);
        });
}

function fetchUsers(page = 0) {
    const keyword = document.getElementById("searchInput").value;
    const roleId = document.getElementById("roleFilter").value;
    const status = document.getElementById("statusFilter").value;

    const url = `/admin/users/api?keyword=${encodeURIComponent(keyword)}&roleId=${roleId}&status=${status}&page=${page}`;

    fetch(url)
        .then(res => res.json())
        .then(data => {
            renderUsersTable(data.content);
            renderPagination(data.totalPages, data.number);
        })
        .catch(err => {
            console.error(err);
            showNotification("Lỗi tải danh sách người dùng", "error");
        });
}

function renderUsersTable(users) {
    const tbody = document.getElementById("usersTableBody");
    tbody.innerHTML = "";

    users.forEach(user => {
        const row = document.createElement("tr");
        row.id = `userRow-${user.id}`;
        row.setAttribute("data-user-id", user.id);

        row.innerHTML = `
            <td></td>
            <td>
                <div class="user-avatar-table">
                    <img src="/avatar/${user.id}" onerror="this.src='/images/default.png'" alt="Avatar" />
                </div>
            </td>
            <td>
                <div class="user-info">
                    <div class="user-details">
                        <h4 class="user-fullname">${user.fullName}</h4>
                        <p class="user-email">${user.email}</p>
                    </div>
                </div>
            </td>
            <td>
                <span class="user-role role-badge role-${user.roleName.toLowerCase().replace('role_', '')}">
                    ${user.roleName.replace('ROLE_', '')}
                </span>
            </td>
            <td>
                <span class="user-status status-badge ${user.status.toLowerCase() === 'active' ? 'status-active' : 'status-blocked'}">
                    ${user.status}
                </span>
            </td>
            <td>${formatDateTime(user.createdAt)}</td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn btn-success" onclick="editUser(${user.id})" title="Chỉnh sửa">✏️</button>
                </div>
            </td>
        `;

        tbody.appendChild(row);
    });
}

function renderPagination(totalPages, currentPage) {
    const pagination = document.getElementById("pagination");
    pagination.innerHTML = "";

    const prevBtn = document.createElement("button");
    prevBtn.className = "page-btn";
    prevBtn.textContent = "‹";
    prevBtn.disabled = currentPage === 0;
    prevBtn.onclick = () => fetchUsers(currentPage - 1);
    pagination.appendChild(prevBtn);

    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement("button");
        btn.className = "page-btn" + (i === currentPage ? " active" : "");
        btn.textContent = i + 1;
        btn.onclick = () => fetchUsers(i);
        pagination.appendChild(btn);
    }

    const nextBtn = document.createElement("button");
    nextBtn.className = "page-btn";
    nextBtn.textContent = "›";
    nextBtn.disabled = currentPage === totalPages - 1;
    nextBtn.onclick = () => fetchUsers(currentPage + 1);
    pagination.appendChild(nextBtn);
}

function formatDateTime(dateStr) {
    const date = new Date(dateStr);
    return date.toLocaleDateString('vi-VN') + ' ' + date.toLocaleTimeString('vi-VN');
}


// Delete user - placeholder function
function deleteUser(userId) {
    if (!confirm('Bạn có chắc chắn muốn xóa người dùng này?')) return;
    showNotification(`Đã xóa người dùng ID: ${userId}`, 'success');
}

// Handle select all checkbox
function handleSelectAll() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const rowCheckboxes = document.querySelectorAll('#usersTableBody input[type="checkbox"]');

    rowCheckboxes.forEach(checkbox => {
        checkbox.checked = selectAllCheckbox.checked;
    });
}

// Export to Excel - placeholder function
function exportToExcel() {
    showNotification('Đang xuất dữ liệu Excel...', 'info');
    setTimeout(() => {
        showNotification('Đã xuất Excel thành công!', 'success');
    }, 2000);
}

// Refresh data - placeholder function
function refreshData() {
    showNotification('Đang làm mới dữ liệu...', 'info');
    setTimeout(() => {
        showNotification('Đã làm mới dữ liệu thành công!', 'success');
    }, 1500);
}

// Search function - placeholder
function searchUsers() {
    const searchInput = document.getElementById('searchInput');
    const searchTerm = searchInput.value;

    if (searchTerm) {
        showNotification(`Searching: "${searchTerm}"`, 'info');
    }
}

// Filter functions - placeholder
function filterByStatus() {
    const statusFilter = document.getElementById('statusFilter');
    const selectedStatus = statusFilter.value;

    if (selectedStatus) {
        showNotification(`Đã lọc theo trạng thái: ${selectedStatus}`, 'info');
    }
}

function filterByRole() {
    const roleFilter = document.getElementById('roleFilter');
    const selectedRole = roleFilter.value;

    if (selectedRole) {
        showNotification(`Đã lọc theo vai trò: ${selectedRole}`, 'info');
    }
}

// Event listeners
document.addEventListener('DOMContentLoaded', function() {
    // Initialize
    initializeSidebar();
    updateToggleIcon();
    fetchUsers();

    document.getElementById("searchInput").addEventListener("input", () => fetchUsers());
    document.getElementById("statusFilter").addEventListener("change", () => fetchUsers());
    document.getElementById("roleFilter").addEventListener("change", () => fetchUsers());
    // Sidebar events
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('sidebar-toggle')) {
            e.preventDefault();
            toggleSidebar();
        }
    });

    sidebarOverlay.addEventListener('click', () => {
        if (isMobile) {
            closeMobileSidebar();
        }
    });

    // Search and filter events
    document.getElementById('searchInput').addEventListener('input', searchUsers);
    document.getElementById('statusFilter').addEventListener('change', filterByStatus);
    document.getElementById('roleFilter').addEventListener('change', filterByRole);

    // Button events
    // document.getElementById('addUserBtn').addEventListener('click', addUser);
    // document.getElementById('exportBtn').addEventListener('click', exportToExcel);
    // document.getElementById('refreshBtn').addEventListener('click', refreshData);

    // Modal events
    document.getElementById('closeEditModal').addEventListener('click', () => hideModal('editUserModal'));
    document.getElementById('cancelEditBtn').addEventListener('click', () => hideModal('editUserModal'));

    document.getElementById('editUserForm').addEventListener('submit', function (e) {
        e.preventDefault();
        const formData = new FormData(this);

        fetch('/admin/users/update', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                if (!response.ok) throw new Error('Cập nhật thất bại');
                return response.text();
            })
            .then(() => {
                showNotification('Updated successfully!', 'success');
                hideModal('editUserModal');

                // Cập nhật bảng
                const userId = document.getElementById('editUserId').value;
                const fullName = document.getElementById('editFullName').value;
                const email = document.getElementById('editEmail').value;
                const roleName = document.getElementById('editRole').selectedOptions[0].text;
                const status = document.getElementById('editStatus').value;

                const row = document.getElementById(`userRow-${userId}`);
                if (row) {
                    row.querySelector('.user-fullname').textContent = fullName;
                    row.querySelector('.user-email').textContent = email;

                    const roleSpan = row.querySelector('.user-role');
                    roleSpan.textContent = roleName;
                    roleSpan.className = 'user-role role-badge role-' + roleName.toLowerCase();

                    const statusSpan = row.querySelector('.user-status');
                    statusSpan.textContent = status.charAt(0).toUpperCase() + status.slice(1);
                    statusSpan.className = 'user-status status-badge ' + (status.toLowerCase() === 'active' ? 'status-active' : 'status-blocked');
                }
            })
            .catch(err => {
                console.error(err);
                showNotification('Updated Failed', 'error');
            });
    });


    editUserModal.addEventListener('click', (e) => {
        if (e.target === editUserModal) {
            hideModal('editUserModal');
        }
    });

    // Pagination click events
    document.querySelectorAll('.page-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            if (this.disabled) return;

            // Remove active class from all buttons
            document.querySelectorAll('.page-btn').forEach(b => b.classList.remove('active'));

            // Add active class to clicked button (if it's a number)
            if (!isNaN(this.textContent)) {
                this.classList.add('active');
                showNotification(`Chuyển đến trang ${this.textContent}`, 'info');
            }
        });
    });
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

// Keyboard shortcuts
document.addEventListener('keydown', (e) => {
    // Ctrl+B to toggle sidebar
    if (e.ctrlKey && e.key === 'b') {
        e.preventDefault();
        toggleSidebar();
    }

    // Escape to close modals or mobile sidebar
    if (e.key === 'Escape') {
        if (addUserModal.classList.contains('show')) {
            hideModal('addUserModal');
        } else if (editUserModal.classList.contains('show')) {
            hideModal('editUserModal');
        } else if (isMobile && sidebar.classList.contains('mobile-open')) {
            closeMobileSidebar();
        }
    }

    // Ctrl+N to add new user
    if (e.ctrlKey && e.key === 'n') {
        e.preventDefault();
        addUser();
    }

    // Ctrl+R to refresh
    if (e.ctrlKey && e.key === 'r') {
        e.preventDefault();
        refreshData();
    }
});
