document.addEventListener('DOMContentLoaded', function () {
    const wishlistBtn = document.querySelector('.add-to-wishlist-btn');

    if (wishlistBtn) {
        wishlistBtn.addEventListener('click', function () {
            const productId = this.getAttribute('data-id');

            fetch(`/wishlist/add/${productId}`, {
                method: 'POST',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
            .then(response => {
                if (!response.ok) {
                    if (response.status === 401) {
                        alert("Vui lòng đăng nhập để thêm vào wishlist.");
                        window.location.href = "/login";
                    } else {
                        alert("Thêm vào wishlist thất bại!");
                    }
                    throw new Error("Request failed with status " + response.status);
                }
                return response.text();
            })
            .then(message => {
                if (message === "unauthorized") {
                    alert("Vui lòng đăng nhập để thêm vào wishlist.");
                    window.location.href = "/login";
                } else {
                    alert(message); // "Added to wishlist"
                }
            })
            .catch(err => {
                console.error("Lỗi khi thêm vào wishlist:", err);
            });
        });
    }
});
