---
name: jpa-performance
description: Bắt buộc kiểm tra hiệu năng JPA/Hibernate, cảnh báo N+1 query, kiểm tra fetch join và phân trang. Kích hoạt khi tương tác với Spring Data JPA, viết query, hoặc load danh sách thực thể để trả về cho template/Thymeleaf.
---

# JPA & Hibernate Performance Guidelines

## 1. Ngăn chặn lỗi N+1 Query
- Khi một Entity chứa các mối quan hệ `@OneToMany` hoặc `@ManyToOne` (ví dụ: Booking và RoomBooking), việc duyệt qua list bằng vòng lặp `th:each` trên Thymeleaf hoặc map sang DTO có thể trigger N+1 query (mỗi vòng lặp lại query thêm 1 lần).
- **Hậu quả:** Gây chậm toàn bộ trang, kết nối database bị chiếm dụng dẫn đến timeout hoặc đứt gãy luồng xử lý (thường gây lỗi đứt gãy hiển thị Thymeleaf như `ERR_INCOMPLETE_CHUNKED_ENCODING`).
- **Action:** Luôn kiểm tra xem query lấy data có đang dùng `JOIN FETCH` hoặc `@EntityGraph` để tải dữ liệu liên quan trong 1 query duy nhất hay không.

## 2. Trả dữ liệu lớn (Phân trang - Pagination)
- KHÔNG BAO GIỜ gọi `findAll()` trên các bảng dữ liệu phát sinh theo thời gian (Booking, Transaction, Log...) để ném thẳng ra UI.
- Luôn cân nhắc hoặc đề xuất sử dụng `Pageable` và đối tượng `Page<T>` để phân trang danh sách dài.

## 3. Khuyến nghị thiết kế
- Hạn chế sử dụng cấu trúc nhúng sâu (deep nested) khi trả ra View nếu không thực sự cần thiết.
- Khuyến nghị dùng DTO Projection thay vì trả nguyên Entity ra Thymeleaf để tránh lazy loading ngoài ý muốn tại tầng View.

## 4. Cẩn trọng với Polymorphic Querying (Truy vấn đa hình sai lệch)
- **Vấn đề:** Khi sử dụng `@Inheritance(strategy = InheritanceType.JOINED)`, nếu sử dụng Derived Query Method (tự sinh theo tên hàm, VD: `findByCustomer`) trong Repository của class CON (VD: `TourBookingRepository`) nhưng field điều kiện (customer) lại được định nghĩa ở class CHA (`Booking`), Spring Data JPA sẽ vô tình sinh ra câu lệnh SQL truy vấn trên bảng của class CHA. Nó sẽ fetch lên danh sách hỗn hợp chứa TẤT CẢ các entity con (gồm cả `TourBooking` và `RoomBooking`).
- **Hậu quả:** Kết quả truy vấn lúc runtime thực chất là `ArrayList<Booking>`. Khi Spring cố gắng ép kiểu danh sách này về `List<TourBooking>` như định nghĩa của interface, nó sẽ quăng ngay ngoại lệ `ConversionFailedException: Failed to convert from type [java.util.ArrayList<?>] to type [...]`.
- **Giải pháp (BẮT BUỘC):** Khi viết Query Method trong Repository của class con mà cần WHERE theo property của class cha, **LUÔN LUÔN phải chỉ định rõ bằng JPQL `@Query("SELECT c FROM ClassCon c WHERE c.propertyCha = :param")`**. Tuyệt đối không phó mặc cho Spring Data Derived Query.
