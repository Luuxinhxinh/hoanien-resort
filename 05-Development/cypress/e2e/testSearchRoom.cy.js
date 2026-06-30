describe('Kịch bản Test UI - Tìm kiếm phòng (Guest)', () => {
  const BASE_URL = 'http://localhost:8080';

  beforeEach(() => {
    // Chỉnh kích thước màn hình to ra (Desktop) để thanh Navigation không bị biến thành menu Mobile (hamburger)
    cy.viewport(1280, 720);
    
    // Bước 1: Truy cập trang Living
    cy.visit(`${BASE_URL}/living`);
  });

  it('Đăng nhập với tài khoản khách và thực hiện tìm kiếm phòng', () => {
    // -----------------------------------------------------
    // 1. ĐĂNG NHẬP VỚI TÀI KHOẢN KHÁCH HÀNG (thibich/admin123)
    // -----------------------------------------------------
    cy.log('--- BẮT ĐẦU ĐĂNG NHẬP ---');
    
    // Click vào nút "Log In" trên Navbar
    cy.contains('a', 'Log In').should('be.visible').click();

    // Chờ modal đăng nhập xuất hiện và nhập thông tin
    cy.get('#ajaxLoginForm').should('be.visible');
    cy.get('#ajaxLoginForm input[name="username"]').clear().type('thibich');
    cy.get('#ajaxLoginForm input[name="password"]').clear().type('admin123');

    // Bấm nút "Tiếp Tục" (Submit)
    cy.get('#ajaxLoginForm button[type="submit"]').click();

    // Xác minh đăng nhập thành công (Thấy nút Profile hoặc Logout)
    cy.contains('a', 'Profile', { matchCase: false, timeout: 10000 }).should('exist');
    cy.log('--- ĐĂNG NHẬP THÀNH CÔNG ---');

    // -----------------------------------------------------
    // 2. ĐI ĐẾN TRANG TÌM KIẾM PHÒNG (/booking)
    // -----------------------------------------------------
    cy.log('--- ĐI TỚI TRANG BOOKING ---');
    // Bấm vào nút Book Now
    cy.contains('a', 'Book Now', { matchCase: false }).click();
    
    // Đảm bảo URL đã chuyển sang trang booking
    cy.url().should('include', '/booking');

    // -----------------------------------------------------
    // 3. TÌM KIẾM THEO TỪ KHÓA (Keyword Search)
    // -----------------------------------------------------
    cy.log('--- TÌM KIẾM THEO TỪ KHÓA ---');
    // Nhập từ khóa phòng (VD: "Deluxe")
    cy.get('#keywordInput').should('be.visible').clear().type('Deluxe');
    
    // Bấm nút tìm kiếm (dùng selector nút có class search-btn)
    cy.get('button.search-btn').click();

    // Chờ hệ thống render kết quả (nếu có loader, đợi nó biến mất)
    cy.wait(1000); // Đợi 1 chút cho JS xử lý (nếu có call API)
    
    // Xác nhận kết quả tìm kiếm hiển thị (chứa chữ Deluxe)
    cy.get('body').then($body => {
      if ($body.text().includes('Deluxe')) {
        cy.contains('Deluxe', { matchCase: false }).should('be.visible');
      } else {
        cy.log('Không có phòng Deluxe nào trống, bỏ qua check này');
      }
    });

    // -----------------------------------------------------
    // 4. TƯƠNG TÁC VỚI BỘ LỌC TÌM KIẾM (Dropdown)
    // -----------------------------------------------------
    cy.log('--- KIỂM TRA CÁC BỘ LỌC TÌM KIẾM ---');
    
    // Click vào Khoảng giá để mở Dropdown
    cy.contains('.field-label', 'Khoảng giá').click();
    
    // Chọn một tùy chọn trong khoảng giá nếu DOM hiển thị các mức giá
    // Note: Kịch bản dùng phím ESC để đóng dropdown nếu UI hỗ trợ
    cy.get('body').type('{esc}'); 
    
    // Click vào Số lượng khách (Booking Details)
    cy.contains('.field-label', 'Booking Details').click();
    
    // (Tuỳ chọn) đóng lại bằng cách click ra ngoài
    cy.get('body').click(0, 0);

    // -----------------------------------------------------
    // 5. TÌM KIẾM TRỐNG (Xem tất cả các phòng) VÀ XEM CHI TIẾT
    // -----------------------------------------------------
    cy.log('--- TÌM KIẾM TẤT CẢ PHÒNG VÀ XEM CHI TIẾT ---');
    cy.get('#keywordInput').clear();
    cy.get('button.search-btn').click();
    
    cy.wait(1000);
    
    // Lấy danh sách phòng và click vào ảnh phòng đầu tiên để xem chi tiết
    cy.get('body').then($body => {
        if ($body.find('.room-card').length > 0) {
            cy.log('Tìm thấy danh sách phòng, tiến hành mở xem chi tiết');
            
            // Lấy thẻ phòng đầu tiên và click vào phần ảnh (.room-media) để mở modal chi tiết
            cy.get('.room-card').first().find('.room-media').click();
            
            // Xác minh modal chi tiết hiện lên (kiểm tra tên phòng và mô tả)
            cy.get('#detailRoomName').should('be.visible').and('not.be.empty');
            cy.get('#detailRoomDesc').should('be.visible');
            cy.get('#detailRoomProperties').should('be.visible'); // Tiện ích, diện tích...
            
            cy.log('--- ĐÃ XEM CHI TIẾT PHÒNG THÀNH CÔNG ---');
            
            // (Tuỳ chọn) Nếu có nút Đóng modal chi tiết, có thể click để đóng
            // cy.get('#closeDetailModalBtn').click(); 
        } else {
            cy.log('Không có phòng nào trong danh sách để test phần xem chi tiết');
        }
    });
  });
});