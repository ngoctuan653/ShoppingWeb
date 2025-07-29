console.log("✅ script.js loaded");

// hiện thông báo
function showToast(message, type) {
    const toastEl = document.getElementById("cart-toast");
    const toastBody = document.getElementById("toast-message");

    if (!toastEl || !toastBody) {
        console.error("❌ Toast element not found.");
        return;
    }

    toastBody.textContent = message;

    toastEl.classList.remove("bg-success", "bg-danger", "bg-warning");
    toastEl.classList.add("bg-" + type);

    const delayTime = (type === "danger") ? 4000 : 1000;
    const toast = new bootstrap.Toast(toastEl, {
        delay: delayTime
    });
    toast.show();
}

function formatCurrency(amount) {
    return "$" + Number(amount).toFixed(2);
}

function loadCart() {
    const cartContainer = document.querySelector("#cartOffcanvas .offcanvas-body");
    const cartTotalEl = document.querySelector("#cart-total-amount");
    const cartBadge = document.querySelector("button[data-bs-target='#cartOffcanvas'] .badge");

    fetch("/cart/json", {
        method: "GET",
        credentials: "include"
    })
        .then(response => {
            const contentType = response.headers.get("content-type");
            if (!response.ok || !contentType || !contentType.includes("application/json")) {
                throw new Error("Chưa đăng nhập hoặc phản hồi không hợp lệ");
            }
            return response.json();
        })
        .then(data => {
            renderCart(data, cartContainer, cartTotalEl, cartBadge);
        })
        .catch(error => {
            console.error("Lỗi tải giỏ hàng:", error);
            cartContainer.innerHTML = `<p class="text-danger">${error.message}</p>`;
            cartBadge.textContent = "0";
        });
}

// load cart khi mở trang
function renderCart(cartItems, cartContainer, cartTotalEl, cartBadge) {
    cartContainer.innerHTML = "";
    let total = 0;

    cartItems.forEach(item => {
        const itemTotal = Number(item.price) * item.quantity;
        total += itemTotal;

        const cartItem = document.createElement("div");
        cartItem.className = "d-flex justify-content-between align-items-center mb-3 border-bottom pb-2";
        cartItem.innerHTML = `
            <a href="/products/${item.productId}">
                <img src="${item.imageBase64 || '/images/default.png'}" alt="${item.productName}" class="me-2 rounded" style="width: 60px; height: 70px; object-fit: cover; border: 1px solid #dee2e6;">
            </a>
            <div class="flex-grow-1 ms-2">
                <a href="/products/${item.productId}" class="text-decoration-none text-dark">
                    <p class="mb-1 fw-semibold">${item.productName}</p>
                </a>
                <p class="mb-1 text-muted small">Size: ${item.sizeLabel ?? "N/A"}</p>
                <div class="d-flex align-items-center">
                    <button class="btn btn-sm btn-outline-secondary me-1 btn-decrease" 
                            data-id="${item.productId}" 
                            data-size="${item.sizeLabel}">-</button>
                    <span class="px-2">${item.quantity}</span>
                    <button class="btn btn-sm btn-outline-secondary ms-1 btn-increase" 
                            data-id="${item.productId}" 
                            data-size="${item.sizeLabel}">+</button>
                </div>
            </div>
            <div class="text-end">
                <p class="fw-bold mb-1">${formatCurrency(itemTotal)}</p>
                <button class="btn btn-sm btn-outline-danger btn-remove" 
                        data-id="${item.productId}" 
                        data-size="${item.sizeLabel}">x</button>
            </div>
        `;
        cartContainer.appendChild(cartItem);
    });

    cartTotalEl.textContent = formatCurrency(total);
    const totalItems = cartItems.reduce((sum, item) => sum + item.quantity, 0);
    cartBadge.textContent = totalItems;

    // Tăng số lượng
    cartContainer.querySelectorAll(".btn-increase").forEach(btn => {
        btn.addEventListener("click", () => {
            const productId = btn.dataset.id;
            const sizeLabel = btn.dataset.size;

            fetch(`/cart/increase/${productId}`, {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ sizeLabel })
            }).then(() => {
                console.log("Tăng số lượng thành công");
                loadCart();
                loadCheckoutCart();
            });
        });
    });

    // Giảm số lượng
    cartContainer.querySelectorAll(".btn-decrease").forEach(btn => {
        btn.addEventListener("click", () => {
            const productId = btn.dataset.id;
            const sizeLabel = btn.dataset.size;

            fetch(`/cart/decrease/${productId}`, {
                method: "POST",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ sizeLabel })
            }).then(() => {
                console.log("Giảm số lượng thành công");
                loadCart();
                loadCheckoutCart();
            });
        });
    });

    // Xóa sản phẩm
    cartContainer.querySelectorAll(".btn-remove").forEach(btn => {
        btn.addEventListener("click", () => {
            const productId = btn.dataset.id;
            const sizeLabel = btn.dataset.size;

            fetch(`/cart/remove/${productId}`, {
                method: "DELETE",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ sizeLabel })
            }).then(() => {
                console.log("Xóa sản phẩm thành công");
                loadCart();
                loadCheckoutCart();
            });
        });
    });
}


function loadCheckoutCart() {
    const cartContainer = document.querySelector("#checkout-cart-items");
    const cartTotal = document.querySelector("#checkout-cart-total");

    if (!cartContainer || !cartTotal) {
        console.warn("⚠️ DOM phần giỏ hàng checkout chưa sẵn sàng. loadCheckoutCart() dừng.");
        return;
    }
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
            const cartContainer = document.getElementById("checkout-cart-items");
            const totalEl = document.getElementById("checkout-cart-total");

            cartContainer.innerHTML = "";
            let total = 0;

            data.forEach(item => {
                const itemTotal = item.price * item.quantity;
                total += itemTotal;

                const card = document.createElement("div");
                card.className = "col-md-4 mb-4";
                card.innerHTML = `
                    <div class="card">
                        <a href="/products/${item.productId}">
                            <img src="${item.imageBase64 || '/images/default.png'}" class="card-img-top" alt="Product Image">
                        </a>
                        <div class="card-body">
                            <a href="/products/${item.productId}" class="text-decoration-none text-dark">
                                <h5 class="card-title">${item.productName}</h5>
                            </a>
                            <p class="text-muted small">Size: ${item.sizeLabel}</p>
                            <p class="fw-bold">$${item.price.toFixed(2)}</p>
                            <div class="d-flex justify-content-between align-items-center">
                                <button class="btn btn-outline-secondary btn-sm" onclick="changeQuantity(${item.productId}, -1, '${item.sizeLabel}')">-</button>
                                <span class="px-2">${item.quantity}</span>
                                <button class="btn btn-outline-secondary btn-sm" onclick="changeQuantity(${item.productId}, 1, '${item.sizeLabel}')">+</button>
                            </div>
                        </div>
                    </div>
                `;
                cartContainer.appendChild(card);
            });

            totalEl.textContent = `$${total.toFixed(2)}`;
            document.getElementById("cartTotalValue").value = total.toFixed(2); // 👈 dùng trong discount
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
        body: JSON.stringify({ sizeLabel: sizeLabel })
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

document.addEventListener("DOMContentLoaded", function () {
    const cartBtn = document.querySelector("button[data-bs-target='#cartOffcanvas']");
    if (cartBtn) {
        cartBtn.addEventListener("click", loadCart);
        loadCart(); // Tải giỏ hàng khi trang load
    } else {
        console.log("cartBtn not found");
    }
});