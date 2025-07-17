// Thêm sản phẩm vào giỏ hàng
function addToCart(button) {
    const productId = button.getAttribute("data-id");
    fetch(`/cart/add/${productId}`, {
        method: "POST",
        headers: {
            "X-Requested-With": "XMLHttpRequest"
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(msg => {
                    throw new Error(msg); // để xử lý dưới catch
                });
            }
            return response.text();
        })
        .then(message => {
            showToast(message, 'success'); // hiện màu xanh nếu thành công
            loadCart();
        })
        .catch(err => {
            showToast(err.message, 'danger'); // hiện màu đỏ nếu lỗi (vd: chưa login)
        });
}

// hiện thông báo
function showToast(message, type) {
    const toastEl = document.getElementById("cart-toast");
    const toastBody = document.getElementById("toast-message");
    toastBody.textContent = message;
    toastEl.classList.remove("bg-success", "bg-danger");
    toastEl.classList.add("bg-" + type);

    const delayTime = (type === "danger") ? 4000 : 400;
    const toast = new bootstrap.Toast(toastEl, {
        delay: delayTime,
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
            <img src="${item.imageBase64}" alt="${item.productName}" class="me-2 rounded" style="width: 60px; height: 70px; object-fit: cover;">
            <div class="flex-grow-1 ms-2">
                <p class="mb-1 fw-semibold">${item.productName}</p>
                <div class="d-flex align-items-center">
                    <button class="btn btn-sm btn-outline-secondary me-1">-</button>
                    <span class="px-2">${item.quantity}</span>
                    <button class="btn btn-sm btn-outline-secondary ms-1">+</button>
                </div>
            </div>
            <div class="text-end">
                <p class="fw-bold mb-1">${formatCurrency(itemTotal)}</p>
                <button class="btn btn-sm btn-outline-danger">x</button>
            </div>
        `;
        cartContainer.appendChild(cartItem);
    });

    cartTotalEl.textContent = formatCurrency(total);
    const totalItems = cartItems.reduce((sum, item) => sum + item.quantity, 0);
    cartBadge.textContent = totalItems;
}

document.addEventListener("DOMContentLoaded", function () {
    const cartBtn = document.querySelector("button[data-bs-target='#cartOffcanvas']");
    cartBtn.addEventListener("click", loadCart);
    loadCart(); // Tải giỏ hàng khi trang load
});
