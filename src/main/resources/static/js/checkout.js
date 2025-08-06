document.getElementById("checkoutForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const shippingAddress = document.getElementById("shippingAddress").value.trim();
    const phone = document.getElementById("phone").value.trim();
    const discountCode = document.getElementById("discountCode").value.trim();
    const paymentMethod = document.getElementById("paymentMethod").value.trim();
    const selectedMap = JSON.parse(localStorage.getItem("selectedCartItems") || "{}");

    // Fetch cart to get correct quantity
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
        alert("You have not chosen any product.");
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
            window.location.href = data.redirectUrl; // Redirect to payment gateway
        } else if (data.success) {
            localStorage.removeItem("selectedCartItems");
            window.location.href = `/order/success?id=${data.orderId}`;
        } else {
            alert("Error: " + data.message);
        }
    } else {
        const text = await response.text();
        alert("Error: " + text);
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
        console.warn("⚠️ DOM cart checkout not ready. loadCheckoutCart() stop.");
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
                throw new Error("Not logged in or invalid response");
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
                cartContainer.innerHTML = `<p class="text-muted">No products selected.</p>`;
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
            console.error("Cannot load cart:", err);
        });
}

function updateCartItemCount() {
    console.log("🔄 Starting updateCartItemCount...");

    fetch('/cart/json')
        .then(res => {
            const contentType = res.headers.get("content-type");
            console.log("📥 Response received from /cart/json:", res.status, contentType);

            if (!res.ok || !contentType.includes("application/json")) {
                throw new Error("❌ Invalid response from /cart/json");
            }
            return res.json();
        })
        .then(data => {
            console.log("✅ Cart data:", data);

            if (!Array.isArray(data)) {
                throw new Error("❌ The returned data is not a list.");
            }

            let totalQuantity = 0;
            data.forEach(item => {
                console.log(`🛒 ${item.productName} (size: ${item.sizeLabel}) - Quantity: ${item.quantity}`);
                totalQuantity += item.quantity;
            });

            const cartCountEl = document.getElementById("cartItemCount");
            if (cartCountEl) {
                cartCountEl.innerText = totalQuantity;
                console.log("✅ Total quantity displayed in cart", totalQuantity);
                console.log("---------------------------------------------------------------");
            } else {
                console.warn("⚠️ Element not found #cartItemCount");
                console.log("---------------------------------------------------------------");
            }
        })
        .catch(err => {
            console.error("❌ Error updating cart quantity:", err.message);
        });

    loadCart();
}

function changeQuantity(productId, delta, sizeLabel) {
    const url = delta > 0 ? `/cart/increase/${productId}` : `/cart/decrease/${productId}`;
    console.log(`🛠️ Sending request to ${delta > 0 ? "INCREASE" : "DECREASE"} quantity: productId=${productId}, sizeLabel=${sizeLabel}, URL=${url}`);

    fetch(url, {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({sizeLabel: sizeLabel})
    })
        .then(res => {
            console.log("📥 Response from quantity change:", res.status);
            if (!res.ok) {
                return res.text().then(text => {
                    throw new Error(text || "Unknown error");
                });
            }
            return res.text();
        })
        .then(msg => {
            console.log("✅ Quantity changed successfully:", msg);
            loadCheckoutCart();
            updateCartItemCount();
        })
        .catch(err => {
            console.error("❌ Error changing quantity:", err.message);
        });
}

document.addEventListener("DOMContentLoaded", loadCheckoutCart);