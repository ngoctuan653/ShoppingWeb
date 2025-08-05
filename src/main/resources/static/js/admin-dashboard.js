// Fixed JavaScript cho dashboard
document.addEventListener('DOMContentLoaded', function () {
    // Global variables
    const sidebar = document.getElementById('sidebar') || document.querySelector('.sidebar');
    const mainContent = document.getElementById('mainContent') || document.querySelector('.main-content');
    const sidebarOverlay = document.getElementById('sidebarOverlay') || document.querySelector('.sidebar-overlay');
    const sidebarToggle = document.getElementById('sidebarToggle') || document.querySelector('.sidebar-toggle');

    // Kiểm tra xem các element có tồn tại không
    if (!sidebar || !mainContent || !sidebarOverlay || !sidebarToggle) {
        console.error('Không tìm thấy một hoặc nhiều element cần thiết cho sidebar');
        return;
    }

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
        if (sidebarToggle) {
            sidebarToggle.innerHTML = sidebarCollapsed || sidebar.classList.contains('mobile-open') ? '☰' : '✕';
        }
    }

    // Sidebar toggle click event
    sidebarToggle.addEventListener('click', function(e) {
        e.preventDefault();
        toggleSidebar();
    });

    // Overlay click to close sidebar
    sidebarOverlay.addEventListener('click', function() {
        if (isMobile) {
            closeMobileSidebar();
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

    // Keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        if (e.ctrlKey && e.key === 'b') {
            e.preventDefault();
            toggleSidebar();
        }

        if (e.key === 'Escape' && isMobile && sidebar.classList.contains('mobile-open')) {
            closeMobileSidebar();
        }
    });

    // Initialize on page load
    initializeSidebar();
    updateToggleIcon();
});