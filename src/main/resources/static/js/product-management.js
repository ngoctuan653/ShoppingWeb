// Global variables
let currentEditingProduct = null;
let uploadedImages = [];
let variantCount = 0;

// DOM Elements
const sidebar = document.querySelector('.sidebar');
const mainContent = document.querySelector('.main-content');
const sidebarToggle = document.getElementById('sidebarToggle');
const addProductBtn = document.getElementById('addProductBtn');
const productModal = document.getElementById('productModal');
const modalClose = document.getElementById('modalClose');
const cancelBtn = document.getElementById('cancelBtn');
const productForm = document.getElementById('productForm');
const imageUpload = document.getElementById('imageUpload');
const imageInput = document.getElementById('imageInput');
const imagePreview = document.getElementById('imagePreview');
const addVariantBtn = document.getElementById('addVariantBtn');
const variantsList = document.getElementById('variantsList');
const toast = document.getElementById('toast');

// Sidebar toggle functionality
sidebarToggle.addEventListener('click', () => {
    if (window.innerWidth <= 768) {
        sidebar.classList.toggle('mobile-open');
    } else {
        sidebar.classList.toggle('collapsed');
        mainContent.classList.toggle('expanded');
    }
});

// Modal functionality
function openModal(isEdit = false, productData = null) {
    const modalTitle = document.getElementById('modalTitle');
    const saveBtn = document.getElementById('saveBtn');

    if (isEdit && productData) {
        modalTitle.textContent = 'Update Product';
        saveBtn.textContent = 'Save Product';
        currentEditingProduct = productData;
        populateForm(productData);
    } else {
        modalTitle.textContent = 'Add New Product';
        saveBtn.textContent = 'Save Product';
        currentEditingProduct = null;
        resetForm();
    }

    productModal.classList.add('show');
    document.body.style.overflow = 'hidden';
}

function closeModal() {
    productModal.classList.remove('show');
    document.body.style.overflow = 'auto';
    resetForm();
    uploadedImages = [];
    variantCount = 0;
}

function resetForm() {
    productForm.reset();
    imagePreview.innerHTML = '';
    variantsList.innerHTML = '';
    uploadedImages = [];
}

function populateForm(data) {
    console.log('🔧 Editing product data:', data);

    setTimeout(() => {
        const categorySelect = document.getElementById('productCategory');
        const subCategorySelect = document.getElementById('productSubCategory');
        const brandSelect = document.getElementById('productBrand');

        const categoryIdStr = data.categoryId?.toString() || '';
        const subCategoryIdStr = String(data.subCategoryId || '');
        const brandIdStr = String(data.brandId || '');

        categorySelect.value = categoryIdStr;
        subCategorySelect.value = subCategoryIdStr;
        brandSelect.value = brandIdStr;

        console.log('✅ After delay, selected category:', categorySelect.value);
        console.log('✅ After delay, selected subCategory:', subCategorySelect.value);
        console.log('✅ After delay, selected brand:', brandSelect.value);
    }, 0); // ← delay 1 tick to ensure DOM is fully rendered

    // Other fields are processed normally
    document.getElementById('productName').value = data.productName || '';
    document.getElementById('productDescription').value = data.description || '';
    document.getElementById('productPrice').value = formatCurrencyInput(data.price || 0);
    document.getElementById('productStatus').value = data.status || 'Active';

    // Variants
    variantsList.innerHTML = '';
    variantCount = 0;
    if (Array.isArray(data.sizes)) {
        data.sizes.forEach(size => {
            variantCount++;
            const variantHtml = `
                <div class="variant-item" id="variant-${variantCount}">
                    <button type="button" class="variant-remove" onclick="removeVariant(${variantCount})">✕</button>
                    <div class="form-row">
                        <div class="form-group">
                            <label class="form-label">Size</label>
                            <input type="text" class="form-input" placeholder="Size" name="variant_name_${variantCount}" value="${size.sizeLabel}">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Quantity</label>
                            <input type="number" class="form-input" placeholder="0" min="0" name="variant_stock_${variantCount}" value="${size.stockQuantity}">
                        </div>
                    </div>
                </div>
            `;
            variantsList.insertAdjacentHTML('beforeend', variantHtml);
        });
    }
}

function formatCurrencyInput(value) {
    const number = Number(value.toString().replace(/[^\d]/g, ''));
    if (isNaN(number)) return '';
    return number.toLocaleString('vi-VN');
}

// Event listeners for modal
addProductBtn.addEventListener('click', () => openModal());
modalClose.addEventListener('click', closeModal);
cancelBtn.addEventListener('click', closeModal);

// Close modal when clicking outside
productModal.addEventListener('click', (e) => {
    if (e.target === productModal) {
        closeModal();
    }
});

// Image upload functionality
imageUpload.addEventListener('click', () => {
    imageInput.click();
});

imageUpload.addEventListener('dragover', (e) => {
    e.preventDefault();
    imageUpload.classList.add('dragover');
});

imageUpload.addEventListener('dragleave', () => {
    imageUpload.classList.remove('dragover');
});

imageUpload.addEventListener('drop', (e) => {
    e.preventDefault();
    imageUpload.classList.remove('dragover');
    const files = Array.from(e.dataTransfer.files);
    handleImageFiles(files);
});

imageInput.addEventListener('change', (e) => {
    const files = Array.from(e.target.files);
    handleImageFiles(files);
});

function handleImageFiles(files) {
    files.forEach(file => {
        if (file.type.startsWith('image/') && file.size <= 5 * 1024 * 1024) {
            const reader = new FileReader();
            reader.onload = (e) => {
                const imageData = {
                    file: file,
                    url: e.target.result,
                    id: Date.now() + Math.random()
                };
                uploadedImages.push(imageData);
                addImagePreview(imageData);
            };
            reader.readAsDataURL(file);
        } else {
            showToast('Invalid file. Only images under 5MB are accepted.', 'error');
        }
    });
}

function addImagePreview(imageData) {
    const previewItem = document.createElement('div');
    previewItem.className = 'preview-item';
    previewItem.innerHTML = `
        <img src="${imageData.url}" alt="Preview" class="preview-image">
        <button type="button" class="preview-remove" onclick="removeImage('${imageData.id}')">✕</button>
    `;
    imagePreview.appendChild(previewItem);
}

function removeImage(imageId) {
    uploadedImages = uploadedImages.filter(img => img.id !== imageId);
    renderImagePreviews();
}

function renderImagePreviews() {
    imagePreview.innerHTML = '';
    uploadedImages.forEach(imageData => {
        addImagePreview(imageData);
    });
}

// Variant functionality
addVariantBtn.addEventListener('click', addVariant);

function addVariant() {
    variantCount++;
    const variantHtml = `
        <div class="variant-item" id="variant-${variantCount}">
            <button type="button" class="variant-remove" onclick="removeVariant(${variantCount})">✕</button>
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label">Size</label>
                    <input type="text" class="form-input" placeholder="Size" name="variant_name_${variantCount}">
                </div>
                <div class="form-group">
                    <label class="form-label">Quantity</label>
                    <input type="number" class="form-input" placeholder="0" min="0" name="variant_stock_${variantCount}">
                </div>
            </div>
        </div>
    `;
    variantsList.insertAdjacentHTML('beforeend', variantHtml);
}

function removeVariant(variantId) {
    const variantElement = document.getElementById(`variant-${variantId}`);
    if (variantElement) {
        variantElement.remove();
    }
}

const productPriceInput = document.getElementById('productPrice');

productPriceInput.addEventListener('input', (e) => {
    const raw = e.target.value.replace(/[^\d]/g, ''); // Keep only numbers
    const formatted = Number(raw).toLocaleString('vi-VN'); // Format as VND
    e.target.value = formatted;
});

// Form submission
productForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const product = {
        id: currentEditingProduct?.id || null,
        productName: document.getElementById('productName').value,
        description: document.getElementById('productDescription').value,
        price: parseInt(document.getElementById('productPrice').value.replace(/[^\d]/g, '')),
        categoryId: parseInt(document.getElementById('productCategory').value),
        subCategoryId: parseInt(document.getElementById('productSubCategory').value),
        brandId: parseInt(document.getElementById('productBrand').value),
        status: document.getElementById('productStatus').value,
        sizes: [],
    };

    document.querySelectorAll('.variant-item').forEach(item => {
        const sizeInput = item.querySelector('input[name^="variant_name_"]');
        const quantityInput = item.querySelector('input[name^="variant_stock_"]');
        if (sizeInput && quantityInput) {
            product.sizes.push({
                sizeLabel: sizeInput.value,
                stockQuantity: parseInt(quantityInput.value || '0')
            });
        }
    });

    const formData = new FormData();
    formData.append('product', new Blob([JSON.stringify(product)], {type: 'application/json'}));

    if (uploadedImages.length > 0) {
        formData.append('image', uploadedImages[0].file);
    }

    const isEdit = !!currentEditingProduct;
    const url = isEdit
        ? `/products/update/${currentEditingProduct.id}`
        : `/products/add`;

    try {
        const response = await fetch(url, {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        if (response.ok) {
            showToast(isEdit ? 'Product updated successfully!' : 'New product added successfully!', 'success');
            closeModal();

            // 🔁 Reload the entire page after 1.5s to ensure new data
            setTimeout(() => {
                window.location.reload();
            }, 1500);
        } else {
            showToast(result.message || 'An error occurred!', 'error');
        }
    } catch (err) {
        console.error("[ERROR] Fetch or network error:", err);
        showToast('System error while saving product!', 'error');
    }
});

// Toast notification
function showToast(message, type = 'success') {
    toast.textContent = message;
    toast.className = `toast ${type}`;
    toast.classList.add('show');

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// Table action handlers
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('action-edit')) {
        const productId = e.target.dataset.productId;
        fetch(`/api/products/${productId}`)
            .then(response => {
                if (!response.ok) throw new Error('Unable to load product data');
                return response.json();
            })
            .then(data => {
                openModal(true, data);
            })
            .catch(err => {
                console.error(err);
                showToast('Error loading product for editing!', 'error');
            });

    } else if (e.target.classList.contains('action-delete')) {
        const productId = e.target.closest('.actions').querySelector('[data-product-id]')?.dataset.productId;

        if (productId && confirm('Are you sure you want to delete this product?')) {
            fetch(`/products/delete/${productId}`, {
                method: 'POST'
            })
                .then(response => {
                    if (!response.ok) throw new Error("Deletion failed");
                    return response.text();
                })
                .then(message => {
                    showToast(message, 'success');
                    setTimeout(() => window.location.reload(), 1000);
                })
                .catch(err => {
                    console.error("❌ Error deleting product:", err);
                    showToast("Error deleting product!", 'error');
                });
        }
    } else if (e.target.classList.contains('action-view')) {
        const productId = e.target.dataset.productId;

        if (confirm('Are you sure you want to hide this product?')) {
            fetch(`/products/hide/${productId}`, {
                method: 'POST'
            })
                .then(response => {
                    if (!response.ok) throw new Error("Hiding failed");
                    return response.text();
                })
                .then(message => {
                    showToast(message, 'success');
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                })
                .catch(err => {
                    console.error("❌ Error hiding product:", err);
                    showToast("Error hiding product!", 'error');
                });
        }
    }
});

function handleSearchAndFilter() {
    const keyword = document.getElementById('searchInput')?.value.trim().toLowerCase() || '';
    const brand = document.getElementById('filterBrand')?.value || '';
    const category = document.getElementById('filterCategory')?.value || '';
    const subcategory = document.getElementById('filterSubcategory')?.value || '';
    const status = document.getElementById('filterStatus')?.value || '';
    const stock = document.getElementById('filterStock')?.value || '';

    const params = new URLSearchParams();
    if (keyword) params.append("keyword", keyword);
    if (brand) params.append("brand", brand);
    if (category) params.append("category", category);
    if (subcategory) params.append("subcategory", subcategory);
    if (status) params.append("status", status);
    if (stock) params.append("stockLevel", stock);

    fetch(`/admin/products/search?${params.toString()}`)
        .then(res => res.json())
        .then(products => {
            renderProductTable(products);
        })
        .catch(err => {
            console.error("Error during search:", err);
            showToast("Error during search!", "error");
        });
}

// Attach event listeners
document.querySelector('.search-input').addEventListener('input', debounce(handleSearchAndFilter, 400));
document.querySelectorAll('.filter-select').forEach(select => {
    select.addEventListener('change', handleSearchAndFilter);
});

// Debounce function to reduce rapid calls
function debounce(func, delay) {
    let timer;
    return function (...args) {
        clearTimeout(timer);
        timer = setTimeout(() => func.apply(this, args), delay);
    };
}

function renderProductTable(products) {
    const tbody = document.querySelector('.product-table tbody');
    tbody.innerHTML = '';

    products.forEach(product => {
        const sizes = product.sizes?.map(s => s.sizeLabel).join(', ') || '';
        const row = `
            <tr>
                <td>
                    <div class="product-info">
                        <div class="product-image">
                            <img src="/products/image/${product.id}" 
                                 class="img-thumbnail" 
                                 style="width:70px;height:70px;" 
                                 onerror="this.onerror=null;this.src='/images/default.png';">
                        </div>
                        <div class="product-details">
                            <h4>${product.productName}</h4>
                            <div class="product-sku">Size: ${sizes}</div>
                        </div>
                    </div>
                </td>
                <td>${product.id}</td>
                <td>${product.category?.categoryName || ''}</td>
                <td>${product.subcategory?.subcategoryName || ''}</td>
                <td>${product.brand?.brandName || ''}</td>
                <td class="price">${formatCurrencyInput(product.price)}đ</td>
                <td><span class="stock-level">${product.stockQuantity ?? 0}</span></td>
                <td>
                    <span class="status-badge ${product.status === 'Active' ? 'status-active' : 'status-inactive'}">
                        ${product.status}
                    </span>
                </td>
                <td>${new Date(product.createdAt).toLocaleString('en-US')}</td>
                <td>
                    <div class="actions">
                        <button class="action-btn action-view" data-product-id="${product.id}" title="Hide">👁️</button>
                        <button class="action-btn action-edit" data-product-id="${product.id}" title="Edit">✏️</button>
                        <button class="action-btn action-delete" data-product-id="${product.id}" title="Delete">🗑️</button>
                    </div>
                </td>
            </tr>
        `;
        tbody.insertAdjacentHTML('beforeend', row);
    });
}

// Search functionality
const searchInput = document.querySelector('.search-input');
searchInput.addEventListener('input', (e) => {
    const searchTerm = e.target.value.toLowerCase();
    // Here you would implement search functionality
    console.log('Searching for:', searchTerm);
});

// Filter functionality
const filterSelects = document.querySelectorAll('.filter-select');
filterSelects.forEach(select => {
    select.addEventListener('change', (e) => {
        // Here you would implement filter functionality
        console.log('Filter changed:', e.target.value);
    });
});

// Responsive handling
window.addEventListener('resize', () => {
    if (window.innerWidth > 768) {
        sidebar.classList.remove('mobile-open');
    }
});

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    console.log('Fashion Store Admin Panel initialized');
});