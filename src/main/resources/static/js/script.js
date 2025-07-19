console.log("✅ script.js loaded");

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
            <a href="/products/${item.productId}">
                <img src="${item.imageBase64}" alt="${item.productName}" class="me-2 rounded" style="width: 60px; height: 70px; object-fit: cover;">
            </a>
            <div class="flex-grow-1 ms-2">
                <a href="/products/${item.productId}" class="text-decoration-none text-dark">
                    <p class="mb-1 fw-semibold">${item.productName}</p>
                </a>
                <div class="d-flex align-items-center">
                    <button class="btn btn-sm btn-outline-secondary me-1 btn-decrease" data-id="${item.productId}">-</button>
                    <span class="px-2">${item.quantity}</span>
                    <button class="btn btn-sm btn-outline-secondary ms-1 btn-increase" data-id="${item.productId}">+</button>
                </div>
            </div>
            <div class="text-end">
                <p class="fw-bold mb-1">${formatCurrency(itemTotal)}</p>
                <button class="btn btn-sm btn-outline-danger btn-remove" data-id="${item.productId}">x</button>
            </div>
        `;
        cartContainer.appendChild(cartItem);
    });

    cartTotalEl.textContent = formatCurrency(total);
    const totalItems = cartItems.reduce((sum, item) => sum + item.quantity, 0);
    cartBadge.textContent = totalItems;

    cartContainer.querySelectorAll(".btn-increase").forEach(btn => {
        btn.addEventListener("click", () => {
            const productId = btn.dataset.id;
            console.log(`Tăng sản phẩm: ${productId}`);
            fetch(`/cart/increase/${productId}`, {
                method: "POST",
                credentials: "include"
            }).then(() => {
                console.log("Tăng xong, load lại cart");
                loadCart();
                loadCheckoutCart();
            });
        });
    });

    cartContainer.querySelectorAll(".btn-decrease").forEach(btn => {
        btn.addEventListener("click", () => {
            const productId = btn.dataset.id;
            console.log(`Giảm sản phẩm: ${productId}`);
            fetch(`/cart/decrease/${productId}`, {
                method: "POST",
                credentials: "include"
            }).then(() => {
                console.log("Giảm xong, load lại cart");
                loadCart();
                loadCheckoutCart();
            });
        });
    });

    cartContainer.querySelectorAll(".btn-remove").forEach(btn => {
        btn.addEventListener("click", () => {
            const productId = btn.dataset.id;
            console.log(`Xóa sản phẩm: ${productId}`);
            fetch(`/cart/remove/${productId}`, {
                method: "DELETE",
                credentials: "include"
            }).then(() => {
                console.log("Xóa xong, load lại cart");
                loadCart();
                loadCheckoutCart();
            });
        });
    });

}

function loadCheckoutCart() {
    fetch("/cart/json", {
        method: "GET",
        credentials: "include"
    })
        .then(res => res.json())
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
                        <img src="${item.imageBase64  || '/images/default.png'}" class="card-img-top" alt="Product Image">
                        <div class="card-body">
                            <h5 class="card-title">${item.productName}</h5>
                            <p class="fw-bold">$${item.price.toFixed(2)}</p>
                            <div class="d-flex justify-content-between align-items-center">
                                <button class="btn btn-outline-secondary btn-sm" onclick="changeQuantity(${item.productId}, -1)">-</button>
                                <span class="px-2">${item.quantity}</span>
                                <button class="btn btn-outline-secondary btn-sm" onclick="changeQuantity(${item.productId}, 1)">+</button>
                            </div>
                        </div>
                    </div>
                `;
                cartContainer.appendChild(card);
            });

            totalEl.textContent = `$${total.toFixed(2)}`;
        })
        .catch(err => {
            console.error("Không thể tải giỏ hàng:", err);
        });
}

function updateCartItemCount() {
    fetch('/api/cart')
        .then(res => res.json())
        .then(data => {
            let totalQuantity = 0;
            data.forEach(item => totalQuantity += item.quantity);
            document.getElementById("cartItemCount").innerText = totalQuantity;
        });
    loadCart();
}

function changeQuantity(productId, delta) {
    const url = delta > 0 ? `/cart/increase/${productId}` : `/cart/decrease/${productId}`;
    fetch(url, { method: "POST", credentials: "include" })
        .then(() => {
            loadCheckoutCart();       // cập nhật lại giao diện checkout
            updateCartItemCount();    // cập nhật số lượng ở biểu tượng cart
        })
        .catch(err => console.error("Lỗi thay đổi số lượng:", err));
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

//search Product by AJAX
const searchInput = document.getElementById("searchInput");
const productGrid = document.getElementById("productGrid");
const minPriceInput = document.getElementById("minPrice");
const maxPriceInput = document.getElementById("maxPrice");
const priceRangeInput = document.getElementById("priceRange");
const priceValueSpan = document.getElementById("priceValue");
let debounceTimeout = null;

const showLoading = () => {
    productGrid.innerHTML = `
        <div class="col-12 text-center py-4">
            <div class="spinner-border text-dark" role="status"></div>
        </div>`;
};

// Helper: Lấy danh sách ID từ checkbox
function getCheckedValues(selector) {
    return Array.from(document.querySelectorAll(selector + ":checked")).map(cb => cb.value);
}

const handleSearch = () => {
    const keyword = searchInput.value.trim();
    const minPrice = minPriceInput.value;
    const maxPrice = maxPriceInput.value;
    const selectedCategories = getCheckedValues(".category-filter");
    const selectedBrands = getCheckedValues(".brand-filter");


    // ✅ In ra console để debug
    console.log("🔍 Keyword:", keyword);
    console.log("💲 Min Price:", minPrice);
    console.log("💲 Max Price:", maxPrice);
    console.log("📂 Selected Categories:", selectedCategories);
    console.log("🏷️ Selected Brands:", selectedBrands);

    showLoading();

    const params = new URLSearchParams();
    if (keyword) params.append("keyword", keyword);
    if (minPrice) params.append("minPrice", minPrice);
    if (maxPrice) params.append("maxPrice", maxPrice);

    // ✅ Dùng vòng lặp để append từng cái
    selectedCategories.forEach(id => params.append("categories", id));
    selectedBrands.forEach(id => params.append("brands", id));

    /// ✅ In ra URL
    console.log("🌐 Final Search URL:", `/search?${params.toString()}`);

    fetch(`/search?${params.toString()}`)
        .then(res => {
            console.log("🔄 Đang gửi request tới:", `/search?${params.toString()}`);
            return res.json();
        })
        .then(products => {
            console.log("Dữ liệu trả về từ server:", products);

            productGrid.innerHTML = "";

            if (!Array.isArray(products) || products.length === 0) {
                console.warn("Không có sản phẩm nào thỏa mãn điều kiện lọc.");
                productGrid.innerHTML = `<div class="col-12"><p class="text-muted text-center">Not found Product.</p></div>`;
                return;
            }
            products.forEach(product => {
                const productCard = `
                <div class="col-md-4 mb-4 fade-in">
                  <div class="card h-100 shadow-sm">
                    <a href="/products/${product.id}">
                    <img  src="data:image/jpeg;base64,${product.image}" class="card-img-top" alt="${product.productName}" />
                    </a>
                    <div class="card-body d-flex flex-column">
                    <a href="/products/${product.id}" class="text-decoration-none text-dark">
                      <h5 class="card-title">${product.productName}</h5>
                    </a>
                      <p class="card-text text-truncate">${product.description}</p>
                      <div class="mt-auto">
                        <p class="fw-bold">$${product.price}</p>
                        <button class="btn btn-dark w-100" onclick="addToCartById(${product.id})">Add to Cart</button>
                      </div>
                    </div>
                  </div>
                </div>
            `;
                productGrid.insertAdjacentHTML("beforeend", productCard);
            });
        })
        .catch(error => {
            console.error("❌ Lỗi khi tìm kiếm sản phẩm:", error);
        });
};

// Debounce search input
if (searchInput) {
    let debounceTimeout;
    searchInput.addEventListener("input", () => {
        clearTimeout(debounceTimeout);
        debounceTimeout = setTimeout(handleSearch, 300);
    });
} else {
    console.log("searchInput not found");
}

if (minPriceInput) {
    minPriceInput.addEventListener("input", () => {
        clearTimeout(debounceTimeout);
        debounceTimeout = setTimeout(handleSearch, 300);
    });
} else {
    console.log("minPriceInput not found");
}

if (maxPriceInput) {
    maxPriceInput.addEventListener("input", () => {
        clearTimeout(debounceTimeout);
        debounceTimeout = setTimeout(handleSearch, 300);
    });
} else {
    console.log("maxPriceInput not found");
}

document.addEventListener("DOMContentLoaded", () => {
    // Gắn sự kiện change cho các checkbox category
    document.querySelectorAll(".category-filter").forEach(cb => {
        cb.addEventListener("change", () => {
            clearTimeout(debounceTimeout);
            debounceTimeout = setTimeout(handleSearch, 100);
        });
    });

    // Gắn sự kiện change cho các checkbox brand
    document.querySelectorAll(".brand-filter").forEach(cb => {
        cb.addEventListener("change", () => {
            clearTimeout(debounceTimeout);
            debounceTimeout = setTimeout(handleSearch, 100);
        });
    });

    // Nếu bạn có nút tìm kiếm thì cũng gắn ở đây
    document.getElementById("searchBtn")?.addEventListener("click", handleSearch);
});


