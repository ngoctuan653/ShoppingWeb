const currentUserId = window.currentUserId;
const receiverId = window.receiverId;
let stompClient = null;

function toggleChatBox() {
    const chat = document.getElementById("chatContainer");
    const isVisible = chat.style.display === "flex";

    if (!isVisible) {
        loadChatHistory();
    }

    chat.style.display = isVisible ? "none" : "flex";
}

function loadChatHistory() {
    fetch(`/api/chat/history/${receiverId}`)
        .then(response => response.json())
        .then(messages => {
            const chatBox = document.getElementById("chatBox");
            chatBox.innerHTML = ""; // Clear old content
            messages.forEach(displayMessage);
            chatBox.scrollTop = chatBox.scrollHeight;
        });
}

function connectWebSocket() {
    const socket = new SockJS('/ws-chat');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, () => {
        stompClient.subscribe('/topic/messages/' + currentUserId, function (msg) {
            const message = JSON.parse(msg.body);
            displayMessage(message);
        });
    });
}

function sendMessage() {
    const input = document.getElementById("chatInput");
    const content = input.value.trim();
    if (!content) return;

    const message = {
        senderId: currentUserId,
        receiverId: receiverId,
        content: content
    };

    stompClient.send("/app/chat/send", {}, JSON.stringify(message));

    // 👇 Hiển thị tin nhắn ngay lập tức
    displayMessage(message);

    input.value = '';
}

function displayMessage(message) {
    const chatBox = document.getElementById("chatBox");
    const msgDiv = document.createElement("div");

    msgDiv.textContent = (message.senderId === currentUserId ? "" : "") + message.content;

    // Style mỗi tin nhắn 1 dòng riêng, căn lề, màu nền
    msgDiv.style.backgroundColor = (message.senderId === currentUserId ? "#d1e7dd" : "#f8d7da");
    msgDiv.style.padding = "8px";
    msgDiv.style.borderRadius = "10px";
    msgDiv.style.maxWidth = "80%";
    msgDiv.style.margin = "4px 0";
    msgDiv.style.display = "inline-block";
    msgDiv.style.clear = "both";
    msgDiv.style.float = (message.senderId === currentUserId ? "right" : "left");

    chatBox.appendChild(msgDiv);
    chatBox.appendChild(document.createElement("div")); // xuống dòng

    // Tự động cuộn xuống cuối
    chatBox.scrollTop = chatBox.scrollHeight;
}

window.addEventListener("DOMContentLoaded", () => {
    connectWebSocket();
    loadChatHistory(); // <-- thêm dòng này để luôn hiển thị tin nhắn ngay sau khi load trang
});
