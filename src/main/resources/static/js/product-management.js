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
    // This would populate form fields with existing product data
    document.getElementById('productName').value = data.productName || '';
    document.getElementById('productDescription').value = data.description || '';
    document.getElementById('productPrice').value = data.price || '';
    document.getElementById('productCategory').value = data.categoryId || '';
    document.getElementById('productSubCategory').value = data.subcategoryId || '';
    document.getElementById('productBrand').value = data.brandId || '';
    document.getElementById('productStatus').value = data.status || 'Active';
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

// Form submission
productForm.addEventListener('submit', (e) => {
    e.preventDefault();

    // Collect form data
    const formData = new FormData(productForm);

    // Add images
    uploadedImages.forEach((img, index) => {
        formData.append(`images[${index}]`, img.file);
    });

    // Add variants
    const variants = [];
    document.querySelectorAll('.variant-item').forEach((item, index) => {
        const inputs = item.querySelectorAll('input');
        if (inputs.length >= 4) {
            variants.push({
                name: inputs[0].value,
                values: inputs[1].value,
                price: inputs[2].value,
                stock: inputs[3].value
            });
        }
    });
    formData.append('variants', JSON.stringify(variants));

    // Simulate API call
    setTimeout(() => {
        if (currentEditingProduct) {
            showToast('Sản phẩm đã được cập nhật thành công!', 'success');
        } else {
            showToast('Sản phẩm mới đã được thêm thành công!', 'success');
        }
        closeModal();
        // Here you would typically refresh the product list
    }, 1000);
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
        // Mock product data for editing
        const mockProductData = {
            name: 'Áo Thun Nam Basic Cotton',
            sku: 'TSB001',
            category: 'ao-thun',
            description: 'Áo thun nam chất liệu cotton cao cấp',
            price: 299000,
            costPrice: 150000,
            stock: 156,
            status: 'active',
            order: 1,
            tags: 'áo thun, nam, cotton, basic'
        };
        openModal(true, mockProductData);
    } else if (e.target.classList.contains('action-delete')) {
        if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
            showToast('Sản phẩm đã được xóa thành công!', 'success');
        }
    } else if (e.target.classList.contains('action-view')) {
        showToast('Tính năng xem chi tiết đang được phát triển!', 'success');
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