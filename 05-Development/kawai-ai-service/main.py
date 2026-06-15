"""
AI Face Recognition Service - Kawai Resort (UC21)
Nhận diện khuôn mặt từ ảnh chụp (probe image) và so sánh với danh sách khách đi tour.

Cách gọi mới (Web-based):
    python main.py <probe_image_path> '<json_guest_list>'

Cách gọi cũ (Desktop camera - DEPRECATED):
    python main.py '<json_guest_list>'
"""
import sys
# Cấu hình UTF-8 cho console output trên Windows
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')
if hasattr(sys.stderr, 'reconfigure'):
    sys.stderr.reconfigure(encoding='utf-8')

import face_recognition
import numpy as np
from datetime import datetime
import json
import os

# ============================================================
# CONFIGURATION
# ============================================================
FACE_MATCH_TOLERANCE = 0.55  # Ngưỡng tolerance (thấp hơn = chặt hơn, mặc định face_recognition là 0.6)
MIN_FACE_MATCH_SCORE = 85    # Tỷ lệ % tối thiểu theo BR-TR-02

today_str = datetime.now().strftime("%Y-%m-%d")


def parse_guest_list(json_str):
    """Parse danh sách khách từ JSON."""
    try:
        guests = json.loads(json_str)
        # Gán ngày hôm nay cho các bản ghi
        for record in guests:
            if "tour_date" not in record:
                record["tour_date"] = today_str
        return guests
    except Exception as e:
        print(f"Lỗi parse JSON: {e}", file=sys.stderr)
        return [
            {"customer_id": 1, "name": "Lê Hoàng Nam", "image_path": "me.jpg", "tour_date": today_str},
            {"customer_id": 2, "name": "Nguyễn Văn An", "image_path": "ngoclon.jpg", "tour_date": today_str},
        ]


def load_known_faces(guest_list):
    """Nạp ảnh khuôn mặt đã biết từ danh sách khách đi tour hôm nay."""
    known_encodings = []
    known_names = []

    # Hardcoded check-in image for "Lê Hoàng Nam"
    hardcoded_path = r"D:\SWP391\su26-swp391-se2023-g2\03_sourcecode\kawai-backend\me.jpg"
    if os.path.exists(hardcoded_path):
        try:
            image = face_recognition.load_image_file(hardcoded_path)
            encodings = face_recognition.face_encodings(image)
            if encodings:
                known_encodings.append(encodings[0])
                known_names.append("Lê Hoàng Nam")
                print(f"✓ [HARDCODED] Đã nạp ảnh nhận diện: Lê Hoàng Nam ({hardcoded_path})")
        except Exception as e:
            print(f"Lỗi khi nạp ảnh cứng: {e}", file=sys.stderr)

    for record in guest_list:
        if record.get("tour_date") == today_str:
            image_path = record["image_path"]
            name = record["name"]

            # Bỏ qua nếu đã được nạp qua ảnh cứng để tránh trùng lặp
            if name == "Lê Hoàng Nam" and any(n == "Lê Hoàng Nam" for n in known_names):
                continue

            # Nếu path tương đối và không tồn tại, thử tìm trong thư mục kawai-backend
            if not os.path.isabs(image_path) and not os.path.exists(image_path):
                alt_path = os.path.join("..", "kawai-backend", image_path)
                if os.path.exists(alt_path):
                    image_path = alt_path

            if not os.path.exists(image_path):
                print(f"Cảnh báo: Không tìm thấy ảnh {image_path} cho {name}", file=sys.stderr)
                continue

            try:
                image = face_recognition.load_image_file(image_path)
                encodings = face_recognition.face_encodings(image)
                if encodings:
                    known_encodings.append(encodings[0])
                    known_names.append(name)
                    print(f"✓ Đã nạp ảnh: {name} ({image_path})")
                else:
                    print(f"Cảnh báo: Không phát hiện khuôn mặt trong ảnh {image_path} của {name}", file=sys.stderr)
            except Exception as e:
                print(f"Lỗi khi nạp ảnh {image_path}: {e}", file=sys.stderr)

    return known_encodings, known_names


def recognize_face(probe_image_path, known_encodings, known_names):
    """
    Nhận diện khuôn mặt từ ảnh probe.
    Returns: (matched_name, confidence_percent) hoặc (None, 0)
    """
    if not os.path.exists(probe_image_path):
        print(f"Lỗi: File ảnh probe không tồn tại: {probe_image_path}", file=sys.stderr)
        return None, 0

    try:
        probe_image = face_recognition.load_image_file(probe_image_path)
        probe_encodings = face_recognition.face_encodings(probe_image)

        if not probe_encodings:
            print("Không phát hiện khuôn mặt nào trong ảnh chụp.")
            return None, 0

        if not known_encodings:
            print("Không có khuôn mặt nào trong cơ sở dữ liệu để so sánh.")
            return None, 0

        # So sánh với tất cả các khuôn mặt đã biết
        probe_encoding = probe_encodings[0]
        face_distances = face_recognition.face_distance(known_encodings, probe_encoding)
        matches = face_recognition.compare_faces(known_encodings, probe_encoding, tolerance=FACE_MATCH_TOLERANCE)

        best_match_index = np.argmin(face_distances)
        best_distance = face_distances[best_match_index]

        # Tính phần trăm tin cậy sao cho khoảng cách bằng tolerance (0.55) tương ứng với 85% theo BR-TR-02
        if best_distance <= FACE_MATCH_TOLERANCE:
            confidence = 100.0 - (best_distance / FACE_MATCH_TOLERANCE) * 15.0
        else:
            confidence = max(0.0, 85.0 - ((best_distance - FACE_MATCH_TOLERANCE) / (1.0 - FACE_MATCH_TOLERANCE)) * 85.0)

        print(f"Khoảng cách tốt nhất: {best_distance:.4f}")
        print(f"Tỷ lệ tin cậy: {confidence:.1f}%")
        print(f"Kết quả so khớp: {matches}")

        if matches[best_match_index]:
            # Kiểm tra ngưỡng theo BR-TR-02 (≥ 85%)
            if confidence >= MIN_FACE_MATCH_SCORE:
                matched_name = known_names[best_match_index]
                print(f"✓ Nhận diện thành công: {matched_name} (tin cậy: {confidence:.1f}%)")
                return matched_name, confidence
            else:
                print(f"✗ Khuôn mặt khớp nhưng tin cậy thấp ({confidence:.1f}% < {MIN_FACE_MATCH_SCORE}%)")
                return None, confidence
        else:
            # Tìm khuôn mặt gần nhất nhưng không đủ tolerance
            closest_name = known_names[best_match_index]
            print(f"✗ Không khớp. Khuôn mặt gần nhất: {closest_name} (tin cậy: {confidence:.1f}%)")
            return None, confidence

    except Exception as e:
        print(f"Lỗi khi nhận diện: {e}", file=sys.stderr)
        return None, 0


def run_camera_recognition(known_encodings, known_names):
    """Mở webcam máy tính để nhận diện khuôn mặt thời gian thực."""
    try:
        import cv2
    except ImportError:
        print("\n[LỖI] Chưa cài đặt thư viện OpenCV để dùng webcam.")
        print("Vui lòng chạy lệnh: pip install opencv-python\n")
        sys.exit(1)

    print("\n=== ĐANG MỞ WEBCAM... NHẤN 'q' ĐỂ THOÁT ===")
    video_capture = cv2.VideoCapture(0)

    if not video_capture.isOpened():
        print("[LỖI] Không thể kết nối với webcam.")
        sys.exit(1)

    while True:
        # Chụp một frame từ webcam
        ret, frame = video_capture.read()
        if not ret:
            print("[LỖI] Không thể đọc frame từ webcam.")
            break

        # Chuyển đổi màu từ BGR (OpenCV) sang RGB (face_recognition)
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

        # Tìm vị trí và mã hóa các khuôn mặt trong frame
        face_locations = face_recognition.face_locations(rgb_frame)
        face_encodings = face_recognition.face_encodings(rgb_frame, face_locations)

        for (top, right, bottom, left), face_encoding in zip(face_locations, face_encodings):
            # So khớp với danh sách đã biết
            face_distances = face_recognition.face_distance(known_encodings, face_encoding)
            matches = face_recognition.compare_faces(known_encodings, face_encoding, tolerance=FACE_MATCH_TOLERANCE)

            name = "Unknown"
            color = (0, 0, 255) # Đỏ cho Unknown

            if len(face_distances) > 0:
                best_match_index = np.argmin(face_distances)
                best_distance = face_distances[best_match_index]
                if best_distance <= FACE_MATCH_TOLERANCE:
                    confidence = 100.0 - (best_distance / FACE_MATCH_TOLERANCE) * 15.0
                else:
                    confidence = max(0.0, 85.0 - ((best_distance - FACE_MATCH_TOLERANCE) / (1.0 - FACE_MATCH_TOLERANCE)) * 85.0)

                if matches[best_match_index] and confidence >= MIN_FACE_MATCH_SCORE:
                    name = f"{known_names[best_match_index]} ({confidence:.1f}%)"
                    color = (0, 255, 0) # Xanh lá cho Match thành công
                    print(f"[LIVE MATCH] {known_names[best_match_index]} - Tin cậy: {confidence:.1f}%")

            # Vẽ khung chữ nhật quanh khuôn mặt
            cv2.rectangle(frame, (left, top), (right, bottom), color, 2)

            # Vẽ nhãn tên dưới khuôn mặt
            cv2.rectangle(frame, (left, bottom - 35), (right, bottom), color, cv2.FILLED)
            font = cv2.FONT_HERSHEY_DUPLEX
            cv2.putText(frame, name, (left + 6, bottom - 6), font, 0.6, (255, 255, 255), 1)

        # Hiển thị frame kết quả
        cv2.imshow('Kawai Resort - AI FaceID Live Test', frame)

        # Thoát khi nhấn phím 'q'
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    # Dọn dẹp
    video_capture.release()
    cv2.destroyAllWindows()
    print("=== ĐÃ TẮT CAMERA ===")


def main():
    """
    Entry point: Hỗ trợ các chế độ:
    1. Web mode: python main.py <probe_image_path> '<json_guest_list>'
    2. Camera mode: python main.py (dùng webcam trực tiếp để test)
    """
    probe_image_path = None
    json_args = None

    if len(sys.argv) == 3:
        # Web mode: probe image path + guest list JSON
        probe_image_path = sys.argv[1]
        json_args = sys.argv[2]
        print(f"=== KAWAI AI FACE SERVICE (Web Mode) ===")
        print(f"Ảnh chụp: {probe_image_path}")
    elif len(sys.argv) == 2:
        # Chạy trực tiếp từ console với json khách hoặc camera test
        arg = sys.argv[1]
        if arg.endswith('.jpg') or arg.endswith('.png'):
            probe_image_path = arg
        else:
            json_args = arg
        print("=== KAWAI AI FACE SERVICE (CLI Mode) ===")
    else:
        # Mặc định chạy webcam test nếu không truyền tham số
        print("=== KAWAI AI FACE SERVICE (Live Camera Test Mode) ===")

    # 1. Parse danh sách khách
    if not json_args:
        # Tạo dữ liệu test mặc định trỏ tới me.jpg và ngoclon.jpg tại thư mục làm việc hiện tại
        json_args = json.dumps([
            {"customer_id": 1, "name": "Lê Hoàng Nam", "image_path": "me.jpg", "tour_date": today_str},
            {"customer_id": 2, "name": "Nguyễn Văn An", "image_path": "ngoclon.jpg", "tour_date": today_str},
        ])

    guest_list = parse_guest_list(json_args)
    print(f"Danh sách khách hôm nay: {[g['name'] for g in guest_list if g.get('tour_date') == today_str]}")

    # 2. Nạp ảnh khuôn mặt đã biết
    known_encodings, known_names = load_known_faces(guest_list)

    if not known_encodings:
        print("Không có khuôn mặt nào để so sánh. Kết thúc.")
        sys.exit(1)

    # 3. Nhận diện từ ảnh probe hoặc chạy live cam
    if probe_image_path:
        matched_name, confidence = recognize_face(probe_image_path, known_encodings, known_names)

        if matched_name:
            print(f"MATCH: {matched_name}")
            sys.exit(0)
        else:
            print(f"NO_MATCH (confidence: {confidence:.1f}%)")
            sys.exit(0)
    else:
        # Live camera mode
        run_camera_recognition(known_encodings, known_names)


if __name__ == "__main__":
    main()
