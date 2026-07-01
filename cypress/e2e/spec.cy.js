describe('Bộ Test UI/UX Giao Diện Người Dùng', () => {

  // Trước mỗi ca test, đổi kích thước màn hình sang Laptop (Responsive Test)
  beforeEach(() => {
    cy.viewport(1280, 720); // Đặt độ phân giải màn hình chuẩn Laptop
    cy.visit('https://example.cypress.io'); // Thay URL trang web của bạn vào đây
  });

  it('Kiểm tra hiển thị UI cơ bản (Header, Logo, Text)', () => {
    // 1. Kiểm tra tiêu đề trang
    cy.title().should('include', 'Cypress');

    // 2. Kiểm tra thanh điều hướng (Navbar) có hiển thị không
    cy.get('.navbar').should('be.visible');

    // 3. Kiểm tra màu sắc CSS của một nút bấm
    // Lưu ý: Cypress đọc màu dạng RGB/RGBA thay vì mã Hex (#ffffff)
    cy.get('.navbar-brand')
      .should('have.css', 'color', 'rgb(240, 240, 240)') 
      .and('have.css', 'font-size', '18px');
  });

  it('Kiểm tra trải nghiệm tương tác UX (Form và Nút bấm)', () => {
    // 1. Cuộn trang xuống phần chứa Utilities để tương tác
    cy.contains('Utilities').scrollIntoView().click();

    // 2. Kiểm tra UX khi gõ vào ô Input
    cy.get('.action-email')
      .type('test-ui-ux@gmail.com')
      .should('have.value', 'test-ui-ux@gmail.com'); // Chắc chắn chữ hiển thị đúng

    // 3. Kiểm tra trạng thái Alert hoặc hiệu ứng sau khi tương tác
    // Ví dụ: Bấm submit và kiểm tra xem có thông báo thành công không
    // cy.get('#submit-btn').click();
    // cy.get('.success-message').should('be.visible').and('contain', 'Thành công!');
  });

  it('Kiểm tra UX trên thiết bị di động (Responsive)', () => {
    // Đổi sang màn hình iPhone XR
    cy.viewport('iphone-xr');
    
    // Thường trên mobile, menu sẽ biến thành nút Ba Gạch (Hamburger menu)
    // Kiểm tra xem nút đó có xuất hiện không và menu chính có bị ẩn đi không
    cy.get('.navbar-toggle').should('be.visible').click();
    cy.get('.navbar-nav').should('be.visible'); 
  });
});