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
    }, 0); // ← delay 1 tick để DOM đảm bảo render xong

    // Các trường còn lại vẫn xử lý bình thường
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
            showToast('Tệp không hợp lệ. Chỉ chấp nhận hình ảnh dưới 5MB.', 'error');
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

// Định dạng khi người dùng nhập giá
productPriceInput.addEventListener('input', (e) => {
    const raw = e.target.value.replace(/[^\d]/g, ''); // Chỉ giữ lại số
    const formatted = Number(raw).toLocaleString('vi-VN'); // Format kiểu VNĐ
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
            showToast(isEdit ? 'Cập nhật sản phẩm thành công!' : 'Thêm sản phẩm mới thành công!', 'success');
            closeModal();

            // 🔁 Reload toàn bộ trang sau 1.5s để đảm bảo dữ liệu mới
            setTimeout(() => {
                window.location.reload();
            }, 1500);
        } else {
            showToast(result.message || 'Đã có lỗi xảy ra!', 'error');
        }
    } catch (err) {
        console.error("[ERROR] Lỗi fetch hoặc mạng:", err);
        showToast('Lỗi hệ thống khi lưu sản phẩm!', 'error');
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
                if (!response.ok) throw new Error('Không thể tải dữ liệu sản phẩm');
                return response.json();
            })
            .then(data => {
                openModal(true, data);
            })
            .catch(err => {
                console.error(err);
                showToast('Lỗi khi tải sản phẩm để chỉnh sửa!', 'error');
            });

    } else if (e.target.classList.contains('action-delete')) {
        const productId = e.target.closest('.actions').querySelector('[data-product-id]')?.dataset.productId;

        if (productId && confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
            fetch(`/products/delete/${productId}`, {
                method: 'POST'
            })
                .then(response => {
                    if (!response.ok) throw new Error("Xóa thất bại");
                    return response.text();
                })
                .then(message => {
                    showToast(message, 'success');
                    setTimeout(() => window.location.reload(), 1000);
                })
                .catch(err => {
                    console.error("❌ Lỗi khi xóa sản phẩm:", err);
                    showToast("Lỗi khi xóa sản phẩm!", 'error');
                });
        }
    } else if (e.target.classList.contains('action-view')) {
        const productId = e.target.dataset.productId;

        if (confirm('Bạn có chắc chắn muốn ẩn sản phẩm này?')) {
            fetch(`/products/hide/${productId}`, {
                method: 'POST'
            })
                .then(response => {
                    if (!response.ok) throw new Error("Ẩn thất bại");
                    return response.text();
                })
                .then(message => {
                    showToast(message, 'success');
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                })
                .catch(err => {
                    console.error("❌ Lỗi khi ẩn:", err);
                    showToast("Lỗi khi ẩn sản phẩm!", 'error');
                });
        }
    }

});


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