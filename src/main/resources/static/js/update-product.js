// === Bắt sự kiện nút Edit để mở modal ===
document.querySelectorAll(".btn-edit").forEach(btn => {
    btn.addEventListener("click", async () => {
        const productId = btn.getAttribute("data-id");

        let sizes = [];
        try {
            const response = await fetch(`/${productId}/sizes`);
            if (!response.ok) throw new Error("Failed to fetch sizes");
            sizes = await response.json();
        } catch (err) {
            console.error("[ERROR] Không thể load size:", err);
            alert("Không thể tải size cho sản phẩm này!");
        }

        const product = {
            id: productId,
            productName: btn.getAttribute("data-name"),
            price: btn.getAttribute("data-price"),
            stockQuantity: btn.getAttribute("data-quantity"),
            description: btn.getAttribute("data-description"),
            category: { id: btn.getAttribute("data-category-id") },
            subcategory: { id: btn.getAttribute("data-subcategory-id") },
            brand: { id: btn.getAttribute("data-brand-id") },
            status: btn.getAttribute("data-status"),
            sizes: sizes
        };

        console.log("[DEBUG] Open Edit - Product with sizes:", product);
        openEditModal(product);
    });
});


const sizesUpdate = [];

function renderSizeListUpdate() {
    const list = document.getElementById("sizeListUpdate");
    list.innerHTML = "";
    sizesUpdate.forEach(size => {
        const li = document.createElement("li");
        li.classList.add("list-group-item", "d-flex", "justify-content-between", "align-items-center");

        li.innerHTML = `
            <span>${size.sizeLabel} - SL: ${size.stockQuantity}</span>
            <button type="button" class="btn btn-sm btn-outline-danger">❌</button>
        `;

        li.querySelector("button").addEventListener("click", () => {
            const index = sizesUpdate.findIndex(s =>
                s.sizeLabel === size.sizeLabel
            );
            if (index !== -1) {
                sizesUpdate.splice(index, 1);
                renderSizeListUpdate(); // cập nhật lại giao diện sau khi xoá
            }
        });

        list.appendChild(li);
    });
}

function addSizeUpdate() {
    const sizeLabel = document.getElementById("sizeLabelUpdate").value.trim();
    const stockQuantity = document.getElementById("stockQuantityUpdate").value.trim();

    if (sizeLabel && !isNaN(parseInt(stockQuantity))) {
        const quantity = parseInt(stockQuantity);
        const existingIndex = sizesUpdate.findIndex(s => s.sizeLabel === sizeLabel);

        if (existingIndex !== -1) {
            // Nếu đã tồn tại → cộng thêm số lượng
            sizesUpdate[existingIndex].stockQuantity += quantity;
        } else {
            sizesUpdate.push({
                sizeLabel: sizeLabel,
                stockQuantity: quantity
            });
        }

        renderSizeListUpdate();

        // Reset input
        document.getElementById("sizeLabelUpdate").value = "";
        document.getElementById("stockQuantityUpdate").value = "";

        console.log("[DEBUG] Current sizesUpdate:", sizesUpdate);
    } else {
        alert("Vui lòng nhập đúng Size và Số lượng!");
    }
}

function openEditModal(product) {
    sizesUpdate.length = 0;
    document.getElementById("sizeListUpdate").innerHTML = "";

    // Gán thông tin sản phẩm
    document.getElementById("edit-id").value = product.id;
    document.getElementById("edit-productName").value = product.productName;
    if (editDescriptionEditor) {
        editDescriptionEditor.setData(product.description);
    }
    document.getElementById("edit-price").value = product.price;
    document.getElementById("edit-stockQuantity").value = product.stockQuantity;
    document.getElementById("edit-category").value = product.category.id;
    document.getElementById("edit-subcategory").value = product.subcategory.id;
    document.getElementById("edit-brand").value = product.brand.id;
    document.getElementById("edit-status").value = product.status;

    // Gán size
    product.sizes.forEach(size => {
        sizesUpdate.push({
            id: size.id,
            sizeLabel: size.sizeLabel,
            stockQuantity: size.stockQuantity
        });
    });


    renderSizeListUpdate();
}


// === Submit cập nhật sản phẩm ===
document.getElementById("updateForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const productId = document.getElementById("edit-id").value;
    const formData = new FormData();

    const validSizes = sizesUpdate.filter(s =>
        s.sizeLabel && !isNaN(parseInt(s.stockQuantity))
    ).map(s => ({
        id: s.id ?? null,
        sizeLabel: s.sizeLabel,
        stockQuantity: parseInt(s.stockQuantity)
    }));


    const updatedProduct = {
        productName: document.getElementById("edit-productName").value,
        description: document.getElementById("edit-description").value,
        price: parseFloat(document.getElementById("edit-price").value),
        // stockQuantity: parseInt(document.getElementById("edit-stockQuantity").value),
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
            alert("Update successfully!");
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
