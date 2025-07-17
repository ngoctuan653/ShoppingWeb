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
        })
        .catch(err => {
            showToast(err.message, 'danger'); // hiện màu đỏ nếu lỗi (vd: chưa login)
        });
}


function showToast(message, type) {
    const toastEl = document.getElementById("cart-toast");
    const toastBody = document.getElementById("toast-message");

    toastBody.textContent = message;

    // Xóa các màu cũ
    toastEl.classList.remove("bg-success", "bg-danger");
    toastEl.classList.add("bg-" + type);

    const toast = new bootstrap.Toast(toastEl, {
        delay: 2000,
    });
    toast.show();
}