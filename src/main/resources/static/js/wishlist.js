document.addEventListener('DOMContentLoaded', function () {
    const wishlistBtn = document.querySelector('.add-to-wishlist-btn');

    if (wishlistBtn) {
        const productId = wishlistBtn.getAttribute('data-id');

        wishlistBtn.addEventListener('click', function () {
            const isInWishlist = wishlistBtn.classList.contains('btn-dark');

            if (!isInWishlist) {
                // === Add to wishlist ===
                fetch(`/wishlist/add/${productId}`, {
                    method: 'POST',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                })
                    .then(response => {
                        if (!response.ok) {
                            if (response.status === 401) {
                                showToast("Vui lòng đăng nhập để thêm vào wishlist.", "warning");
                                window.location.href = "/login";
                            } else {
                                showToast("Thêm vào wishlist thất bại!", "danger");
                            }
                            throw new Error("Request failed with status " + response.status);
                        }
                        return response.text();
                    })
                    .then(message => {
                        showToast(message, "success"); // "Added to wishlist"
                        wishlistBtn.classList.remove('btn-outline-dark');
                        wishlistBtn.classList.add('btn-dark');
                        wishlistBtn.innerHTML = '<i class="bi bi-heart-fill me-2"></i>Remove from Wishlist';
                    })
                    .catch(err => {
                        console.error("Lỗi khi thêm vào wishlist:", err);
                        showToast("Đã xảy ra lỗi!", "danger");
                    });
            } else {
                // === Remove from wishlist ===
                fetch(`/wishlist/remove`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: `productId=${productId}`
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error("Failed to remove from wishlist");
                        }
                        return response.text();
                    })
                    .then(message => {
                        showToast("Remove to wishlist.", "danger");
                        wishlistBtn.classList.remove('btn-dark');
                        wishlistBtn.classList.add('btn-outline-dark');
                        wishlistBtn.innerHTML = '<i class="bi bi-heart me-2"></i>Add to Wishlist';
                    })
                    .catch(err => {
                        console.error("Lỗi khi xóa khỏi wishlist:", err);
                        showToast("Xóa khỏi wishlist thất bại!", "danger");
                    });
            }
        });
    }
});
