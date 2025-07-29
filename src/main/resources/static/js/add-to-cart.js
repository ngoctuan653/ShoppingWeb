function addToCart(button) {
    const productId = button.getAttribute("data-id");
    const selectedSize = document.getElementById("sizeSelect").value;

    if (!selectedSize) {
        showToast("Please choose a size!", "warning");
        return;
    }

    fetch(`/cart/add/${productId}`, {
        method: "POST",
        headers: {
            "X-Requested-With": "XMLHttpRequest",
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ sizeLabel: selectedSize })
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(msg => {
                    throw new Error(msg);
                });
            }
            return response.text();
        })
        .then(message => {
            showToast(message, "success");
            loadCart();
        })
        .catch(err => {
            showToast(err.message, "danger");
        });
}

document.addEventListener("DOMContentLoaded", function () {
    const pathSegments = window.location.pathname.split("/");
    const productId = pathSegments[pathSegments.length - 1];

    // Nếu URL dạng /products/{id}
    if (!isNaN(productId)) {
        loadSizes(productId);
    }

    function loadSizes(productId) {
        fetch(`/${productId}/sizes`)
            .then(res => {
                const contentType = res.headers.get("content-type");
                if (!res.ok || !contentType || !contentType.includes("application/json")) {
                    throw new Error("Không thể tải size: Phản hồi không hợp lệ hoặc chưa đăng nhập");
                }
                return res.json();
            })
            .then(sizes => {
                const select = document.getElementById("sizeSelect");
                select.innerHTML = `<option value="">-- Choose size --</option>`;

                sizes
                    .filter(size => size.stockQuantity > 0)  // ✨ Lọc chỉ size còn hàng
                    .forEach(size => {
                        const option = document.createElement("option");
                        option.value = size.sizeLabel;
                        option.textContent = size.sizeLabel;
                        select.appendChild(option);
                    });
            })

            .catch(error => {
                console.error("❌ Lỗi khi tải size:", error);
            });
    }

    // Gắn sự kiện click cho tất cả nút Add to Cart
    document.querySelectorAll(".add-to-cart-btn").forEach(button => {
        button.addEventListener("click", () => addToCart(button));
    });
});