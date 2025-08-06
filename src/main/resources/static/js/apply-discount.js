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
            if (!res.ok) {
                return res.text().then(msg => { throw new Error(msg); });
            }
            return res.json();
        })
        .then(data => {
            const percentage = parseFloat(data.percentage);
            const cartTotal = parseFloat(document.getElementById("cartTotalValue").value);
            const discountValue = cartTotal * (percentage / 100);
            const newTotal = Math.max(0, cartTotal - discountValue);

            discountAmountEl.textContent = `- ${discountValue.toLocaleString('vi-VN')} VNĐ`;
            finalTotalEl.textContent = `${newTotal.toLocaleString('vi-VN')} VNĐ`;
            discountRow.style.display = "flex";
            finalTotalRow.style.display = "flex";

            feedback.textContent = `✓ Code "${code}" applied (${percentage}% off)`;
            feedback.className = "form-text text-success";
            hiddenInput.value = code;
        })
        .catch(err => {
            discountRow.style.display = "none";
            finalTotalRow.style.display = "none";
            feedback.textContent = `❌ ${err.message}`;
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
    discountAmountEl.textContent = "-0 VNĐ";
    finalTotalEl.textContent = "0 VNĐ";
    hiddenInput.value = "";
    feedback.textContent = "Discount removed.";
    feedback.className = "form-text text-warning";
});


function loadAvailableDiscounts() {
    fetch('/api/discounts/available')
        .then(res => res.json())
        .then(data => {
            const list = document.querySelector('#couponModal .list-group');
            list.innerHTML = "";

            data.forEach(discount => {
                const li = document.createElement('li');
                li.className = "list-group-item d-flex justify-content-between align-items-center";

                const label = document.createElement('span');
                label.innerHTML = `<strong>${discount.code}</strong> – ${discount.description}`;

                const applyBtn = document.createElement('button');
                applyBtn.className = "btn btn-sm";
                applyBtn.textContent = "Use";

                const notUsable = discount.used || discount.expired || discount.inactive || discount.outOfQuantity;

                if (notUsable) {
                    applyBtn.classList.add("btn-danger");
                    applyBtn.disabled = true;

                    if (discount.used) {
                        applyBtn.textContent = "Used";
                    } else if (discount.outOfQuantity) {
                        applyBtn.textContent = "Exhausted";
                    } else if (discount.expired) {
                        applyBtn.textContent = "Expired";
                    } else if (discount.inactive) {
                        applyBtn.textContent = "Inactive";
                    }
                } else {
                    applyBtn.classList.add("btn-outline-success");
                    applyBtn.addEventListener("click", () => {
                        document.getElementById("discountCode").value = discount.code;
                        document.getElementById("applyDiscount").click();
                        const modal = bootstrap.Modal.getInstance(document.getElementById('couponModal'));
                        modal.hide();
                    });
                }

                li.appendChild(label);
                li.appendChild(applyBtn);
                list.appendChild(li);
            });
        });
}

document.getElementById("couponModal").addEventListener("show.bs.modal", loadAvailableDiscounts);

