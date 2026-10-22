(() => {
    'use strict';
    const root = document.getElementById('recovery');
    if (!root) return;
    const api = root.dataset.api;
    const emailForm = document.getElementById('email-form');
    const otpForm = document.getElementById('otp-form');
    const passwordForm = document.getElementById('password-form');
    const emailInput = document.getElementById('recovery-email');
    const otpInput = document.getElementById('otp');
    const message = document.getElementById('recovery-message');
    const network = document.getElementById('network-status');
    let busy = false;
    let resendAt = 0;
    let sendRequest = null;
    let verifyRequest = null;
    let uncertainReset = false;
    const steps = {EMAIL: 'email-form', OTP: 'otp-step', PASSWORD: 'password-form', DONE: 'done-step', EXPIRED: 'expired-step'};

    function notify(text, error = false) {
        message.textContent = text;
        message.className = 'alert ' + (error ? 'error' : 'success');
        message.hidden = !text;
    }

    function buttons() {
        root.querySelectorAll('button').forEach(button => { button.disabled = busy || !navigator.onLine; });
        const resend = document.getElementById('resend');
        const seconds = Math.max(0, Math.ceil((resendAt - Date.now()) / 1000));
        resend.disabled = busy || !navigator.onLine || seconds > 0;
        resend.textContent = seconds ? `Gửi lại sau ${seconds}s` : 'Gửi lại OTP';
        passwordForm.querySelector('button').disabled = busy || !navigator.onLine || uncertainReset;
    }

    function render(data) {
        Object.entries(steps).forEach(([state, id]) => { document.getElementById(id).hidden = state !== data.state; });
        if (data.email) emailInput.value = data.email;
        if (data.state !== 'OTP') otpInput.value = '';
        if (data.state === 'DONE' || data.state === 'EXPIRED') passwordForm.reset();
        buttons();
    }

    async function request(path, body) {
        const abort = new AbortController();
        const timer = setTimeout(() => abort.abort(), 25000);
        try {
            const headers = {'Accept': 'application/json'};
            if (body) {
                headers['Content-Type'] = 'application/json';
                headers[document.querySelector('meta[name="csrf-header"]').content] = document.querySelector('meta[name="csrf-token"]').content;
            }
            const response = await fetch(api + path, {method: body ? 'POST' : 'GET', headers,
                body: body ? JSON.stringify(body) : undefined, credentials: 'same-origin', cache: 'no-store', signal: abort.signal});
            if (response.status === 403) throw new Error('Phiên đã hết hạn. Tải lại trang để tiếp tục hoặc yêu cầu OTP mới.');
            if (!response.headers.get('content-type')?.includes('application/json')) throw new Error('Không đọc được phản hồi. Tải lại trang để kiểm tra trạng thái.');
            const data = await response.json();
            if (!response.ok) throw new Error(data.message || 'Chưa xử lý được yêu cầu. Hãy kiểm tra trạng thái rồi thử lại.');
            return data;
        } finally { clearTimeout(timer); }
    }

    async function run(action) {
        if (busy) return;
        if (!navigator.onLine) { showNetwork(); return; }
        busy = true;
        buttons();
        try { await action(); }
        catch (error) {
            const lost = error instanceof TypeError || error.name === 'AbortError';
            notify(lost ? 'Chưa nhận được phản hồi. Yêu cầu có thể đã được xử lý. Giữ trang này và bấm Kiểm tra trạng thái khi có mạng.' : error.message, true);
        } finally { busy = false; buttons(); }
    }

    async function check() {
        const data = await request('/status');
        uncertainReset = false;
        render(data);
        if (data.state === 'PASSWORD') notify('Email đã xác minh. Bạn có thể nhập mật khẩu mới; nếu lần trước chưa hoàn tất, hãy gửi lại.');
        else if (data.state === 'DONE') notify('Đã xác nhận mật khẩu được cập nhật thành công.');
    }

    async function send() {
        const email = emailInput.value.trim();
        if (!sendRequest || sendRequest.email !== email) sendRequest = {email, requestId: crypto.randomUUID()};
        const data = await request('/send', sendRequest);
        sendRequest = null;
        verifyRequest = null;
        otpInput.value = '';
        resendAt = Date.now() + 60000;
        render(data);
        notify(data.message);
    }

    emailForm.addEventListener('submit', event => { event.preventDefault(); run(send); });
    document.getElementById('resend').addEventListener('click', () => run(send));
    otpForm.addEventListener('submit', event => {
        event.preventDefault();
        run(async () => {
            const otp = otpInput.value;
            if (!verifyRequest || verifyRequest.otp !== otp) verifyRequest = {otp, requestId: crypto.randomUUID()};
            const data = await request('/verify', verifyRequest);
            verifyRequest = null;
            render(data);
            notify('Xác minh thành công. Nhập mật khẩu mới.');
        });
    });
    passwordForm.addEventListener('submit', event => {
        event.preventDefault();
        run(async () => {
            const newPassword = document.getElementById('new-password').value;
            const confirmPassword = document.getElementById('confirm-password').value;
            if (newPassword !== confirmPassword) { notify('Hai mật khẩu chưa khớp.', true); return; }
            // No password/OTP is written to localStorage, sessionStorage or URL.
            uncertainReset = true;
            const data = await request('/reset', {newPassword, confirmPassword});
            uncertainReset = false;
            render(data);
        });
    });
    for (const id of ['change-email', 'restart']) {
        document.getElementById(id).addEventListener('click', () => { render({state: 'EMAIL'}); notify(''); });
    }
    document.getElementById('check-status').addEventListener('click', () => run(check));
    function showNetwork() {
        network.hidden = navigator.onLine;
        network.textContent = 'Đang mất kết nối. Dữ liệu đã nhập vẫn được giữ trên trang; OTP vẫn hết hạn theo thời gian của server.';
        buttons();
    }
    window.addEventListener('offline', showNetwork);
    window.addEventListener('online', () => { showNetwork(); run(check); });
    setInterval(buttons, 1000);
    showNetwork();
    run(check);
})();
