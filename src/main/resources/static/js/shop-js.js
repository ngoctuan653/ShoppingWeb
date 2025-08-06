let currentPage = 1;
const pageSize = 6;
const loadMoreBtn = document.getElementById("loadMoreBtn");

// 🔧 Helper
function getCheckedValues(selector) {
    return Array.from(document.querySelectorAll(selector + ":checked")).map(cb => cb.value);
}

const loadMoreProducts = () => {
    loadMoreBtn.disabled = true;
    loadMoreBtn.textContent = "Loading...";

    const keyword = searchInput?.value.trim();
    const minPrice = minPriceInput?.value;
    const maxPrice = maxPriceInput?.value;
    const selectedCategories = getCheckedValues(".category-filter");
    const selectedSubcategories = getCheckedValues(".subcategory-filter");
    const selectedBrands = getCheckedValues(".brand-filter");

    const params = new URLSearchParams();
    params.append("page", currentPage); // use the current currentPage
    params.append("size", pageSize);
    if (keyword) params.append("keyword", keyword);
    if (minPrice) params.append("minPrice", minPrice);
    if (maxPrice) params.append("maxPrice", maxPrice);
    selectedCategories.forEach(id => params.append("categories", id));
    selectedSubcategories.forEach(id => params.append("subcategories", id));
    selectedBrands.forEach(id => params.append("brands", id));

    fetch(`/search?${params.toString()}`)
        .then(res => res.json())
        .then(pageData => {
            const products = pageData.content; // content is the list of products
            const isLast = pageData.last;      // check if it's the last page

            if (!Array.isArray(products) || products.length === 0) {
                loadMoreBtn.style.display = "none";
                return;
            }

            products.forEach(product => {
                const productImage = product.image ? `data:image/jpeg;base64,${product.image}` : '/images/default.png';
                const productCard = `
                <div class="col-md-4 mb-4 fade-in">
                    <div class="card h-100 shadow-sm">
                        <a href="/products/${product.id}">
                            <img src="${productImage}" class="card-img-top" alt="${product.productName}" style="height: 350px; object-fit: cover;" />
                        </a>
                        <div class="card-body d-flex flex-column">
                            <a href="/products/${product.id}" class="text-decoration-none text-dark">
                                <h5 class="card-title">${product.productName}</h5>
                            </a>
                            <p class="card-text text-muted">${product.subcategory?.subcategoryName || ''}</p>
                            <p class="card-text text-muted">${product.category?.categoryName || ''}</p>
                            <div class="mt-auto">
                                <p class="fw-bold">${formatVND(product.price)}</p>
                            </div>
                        </div>
                    </div>
                </div>
            `;
                productGrid.insertAdjacentHTML("beforeend", productCard);
            });

            currentPage++;
            if (isLast) {
                loadMoreBtn.style.display = "none";
            } else {
                loadMoreBtn.disabled = false;
                loadMoreBtn.textContent = "Load More Products";
            }
        });
};

loadMoreBtn?.addEventListener("click", loadMoreProducts);

document.addEventListener("DOMContentLoaded", () => {
    currentPage = 1;
});

// Search Product by AJAX
const searchInput = document.getElementById("searchInput");
const productGrid = document.getElementById("productGrid");
const minPriceInput = document.getElementById("minPrice");
const maxPriceInput = document.getElementById("maxPrice");
let debounceTimeout = null;

function formatNumberInput(value) {
    const number = value.replace(/\D/g, ""); // keep only numbers
    return Number(number || 0).toLocaleString("vi-VN"); // add thousand separators
}

function unformatNumber(value) {
    return value.replace(/\./g, ""); // remove thousand separators
}

[minPriceInput, maxPriceInput].forEach(input => {
    input.addEventListener("input", () => {
        const raw = unformatNumber(input.value);
        input.value = formatNumberInput(raw);
        input.setSelectionRange(input.value.length, input.value.length);

        clearTimeout(debounceTimeout);
        debounceTimeout = setTimeout(handleSearch, 300);
    });
});

const showLoading = () => {
    productGrid.innerHTML = `
        <div class="col-12 text-center py-4">
            <div class="spinner-border text-dark" role="status"></div>
        </div>`;
};

// Helper: Get list of IDs from checkboxes
function getCheckedValues(selector) {
    return Array.from(document.querySelectorAll(selector + ":checked")).map(cb => cb.value);
}

function formatVND(amount) {
    if (isNaN(amount)) return "";
    return parseInt(amount).toLocaleString("vi-VN") + " VNĐ";
}

const handleSearch = () => {
    const keyword = searchInput.value.trim();
    const minPrice = unformatNumber(minPriceInput.value);
    const maxPrice = unformatNumber(maxPriceInput.value);
    const selectedCategories = getCheckedValues(".category-filter");
    const selectedSubcategories = getCheckedValues(".subcategory-filter");
    const selectedBrands = getCheckedValues(".brand-filter");

    // 👇 Reset pagination state
    currentPage = 1;
    loadMoreBtn.style.display = "block";
    loadMoreBtn.disabled = false;
    loadMoreBtn.textContent = "Load More Products";

    showLoading();

    const params = new URLSearchParams();
    params.append("page", 0); // First page
    params.append("size", pageSize);
    if (keyword) params.append("keyword", keyword);
    if (minPrice) params.append("minPrice", minPrice);
    if (maxPrice) params.append("maxPrice", maxPrice);
    selectedCategories.forEach(id => params.append("categories", id));
    selectedSubcategories.forEach(id => params.append("subcategories", id));
    selectedBrands.forEach(id => params.append("brands", id));

    const url = `/search?${params.toString()}`;

    fetch(url)
        .then(res => {
            if (!res.ok) {
                return res.text().then(text => {
                    throw new Error(text || `HTTP error ${res.status}`);
                });
            }
            return res.json();
        })
        .then(pageData => {
            const products = pageData.content;
            const isLast = pageData.last;

            productGrid.innerHTML = "";

            if (!Array.isArray(products) || products.length === 0) {
                productGrid.innerHTML = `<div class="col-12"><p class="text-muted text-center">Not found Product.</p></div>`;
                loadMoreBtn.style.display = "none";
                return;
            }

            products.forEach(product => {
                const productImage = product.image ? `data:image/jpeg;base64,${product.image}` : '/images/default.png';
                const productCard = `
                    <div class="col-md-4 mb-4 fade-in">
                        <div class="card h-100 shadow-sm">
                            <a href="/products/${product.id}">
                                <img src="${productImage}" class="card-img-top" alt="${product.productName}" style="height: 350px; object-fit: cover;" />
                            </a>
                            <div class="card-body d-flex flex-column">
                                <a href="/products/${product.id}" class="text-decoration-none text-dark">
                                    <h5 class="card-title">${product.productName}</h5>
                                </a>
                                <p class="card-text text-muted">${product.subcategory?.subcategoryName || ''}</p>
                                <p class="card-text text-muted">${product.category?.categoryName || ''}</p>
                                <div class="mt-auto">
                                    <p class="fw-bold">${formatVND(product.price)}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
                productGrid.insertAdjacentHTML("beforeend", productCard);
            });

            // ✅ Hide button if it's the last page
            if (isLast || products.length < pageSize) {
                loadMoreBtn.style.display = "none";
            } else {
                loadMoreBtn.style.display = "block";
            }
        })
        .catch(error => {
            console.error("❌ Error searching products:", error.message || error);
            productGrid.innerHTML = `<div class="col-12 text-danger text-center">Error loading product data.</div>`;
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
    // Attach change event to category checkboxes
    document.querySelectorAll(".category-filter").forEach(cb => {
        cb.addEventListener("change", () => {
            clearTimeout(debounceTimeout);
            debounceTimeout = setTimeout(handleSearch, 100);
        });
    });

    document.querySelectorAll(".subcategory-filter").forEach(cb => {
        cb.addEventListener("change", () => {
            clearTimeout(debounceTimeout);
            debounceTimeout = setTimeout(handleSearch, 100);
        });
    });

    // Attach change event to brand checkboxes
    document.querySelectorAll(".brand-filter").forEach(cb => {
        cb.addEventListener("change", () => {
            clearTimeout(debounceTimeout);
            debounceTimeout = setTimeout(handleSearch, 100);
        });
    });

    // If you have a search button, attach it here
    document.getElementById("searchBtn")?.addEventListener("click", handleSearch);
});