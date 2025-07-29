const sizesAdd = [];

function addSize() {
    const sizeLabel = document.getElementById("sizeLabel").value.trim();
    const stockQuantity = document.getElementById("stockQuantity").value.trim();

    if (sizeLabel && !isNaN(parseInt(stockQuantity))) {
        sizesAdd.push({ sizeLabel, stockQuantity: parseInt(stockQuantity) });

        const li = document.createElement("li");
        li.innerText = `${sizeLabel} - SL: ${stockQuantity}`;
        document.getElementById("sizeList").appendChild(li);

        document.getElementById("sizeLabel").value = "";
        document.getElementById("stockQuantity").value = "";
    } else {
        Swal.fire({
            icon: 'warning',
            title: 'Vui lòng nhập đúng Size và Số lượng!',
            position: 'center',
            showConfirmButton: false,
            timer: 1500
        });
    }
}

document.getElementById("productForm").addEventListener("submit", async function (e) {
    e.preventDefault();

    const formData = new FormData();

    const product = {
        productName: document.getElementById("productName").value,
        description: document.getElementById("description").value,
        price: parseFloat(document.getElementById("price").value),
        categoryId: parseInt(document.getElementById("categoryId").value),
        subCategoryId: parseInt(document.getElementById("subCategoryId").value),
        brandId: parseInt(document.getElementById("brandId").value),
        status: document.getElementById("status").value,
        sizes: sizesAdd
    };

    formData.append("product", new Blob([JSON.stringify(product)], {
        type: "application/json"
    }));

    const imageInput = document.querySelector("input[name='image']");
    if (imageInput.files.length > 0) {
        formData.append("image", imageInput.files[0]);
    }

    try {
        const response = await fetch("http://localhost:8080/products/add", {
            method: "POST",
            body: formData
        });

        if (!response.ok) throw new Error("Send data is invalid");

        const result = await response.json();
        console.log("[DEBUG] Add product result:", result);

        // ❗ TẮT MODAL bằng Bootstrap API
        const modalElement = document.getElementById("addProductModal");
        const modalInstance = bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) {
            modalInstance.hide();
        }

        // ✅ Hiển thị thông báo ở giữa
        Swal.fire({
            icon: 'success',
            title: 'Add sucessfully !',
            showConfirmButton: false,
            timer: 2000,
            toast: true,
            position: 'top'
        });

        // ✅ Tự đóng modal & reset form sau 2s
        setTimeout(() => {
            const modal = bootstrap.Modal.getInstance(document.getElementById("addProductModal"));
            modal.hide();

            document.getElementById("productForm").reset();
            document.getElementById("sizeList").innerHTML = "";
            sizesAdd.length = 0;

            window.location.reload();
        }, 2000);
    } catch (err) {
        console.error("[ERROR]", err);
        Swal.fire({
            icon: 'error',
            title: 'Lỗi khi thêm sản phẩm!',
            text: err.message,
            position: 'center'
        });
    }
});
