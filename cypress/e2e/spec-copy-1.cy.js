describe('Test Luồng Đăng Nhập và Quản Lý Tài Khoản', () => {

  const baseUrl = 'http://localhost:8080'; 

  beforeEach(() => {
    cy.viewport(1440, 900); 
  });

  it('Kịch bản 1: Đăng nhập hệ thống vận hành', () => {
    // 1. Mở trang đăng nhập Ops
    cy.visit(`${baseUrl}/ops-login`);

    // 2. Kiểm tra các ô input có hiển thị đầy đủ không
    cy.get('input#username').should('be.visible');
    cy.get('input#password').should('be.visible');

    // 3. Nhập tài khoản và mật khẩu admin
    cy.get('input#username').type('admin');
    cy.get('input#password').type('admin123'); 

    // 4. Bấm nút Đăng nhập
    cy.get('button[type="submit"]').click();

    // 5. Kiểm tra xem đăng nhập thành công chưa (Chuyển đến trang quản trị)
    cy.url().should('include', '/admin'); 
  });

  it('Kịch bản 2: Tìm kiếm tài khoản trong Master Data', () => {
    // Đăng nhập nhanh trước khi test
    cy.visit(`${baseUrl}/ops-login`);
    cy.get('input#username').type('admin');
    cy.get('input#password').type('admin123');
    cy.get('button[type="submit"]').click();

    // 1. Chuyển đến trang Account Management
    cy.visit(`${baseUrl}/admin/master-data?tab=Account+Management`);
    cy.url().should('include', 'Account+Management');

    // 2. Gõ từ khóa tìm kiếm
    // Đổi sang 'admin' để chắc chắn có dữ liệu trả về (vì tài khoản đang đăng nhập là admin)
    const searchKeyword = 'admin';
    cy.get('input#search-input').should('be.visible').clear().type(searchKeyword);

    // 3. Chờ bảng lọc kết quả: Dùng ':visible' để loại bỏ các hàng bị ẩn (display: none)
    cy.get('table tbody tr:visible').should('have.length.at.least', 1);
    
    // 4. Kiểm tra dòng đầu tiên xem có chứa từ khóa không, bỏ qua chữ hoa chữ thường
    cy.get('table tbody tr:visible').first().invoke('text').then((text) => {
      expect(text.toLowerCase()).to.contain(searchKeyword.toLowerCase());
    });
  });
});
