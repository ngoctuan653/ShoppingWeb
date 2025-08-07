// Global variables
const sidebar = document.getElementById('sidebar');
const mainContent = document.getElementById('mainContent');
const sidebarOverlay = document.getElementById('sidebarOverlay');
const replyModal = document.getElementById('replyModal');
let isMobile = window.innerWidth <= 768;
let sidebarCollapsed = false;
let currentReviewCard = null;

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    initializeSidebar();
    updateToggleIcon();
    setupEventListeners();
    loadAdminReviews();
    loadReviewStats();
});

function loadReviewStats() {
    fetch("/api/admin/review-stats")
        .then(res => res.json())
        .then(data => {
            document.getElementById("totalReviews").textContent = data.totalReviews.toLocaleString();
            document.getElementById("pendingReviews").textContent = data.pendingReviews.toLocaleString();
            document.getElementById("averageRating").textContent = data.averageRating.toFixed(1);
        })
        .catch(err => {
            console.error("Lỗi khi tải thống kê đánh giá:", err);
        });
}


function loadAdminReviews() {
    const ratingFilter = document.getElementById('ratingFilter').value;
    const statusFilter = document.getElementById('statusFilter').value;

    let url = `/api/admin/reviews`;
    const params = [];

    if (ratingFilter) params.push(`rating=${ratingFilter}`);
    if (statusFilter) params.push(`status=${statusFilter}`);

    if (params.length > 0) {
        url += '?' + params.join('&');
    }

    fetch(url)
        .then(res => res.json())
        .then(data => {
            const container = document.getElementById("reviews-container");
            container.innerHTML = ""; // Xoá cũ

            data.forEach(review => {
                const ratingStars = "★".repeat(review.rating) + "☆".repeat(5 - review.rating);
                const replied = review.adminReply && review.adminReply.trim().length > 0;
                const date = new Date(review.createdAt).toLocaleDateString("vi-VN", {
                    day: "numeric", month: "short", year: "numeric"
                });

                const reviewCard = `
                    <div class="review-card" data-review-id="${review.id}" data-rating="${review.rating}" data-status="${replied ? 'replied' : 'pending'}">
                        <div class="review-header">
                            <div class="review-user">
                                <div class="user-avatar">${review.username.charAt(0).toUpperCase()}</div>
                                <div class="user-info">
                                    <div class="user-name">${review.username}</div>
                                    <div class="review-date">${date}</div>
                                </div>
                            </div>
                            <div class="review-rating">
                                <div class="stars">${ratingStars}</div>
                                <span class="status-badge ${replied ? 'status-replied' : 'status-pending'}">
                                    ${replied ? "Replied" : "Pending"}
                                </span>
                            </div>
                        </div>
                        <div class="review-product">
                            <img src="/products/image/${review.productId}" alt="Product" width="60">
                            <div class="product-info">
                                <div class="product-name">${review.productName}</div>
                                <div class="product-id">Product ID: ${review.productId}</div>
                            </div>
                        </div>
                        <div class="review-content">
                            <p>${review.comment}</p>
                        </div>
                        ${replied ? `
                            <div class="admin-reply">
                                <div class="reply-header"><span class="reply-label">Admin:</span></div>
                                <div class="reply-content">${review.adminReply}</div>
                            </div>
                        ` : ""}
                        <div class="review-actions">
                            ${replied ? `
                                <button class="btn-edit" onclick="editReply(this)">✏️Edit</button>
                            ` : `
                                <button class="btn-reply" onclick="openReplyModal(this)">💬Reply</button>
                            `}
<!--                            <button class="btn-hide" onclick="hideReview(this)">Hide</button>-->
                        </div>
                    </div>
                `;

                container.insertAdjacentHTML("beforeend", reviewCard);
            });
        })
        .catch(err => {
            console.error("Lỗi khi tải danh sách đánh giá:", err);
        });
}


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

// Event listeners
function setupEventListeners() {
    // Sidebar toggle
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('sidebar-toggle')) {
            e.preventDefault();
            toggleSidebar();
        }
    });

    // Overlay click
    sidebarOverlay.addEventListener('click', () => {
        if (isMobile) closeMobileSidebar();
    });

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
        if (e.ctrlKey && e.key === 'b') {
            e.preventDefault();
            toggleSidebar();
        }
        if (e.key === 'Escape') {
            if (isMobile && sidebar.classList.contains('mobile-open')) {
                closeMobileSidebar();
            }
            if (replyModal.classList.contains('show')) {
                closeReplyModal();
            }
        }
    });

    //filter
    document.getElementById('ratingFilter').addEventListener('change', loadAdminReviews);
    document.getElementById('statusFilter').addEventListener('change', loadAdminReviews);

}

// Modal functions
function openReplyModal(button) {
    currentReviewCard = button.closest('.review-card');
    const userName = currentReviewCard.querySelector('.user-name').textContent;
    const stars = currentReviewCard.querySelector('.stars').textContent;
    const reviewContent = currentReviewCard.querySelector('.review-content p').textContent;

    document.getElementById('previewUser').textContent = userName;
    document.getElementById('previewRating').textContent = stars;
    document.getElementById('previewContent').textContent = reviewContent;
    document.getElementById('replyText').value = '';

    replyModal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

function closeReplyModal() {
    replyModal.classList.remove('show');
    document.body.style.overflow = 'auto';
    currentReviewCard = null;
}

function sendReply() {
    const replyText = document.getElementById('replyText').value.trim();

    if (!replyText) {
        showToast('Vui lòng nhập nội dung phản hồi', 'warning');
        return;
    }

    const reviewId = currentReviewCard.getAttribute("data-review-id");

    fetch(`/api/admin/reviews/${reviewId}/reply`, {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ reply: replyText })
    })
        .then(res => {
            if (!res.ok) throw new Error("Lỗi khi gửi phản hồi");
            return res.json();
        })
        .then(() => {
            const adminReplyHTML = `
            <div class="admin-reply">
                <div class="reply-header"><span class="reply-label">Admin:</span></div>
                <div class="reply-content">${replyText}</div>
            </div>
        `;

            const existingReply = currentReviewCard.querySelector('.admin-reply');
            if (existingReply) existingReply.remove();

            const reviewContent = currentReviewCard.querySelector('.review-content');
            reviewContent.insertAdjacentHTML('afterend', adminReplyHTML);

            // Update status badge
            const statusBadge = currentReviewCard.querySelector('.status-badge');
            statusBadge.textContent = 'Đã phản hồi';
            statusBadge.className = 'status-badge status-replied';

            // Update data attribute
            currentReviewCard.setAttribute('data-status', 'replied');

            // Update action buttons
            const actionsDiv = currentReviewCard.querySelector('.review-actions');
            actionsDiv.innerHTML = `
            <button class="btn-edit" onclick="editReply(this)">✏️ Sửa phản hồi</button>
            <button class="btn-hide" onclick="hideReview(this)">🙈 Ẩn</button>
        `;

            closeReplyModal();
            showToast('Đã gửi phản hồi thành công!', 'success');
        })
        .catch(() => {
            showToast('Lỗi khi gửi phản hồi!', 'error');
        });
}


function editReply(button) {
    currentReviewCard = button.closest('.review-card');
    const existingReply = currentReviewCard.querySelector('.reply-content').textContent;

    const userName = currentReviewCard.querySelector('.user-name').textContent;
    const stars = currentReviewCard.querySelector('.stars').textContent;
    const reviewContent = currentReviewCard.querySelector('.review-content p').textContent;

    document.getElementById('previewUser').textContent = userName;
    document.getElementById('previewRating').textContent = stars;
    document.getElementById('previewContent').textContent = reviewContent;
    document.getElementById('replyText').value = existingReply;

    replyModal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

function hideReview(button) {
    const reviewCard = button.closest('.review-card');
    reviewCard.style.opacity = '0.5';
    reviewCard.style.pointerEvents = 'none';

    // Add hidden indicator
    const header = reviewCard.querySelector('.review-header');
    if (!header.querySelector('.hidden-badge')) {
        header.insertAdjacentHTML('beforeend', '<span class="status-badge hidden-badge" style="background: #fee2e2; color: #991b1b;">Đã ẩn</span>');
    }

    showToast('Đã ẩn đánh giá', 'success');
}

// Filter function
function filterReviews() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const ratingFilter = document.getElementById('ratingFilter').value;
    const statusFilter = document.getElementById('statusFilter').value;

    const reviewCards = document.querySelectorAll('.review-card');

    reviewCards.forEach(card => {
        const userName = card.querySelector('.user-name').textContent.toLowerCase();
        const productName = card.querySelector('.product-name').textContent.toLowerCase();
        const rating = card.getAttribute('data-rating');
        const status = card.getAttribute('data-status');

        let showCard = true;

        // Search filter
        if (searchTerm && !userName.includes(searchTerm) && !productName.includes(searchTerm)) {
            showCard = false;
        }

        // Rating filter
        if (ratingFilter && rating !== ratingFilter) {
            showCard = false;
        }

        // Status filter
        if (statusFilter && status !== statusFilter) {
            showCard = false;
        }

        card.style.display = showCard ? 'block' : 'none';
    });
}

// Toast function
function showToast(message, type = 'success') {
    const toastContainer = document.getElementById('toastContainer');

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    const icons = {
        success: '✓',
        error: '✗',
        warning: '⚠'
    };

    toast.innerHTML = `
        <span class="toast-icon">${icons[type] || '✓'}</span>
        <span class="toast-message">${message}</span>
    `;

    toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, 3000);
}