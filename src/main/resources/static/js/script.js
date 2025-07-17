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
    if (cartBtn){
        cartBtn.addEventListener("click", loadCart);
        loadCart(); // Tải giỏ hàng khi trang load
    }else{
        console.log(" cartBtn not found");
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
        .then(res => res.json())
        .then(products => {
            productGrid.innerHTML = "";

            if (products.length === 0) {
                productGrid.innerHTML = `<div class="col-12"><p class="text-muted text-center">Not found Product.</p></div>`;
                return;
            }

            products.forEach(product => {
                const card = `
                <div class="col-md-4 mb-4 fade-in">
                    <div class="card h-100 shadow-sm border-0">
                        <img src="/products/image/${product.id}" class="card-img-top" alt="Product Image" style="object-fit: cover; height: 250px;">
                        <div class="card-body">
                            <h5 class="card-title text-truncate">${product.productName}</h5>
                            <p class="card-text text-muted small">${product.description || ''}</p>
                            <p class="fw-bold text-primary">${formatCurrency(product.price)}</p>
                            <button type="button" class="btn btn-dark btn-sm"
                                    data-id="${product.id}" onclick="addToCart(this)">
                                Add to Cart
                            </button>
                        </div>
                    </div>
                </div>`;
                productGrid.insertAdjacentHTML('beforeend', card);
            });
        })
        .catch(error => {
            productGrid.innerHTML = `<div class="col-12 text-danger text-center">Đã xảy ra lỗi khi tìm kiếm.</div>`;
            console.error("Search error:", error);
        });
};


// Debounce search input
searchInput.addEventListener("input", () => {
    clearTimeout(debounceTimeout);
    debounceTimeout = setTimeout(handleSearch, 300);
});

// Thêm listener khi thay đổi min/max price
minPriceInput.addEventListener("input", () => {
    clearTimeout(debounceTimeout);
    debounceTimeout = setTimeout(handleSearch, 300);
});
maxPriceInput.addEventListener("input", () => {
    clearTimeout(debounceTimeout);
    debounceTimeout = setTimeout(handleSearch, 300);
});

// // Khi kéo slider → cập nhật maxPriceInput và gọi handleSearch
//     priceRangeInput.addEventListener("input", () => {
//         const value = priceRangeInput.value;
//         priceValueSpan.textContent = `$${value}`;
//         maxPriceInput.value = value;
//
//         clearTimeout(debounceTimeout);
//         debounceTimeout = setTimeout(handleSearch, 300);
//     });
//
// // Khi sửa maxPriceInput → cập nhật slider
//     maxPriceInput.addEventListener("input", () => {
//         const value = maxPriceInput.value;
//         if (value) {
//             priceRangeInput.value = value;
//             priceValueSpan.textContent = `$${value}`;
//         }
//
//         clearTimeout(debounceTimeout);
//         debounceTimeout = setTimeout(handleSearch, 300);
//     });

document.addEventListener("DOMContentLoaded", () => {
    // priceValueSpan.textContent = `$${priceRangeInput.value}`;
    // maxPriceInput.value = priceRangeInput.value;

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


