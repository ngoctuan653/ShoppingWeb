window.addEventListener('DOMContentLoaded', function () {
    const toastEl = document.getElementById('contactToast');
    if (toastEl && toastEl.querySelector('.toast-body').textContent.trim() !== '') {
        const toast = new bootstrap.Toast(toastEl, {delay: 4000});
        toast.show();
    }
});


document.getElementById("applyDiscount").addEventListener("click", function () {
    const code = document.getElementById("discountCode").value.trim().toUpperCase();
    const feedback = document.getElementById("discountFeedback");
    const discountRow = document.getElementById("discountRow");
    const finalTotalRow = document.getElementById("finalTotalRow");
    const discountAmountEl = document.getElementById("discountAmount");
    const finalTotalEl = document.getElementById("finalTotal");
    const hiddenInput = document.getElementById("discountCodeHidden");

    if (!code) {
        feedback.textContent = "Please enter a discount code.";
        feedback.className = "form-text text-danger";
        return;
    }

    fetch(`/api/discounts/validate?code=${code}`)
        .then(res => {
            if (!res.ok) throw new Error("Invalid code");
            return res.json();
        })
        .then(data => {
            const percentage = parseFloat(data.percentage);
            const cartTotal = parseFloat(document.getElementById("cartTotalValue").value);
            const discountValue = cartTotal * (percentage / 100);
            const newTotal = Math.max(0, cartTotal - discountValue);

            discountAmountEl.textContent = `- $${discountValue.toFixed(2)}`;
            finalTotalEl.textContent = `$${newTotal.toFixed(2)}`;
            discountRow.style.display = "flex";
            finalTotalRow.style.display = "flex";

            feedback.textContent = `✓ Code "${code}" applied (${percentage}% off)`;
            feedback.className = "form-text text-success";
            hiddenInput.value = code; // để gửi về backend khi submit
        })
        .catch(() => {
            discountRow.style.display = "none";
            finalTotalRow.style.display = "none";
            feedback.textContent = "❌ Invalid discount code";
            feedback.className = "form-text text-danger";
            hiddenInput.value = "";
        });
});

document.getElementById("removeDiscount").addEventListener("click", function () {
    const discountRow = document.getElementById("discountRow");
    const finalTotalRow = document.getElementById("finalTotalRow");
    const discountAmountEl = document.getElementById("discountAmount");
    const finalTotalEl = document.getElementById("finalTotal");
    const hiddenInput = document.getElementById("discountCodeHidden");
    const feedback = document.getElementById("discountFeedback");

    discountRow.style.display = "none";
    finalTotalRow.style.display = "none";
    discountAmountEl.textContent = "- $0.00";
    finalTotalEl.textContent = "$0.00";
    hiddenInput.value = "";
    feedback.textContent = "Discount removed.";
    feedback.className = "form-text text-warning";
});


document.addEventListener("DOMContentLoaded", () => {
    loadCheckoutCart();
});