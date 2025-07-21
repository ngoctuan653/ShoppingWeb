// === Bắt sự kiện nút Edit để mở modal ===
document.querySelectorAll(".btn-edit").forEach(btn => {
    btn.addEventListener("click", () => {
        const product = {
            id: btn.getAttribute("data-id"),
            productName: btn.getAttribute("data-name"),
            price: btn.getAttribute("data-price"),
            stockQuantity: btn.getAttribute("data-quantity"),
            description: btn.getAttribute("data-description"),
            category: { id: btn.getAttribute("data-category-id") },
            subcategory: { id: btn.getAttribute("data-subcategory-id") },
            brand: { id: btn.getAttribute("data-brand-id") },
            status: btn.getAttribute("data-status"),
            sizes: [] // Nếu cần, load thêm
        };

        console.log("[DEBUG] Open Edit - Raw product:", product);
        openEditModal(product);
    });
});

const sizesUpdate = [];

function addSizeUpdate() {
    const sizeLabel = document.getElementById("sizeLabelUpdate").value.trim();
    const stockQuantity = document.getElementById("stockQuantityUpdate").value.trim();

    if (sizeLabel && !isNaN(parseInt(stockQuantity))) {
        const sizeObj = {
            sizeLabel: sizeLabel,
            stockQuantity: parseInt(stockQuantity)
        };

        sizesUpdate.push(sizeObj);

        const li = document.createElement("li");
        li.innerText = `${sizeObj.sizeLabel} - SL: ${sizeObj.stockQuantity}`;
        document.getElementById("sizeListUpdate").appendChild(li);

        // Reset input
        document.getElementById("sizeLabelUpdate").value = "";
        document.getElementById("stockQuantityUpdate").value = "";

        console.log("[DEBUG] Added size:", sizeObj);
        console.log("[DEBUG] Current sizesUpdate:", sizesUpdate);
    } else {
        alert("Vui lòng nhập đúng Size và Số lượng!");
    }
}

function openEditModal(product) {
    sizesUpdate.length = 0;
    document.getElementById("sizeListUpdate").innerHTML = "";

    document.getElementById("edit-id").value = product.id;
    document.getElementById("edit-productName").value = product.productName;
    document.getElementById("edit-description").value = product.description;
    document.getElementById("edit-price").value = product.price;
    document.getElementById("edit-stockQuantity").value = product.stockQuantity;
    document.getElementById("edit-category").value = product.category.id;
    document.getElementById("edit-subcategory").value = product.subcategory.id;
    document.getElementById("edit-brand").value = product.brand.id;
    document.getElementById("edit-status").value = product.status;

    if (product.sizes && Array.isArray(product.sizes)) {
        product.sizes.forEach(size => {
            sizesUpdate.push(size);
            const li = document.createElement("li");
            li.innerText = `${size.sizeLabel} - SL: ${size.stockQuantity}`;
            document.getElementById("sizeListUpdate").appendChild(li);
        });

        console.log("[DEBUG] Loaded sizes from product:", product.sizes);
    }

    const modalElement = document.getElementById('editProductModal');
    if (!modalElement) {
        console.error("Modal 'editProductModal' không tìm thấy trong DOM!");
        return;
    }

    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}

// === Submit cập nhật sản phẩm ===
document.getElementById("updateForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const productId = document.getElementById("edit-id").value;
    const formData = new FormData();

    const validSizes = sizesUpdate.filter(s =>
        s.sizeLabel && !isNaN(parseInt(s.stockQuantity))
    ).map(s => ({
        sizeLabel: s.sizeLabel,
        stockQuantity: parseInt(s.stockQuantity)
    }));

    const updatedProduct = {
        productName: document.getElementById("edit-productName").value,
        description: document.getElementById("edit-description").value,
        price: parseFloat(document.getElementById("edit-price").value),
        stockQuantity: parseInt(document.getElementById("edit-stockQuantity").value),
        categoryId: parseInt(document.getElementById("edit-category").value),
        subCategoryId: parseInt(document.getElementById("edit-subcategory").value),
        brandId: parseInt(document.getElementById("edit-brand").value),
        status: document.getElementById("edit-status").value,
        sizes: validSizes
    };

    const productBlob = new Blob([JSON.stringify(updatedProduct)], {
        type: "application/json"
    });

    console.log("[DEBUG] JSON payload:", JSON.stringify(updatedProduct, null, 2));
    formData.append("product", productBlob);

    const imageFile = document.getElementById("edit-image").files[0];
    if (imageFile) {
        formData.append("image", imageFile);
        console.log("[DEBUG] Image file added:", imageFile.name);
    }

    try {
        const response = await fetch(`http://localhost:8080/products/update/${productId}`, {
            method: "POST",
            body: formData
        });

        const text = await response.text();

        try {
            const json = JSON.parse(text);
            console.log("[DEBUG] Server JSON response:", json);
            alert("Cập nhật thành công!");
            window.location.reload();
        } catch (jsonError) {
            console.error("[ERROR] Response không phải JSON:\n", text);
            alert("Lỗi từ server: " + text);
        }
    } catch (err) {
        console.error("[ERROR] Lỗi fetch:", err);
        alert("Lỗi: " + err.message);
    }
});
