//Add
const sizesAdd = [];
function addSize() {
    const sizeLabel = document.getElementById("sizeLabel").value;
    const stockQuantity = document.getElementById("stockQuantity").value;

    if (sizeLabel && stockQuantity) {
        sizesAdd.push({ sizeLabel, stockQuantity: parseInt(stockQuantity) });

        const li = document.createElement("li");
        li.innerText = `${sizeLabel} - SL: ${stockQuantity}`;
        document.getElementById("sizeList").appendChild(li);

        // Reset input
        document.getElementById("sizeLabel").value = "";
        document.getElementById("stockQuantity").value = "";
    }
}

document.getElementById("productForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const form = document.getElementById("productForm");
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

    const imageInput = form.querySelector("input[name='image']");
    if (imageInput.files.length > 0) {
        formData.append("image", imageInput.files[0]);
    }

    fetch("http://localhost:8080/products/add", {
        method: "POST",
        body: formData
    })
        .then(response => {
            if (!response.ok) throw new Error("Send data is invalid");
            return response.json();
        })
        .then(result => {
            alert("Add product successfully!");
            console.log(result);
            window.location.reload();
        })
        .catch(err => {
            alert("Lỗi: " + err.message);
        });
});