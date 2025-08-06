console.log("✅ script.js loaded");

// Show toast notification
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
    const number = Number(amount);
    if (isNaN(number)) return "0 VNĐ";
    return number.toLocaleString('vi-VN') + " VNĐ";
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
                throw new Error("Not logged in or invalid response");
            }
            return response.json();
        })
        .then(data => {
            renderCart(data, cartContainer, cartTotalEl, cartBadge);
        })
        .catch(error => {
            console.error("Error loading cart:", error);
            cartContainer.innerHTML = `<p class="text-danger">${error.message}</p>`;
            cartBadge.textContent = "0";
        });
}

// Load cart when opening the page
function renderCart(cartItems, cartContainer, cartTotalEl, cartBadge) {
    cartContainer.innerHTML = "";
    let total = 0;

    cartItems.forEach(item => {
        const itemTotal = Number(item.price) * item.quantity;
        const isOutOfStock = item.outOfStock;
        if (!isOutOfStock) total += itemTotal;

        const cartItem = document.createElement("div");
        cartItem.className = "d-flex justify-content-between align-items-center mb-3 border-bottom pb-2";
        if (isOutOfStock) {
            cartItem.classList.add("opacity-50");
        }

        cartItem.innerHTML = `
            <div class="form-check me-2">
                <input class="form-check-input cart-item-checkbox" type="checkbox"
                    data-id="${item.productId}"
                    data-size="${item.sizeLabel ?? ''}"
                    ${isOutOfStock ? 'disabled' : ''}>
            </div>
            <a href="/products/${item.productId}">
                <img src="${item.imageBase64 || '/images/default.png'}" alt="${item.productName}" class="me-2 rounded"
                    style="width: 60px; height: 70px; object-fit: cover; border: 1px solid #dee2e6;">
            </a>
            <div class="flex-grow-1 ms-2">
                <a href="/products/${item.productId}" class="text-decoration-none text-dark">
                    <p class="mb-1 fw-semibold">${item.productName}</p>
                </a>
                <p class="mb-1 text-muted small">Size: ${item.sizeLabel ?? "N/A"}</p>
                ${isOutOfStock
            ? '<span class="badge bg-danger">Out of stock</span>'
            : `
                        <div class="d-flex align-items-center">
                            <button class="btn btn-sm btn-outline-secondary me-1 btn-decrease"
                                    data-id="${item.productId}" data-size="${item.sizeLabel}">-</button>
                            <span class="px-2">${item.quantity}</span>
                            <button class="btn btn-sm btn-outline-secondary ms-1 btn-increase"
                                    data-id="${item.productId}" data-size="${item.sizeLabel}">+</button>
                        </div>
                    `}
            </div>
            <div class="text-end">
                <p class="fw-bold mb-1">${formatCurrency(itemTotal)}</p>
                ${isOutOfStock ? '' : `<button class="btn btn-sm btn-outline-danger btn-remove" 
                    data-id="${item.productId}" data-size="${item.sizeLabel}">x</button>`}
            </div>
        `;

        cartContainer.appendChild(cartItem);
    });

    cartTotalEl.textContent = formatCurrency(total);
    const totalItems = cartItems.reduce((sum, item) => sum + item.quantity, 0);
    cartBadge.textContent = totalItems;

    // Attach events for in-stock products
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
                console.log("Increased quantity successfully");
                loadCart();
                loadCheckoutCart();
            });
        });
    });

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
                console.log("Decreased quantity successfully");
                loadCart();
                loadCheckoutCart();
            });
        });
    });

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
                console.log("Removed product successfully");
                loadCart();
                loadCheckoutCart();
            });
        });
    });

    // Save selected products to localStorage
    cartContainer.querySelectorAll(".cart-item-checkbox").forEach(checkbox => {
        checkbox.addEventListener("change", () => {
            const productId = checkbox.dataset.id;
            const sizeLabel = checkbox.dataset.size;
            const key = `${productId}_${sizeLabel}`;

            let selected = JSON.parse(localStorage.getItem("selectedCartItems") || "{}");

            if (checkbox.checked) {
                selected[key] = { productId, sizeLabel };
            } else {
                delete selected[key];
            }

            localStorage.setItem("selectedCartItems", JSON.stringify(selected));
        });
    });

    const selected = JSON.parse(localStorage.getItem("selectedCartItems") || "{}");
    cartContainer.querySelectorAll(".cart-item-checkbox").forEach(cb => {
        const key = `${cb.dataset.id}_${cb.dataset.size}`;
        if (selected[key]) {
            cb.checked = true;
        }
    });
}

document.addEventListener("DOMContentLoaded", function () {
    const cartBtn = document.querySelector("button[data-bs-target='#cartOffcanvas']");
    if (cartBtn) {
        cartBtn.addEventListener("click", loadCart);
        loadCart(); // Load cart when page loads
    } else {
        console.log("cartBtn not found");
    }
});

document.addEventListener("DOMContentLoaded", () => {
    const checkoutBtn = document.getElementById("checkoutBtn");
    if (!checkoutBtn) return;

    checkoutBtn.addEventListener("click", () => {
        const selectedMap = {};
        document.querySelectorAll(".cart-item-checkbox:checked").forEach(cb => {
            const productId = cb.dataset.id;
            const sizeLabel = cb.dataset.size;
            const key = `${productId}_${sizeLabel}`;
            selectedMap[key] = { productId, sizeLabel };
        });

        if (Object.keys(selectedMap).length === 0) {
            alert("You haven't selected any products!");
            return;
        }

        localStorage.setItem("selectedCartItems", JSON.stringify(selectedMap));
        window.location.href = "/checkout";
    });
});