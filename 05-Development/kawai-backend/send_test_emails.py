import os
import requests

API_KEY = "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"
FROM_EMAIL = "hoanien.00@gmail.com"
FROM_NAME = "Hòa Niên Retreat & Resort"
TO_EMAIL = "liungu2005@gmail.com"
TEMPLATE_DIR = r"D:\SWP391\su26-swp391-se2023-g2\05-Development\kawai-backend\src\main\resources\templates\email"

url = "https://api.sendgrid.com/v3/mail/send"
headers = {
    "Authorization": f"Bearer {API_KEY}",
    "Content-Type": "application/json",
}

SUBJECTS = {
    "booking-table": "[TEST] Xác nhận đặt bàn thành công #1001",
    "cancel-booking": "[TEST] Thông báo hủy đặt bàn #1002",
    "extend-hold": "[TEST] Gia hạn giữ bàn thành công #1001",
    "invoice": "[TEST] Hóa đơn điện tử HOANIEN – INV-2026-00042",
    "password-reset": "[TEST] Đặt lại mật khẩu tài khoản HOANIEN",
    "registration-otp": "[TEST] Xác thực OTP đăng ký tài khoản",
    "room-service": "[TEST] Xác nhận đơn phục vụ tại phòng #5001",
    "tour-booking-confirmation": "[TEST] Xác nhận đặt tour – TOUR-2026-0099",
    "tour-booking-cancelled": "[TEST] Hủy tour – TOUR-2026-0099",
}


def main():
    results = []
    files = [f for f in os.listdir(TEMPLATE_DIR) if f.lower().endswith(".html")]
    files.sort()

    if not files:
        print("Không tìm thấy template HTML nào.")
        return

    for filename in files:
        path = os.path.join(TEMPLATE_DIR, filename)
        name = os.path.splitext(filename)[0]
        try:
            with open(path, "r", encoding="utf-8") as fh:
                html = fh.read()
        except Exception as exc:
            results.append((name, False, f"Lỗi đọc file: {exc}"))
            continue

        subject = SUBJECTS.get(name, f"[TEST] {name}")
        payload = {
            "personalizations": [
                {
                    "to": [{"email": TO_EMAIL}],
                    "subject": subject,
                }
            ],
            "from": {
                "email": FROM_EMAIL,
                "name": FROM_NAME,
            },
            "content": [
                {
                    "type": "text/html",
                    "value": html,
                }
            ],
        }

        try:
            resp = requests.post(url, headers=headers, json=payload, timeout=30)
            if resp.status_code in (200, 202):
                results.append((name, True, f"Gửi thành công (status={resp.status_code})"))
            else:
                results.append((name, False, f"SendGrid lỗi {resp.status_code}: {resp.text[:400]}"))
        except Exception as exc:
            results.append((name, False, f"Lỗi request: {exc}"))

    print("===== Kết quả gửi test email =====")
    for name, ok, msg in results:
        tag = "OK" if ok else "FAIL"
        print(f"[{tag}] {name}: {msg}")


if __name__ == "__main__":
    main()
