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
                                showToast("Please log in to add to wishlist.", "warning");
                                window.location.href = "/login";
                            } else {
                                showToast("Failed to add to wishlist!", "danger");
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
                        console.error("Error adding to wishlist:", err);
                        showToast("An error occurred!", "danger");
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
                        showToast("Removed from wishlist.", "success");
                        wishlistBtn.classList.remove('btn-dark');
                        wishlistBtn.classList.add('btn-outline-dark');
                        wishlistBtn.innerHTML = '<i class="bi bi-heart me-2"></i>Add to Wishlist';
                    })
                    .catch(err => {
                        console.error("Error removing from wishlist:", err);
                        showToast("Failed to remove from wishlist!", "danger");
                    });
            }
        });
    }
});