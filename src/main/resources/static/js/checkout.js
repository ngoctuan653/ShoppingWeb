document.getElementById("checkoutForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const shippingAddress = document.getElementById("shippingAddress").value.trim();
    const phone = document.getElementById("phone").value.trim();
    const discountCode = document.getElementById("discountCode").value.trim();
    const paymentMethod = document.getElementById("paymentMethod").value.trim();
    const selectedMap = JSON.parse(localStorage.getItem("selectedCartItems") || "{}");

    // Gọi lại giỏ hàng để lấy đúng quantity
    const res = await fetch("/cart/json", {credentials: "include"});
    const cartData = await res.json();

    const items = cartData
        .filter(item => selectedMap[`${item.productId}_${item.sizeLabel}`])
        .map(item => ({
            productId: item.productId,
            sizeLabel: item.sizeLabel,
            quantity: item.quantity
        }));

    if (items.length === 0) {
        alert("Bạn chưa chọn sản phẩm nào.");
        return;
    }

    const requestBody = {
        shippingAddress,
        phone,
        discountCode,
        paymentMethod,
        items
    };

    const response = await fetch("/cart/checkout-ajax", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        credentials: "include",
        body: JSON.stringify(requestBody)
    });

    if (response.ok) {
        const data = await response.json();
        if (data.redirectUrl) {
            window.location.href = data.redirectUrl; // Redirect đến cổng thanh toán
        } else if (data.success) {
            localStorage.removeItem("selectedCartItems");
            window.location.href = `/order/success?id=${data.orderId}`;
        } else {
            alert("Lỗi: " + data.message);
        }
    } else {
        const text = await response.text();
        alert("Lỗi: " + text);
    }
});

window.addEventListener('DOMContentLoaded', function () {
    const toastEl = document.getElementById('contactToast');
    if (toastEl && toastEl.querySelector('.toast-body').textContent.trim() !== '') {
        const toast = new bootstrap.Toast(toastEl, {delay: 4000});
        toast.show();
    }
});

function loadCheckoutCart() {
    const cartContainer = document.querySelector("#checkout-cart-items");
    const cartTotal = document.querySelector("#checkout-cart-total");

    if (!cartContainer || !cartTotal) {
        console.warn("⚠️ DOM phần giỏ hàng checkout chưa sẵn sàng. loadCheckoutCart() dừng.");
        return;
    }

    const selectedMap = JSON.parse(localStorage.getItem("selectedCartItems") || "{}");

    fetch("/cart/json", {
        method: "GET",
        credentials: "include"
    })
        .then(res => {
            const contentType = res.headers.get("content-type");
            if (!res.ok || !contentType || !contentType.includes("application/json")) {
                throw new Error("Chưa đăng nhập hoặc phản hồi không hợp lệ");
            }
            return res.json();
        })
        .then(data => {
            cartContainer.innerHTML = "";
            let total = 0;

            const selectedItems = data.filter(item => {
                const key = `${item.productId}_${item.sizeLabel}`;
                return selectedMap[key];
            });

            if (selectedItems.length === 0) {
                cartContainer.innerHTML = `<p class="text-muted">Không có sản phẩm nào được chọn.</p>`;
                cartTotal.textContent = "$0.00";
                document.getElementById("cartTotalValue").value = 0;
                return;
            }

            selectedItems.forEach(item => {
                const itemTotal = item.price * item.quantity;
                total += itemTotal;

                const card = document.createElement("div");
                card.className = "col-md-4 mb-4";
                card.innerHTML = `
                <div class="card h-100">
                    <a href="/products/${item.productId}">
                        <img src="${item.imageBase64 || '/images/default.png'}" class="card-img-top" alt="Product Image" style="height: 200px; object-fit: cover;">
                    </a>
                    <div class="card-body d-flex flex-column">
                        <a href="/products/${item.productId}" class="text-decoration-none text-dark mb-2">
                            <h5 class="card-title">${item.productName}</h5>
                        </a>
                        <p class="text-muted small mb-1">Size: ${item.sizeLabel}</p>
                        <p class="fw-bold mb-2">${formatCurrency(item.price)}</p>
                        <div class="d-flex justify-content-between align-items-center mt-auto">
                            <button class="btn btn-outline-secondary btn-sm" onclick="changeQuantity(${item.productId}, -1, '${item.sizeLabel}')">-</button>
                            <span class="px-2">${item.quantity}</span>
                            <button class="btn btn-outline-secondary btn-sm" onclick="changeQuantity(${item.productId}, 1, '${item.sizeLabel}')">+</button>
                        </div>
                    </div>
                </div>
            `;
                cartContainer.appendChild(card);
            });

            cartTotal.textContent = formatCurrency(total);
            document.getElementById("cartTotalValue").value = total.toFixed(2);
            cartContainer.classList.remove("d-none");
        })
        .catch(err => {
            console.error("Không thể tải giỏ hàng:", err);
        });
}

function updateCartItemCount() {
    console.log("🔄 Bắt đầu updateCartItemCount...");

    fetch('/cart/json')
        .then(res => {
            const contentType = res.headers.get("content-type");
            console.log("📥 Phản hồi nhận được từ /cart/json:", res.status, contentType);

            if (!res.ok || !contentType.includes("application/json")) {
                throw new Error("❌ Phản hồi không hợp lệ từ /cart/json");
            }
            return res.json();
        })
        .then(data => {
            console.log("✅ Dữ liệu giỏ hàng:", data);

            if (!Array.isArray(data)) {
                throw new Error("❌ Dữ liệu trả về không phải là danh sách");
            }

            let totalQuantity = 0;
            data.forEach(item => {
                console.log(`🛒 ${item.productName} (size: ${item.sizeLabel}) - SL: ${item.quantity}`);
                totalQuantity += item.quantity;
            });

            const cartCountEl = document.getElementById("cartItemCount");
            if (cartCountEl) {
                cartCountEl.innerText = totalQuantity;
                console.log("✅ Tổng số lượng hiển thị trong giỏ:", totalQuantity);
                console.log("---------------------------------------------------------------");
            } else {
                console.warn("⚠️ Không tìm thấy phần tử #cartItemCount");
                console.log("---------------------------------------------------------------");
            }
        })
        .catch(err => {
            console.error("❌ Lỗi khi cập nhật số lượng giỏ hàng:", err.message);
        });

    loadCart();
}

function changeQuantity(productId, delta, sizeLabel) {
    const url = delta > 0 ? `/cart/increase/${productId}` : `/cart/decrease/${productId}`;
    console.log(`🛠️ Gửi yêu cầu ${delta > 0 ? "TĂNG" : "GIẢM"} số lượng: productId=${productId}, sizeLabel=${sizeLabel}, URL=${url}`);

    fetch(url, {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({sizeLabel: sizeLabel})
    })
        .then(res => {
            console.log("📥 Phản hồi từ thay đổi số lượng:", res.status);
            if (!res.ok) {
                return res.text().then(text => {
                    throw new Error(text || "Lỗi không xác định");
                });
            }
            return res.text();
        })
        .then(msg => {
            console.log("✅ Thay đổi số lượng thành công:", msg);
            loadCheckoutCart();
            updateCartItemCount();
        })
        .catch(err => {
            console.error("❌ Lỗi thay đổi số lượng:", err.message);
        });
}

document.addEventListener("DOMContentLoaded", loadCheckoutCart);