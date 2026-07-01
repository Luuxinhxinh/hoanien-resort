describe('Kịch bản 3: Kiểm thử luồng Admin - Menu Nhà Hàng, Tour Lịch Trình, Quản Lý Dữ Liệu', () => {
  const baseUrl = 'http://localhost:8080';

  beforeEach(() => {
    cy.viewport(1440, 900);
    // Đăng nhập trước mỗi test case
    cy.visit(`${baseUrl}/ops-login`);
    cy.get('input#username').type('admin');
    cy.get('input#password').type('admin123');
    cy.get('button[type="submit"]').click();
    cy.url().should('include', '/admin');
  });

  it('Test 1: Điều hướng sang Quản lý Dữ liệu và test Menu Nhà Hàng', () => {
    // Vào Master Data
    cy.visit(`${baseUrl}/admin/master-data`);

    // Chuyển sang Tab Menu Nhà hàng (Restaurant Menu)
    cy.contains('.md-tab-link', 'Restaurant Menu').click();

    // ❌ ĐOẠN NÀY SẼ CỐ TÌNH BÁO LỖI (ĐỎ)
    // Kỳ vọng URL có chứa 'Restaurant+Menu' nhưng cố tình ghi sai thành 'ThucDon'
    cy.url().should('include', 'tab=ThucDon');

    // ✅ ĐOẠN NÀY SẼ PASS (XANH) - Bỏ comment để test pass sau khi thấy lỗi đỏ
    // cy.url().should('include', 'tab=Restaurant');

    // UI/UX: Kiểm tra xem bảng (table) dữ liệu của nhà hàng có hiển thị không
    // ❌ ĐOẠN NÀY SẼ CỐ TÌNH BÁO LỖI (ĐỎ)
    // Kỳ vọng là danh sách có đúng 999 món ăn (thực tế không thể nhiều thế)
    cy.get('table tbody tr').should('have.length', 999);

    // ✅ ĐOẠN NÀY SẼ PASS (XANH)
    // cy.get('table tbody tr').should('have.length.at.least', 1);
  });

  it('Test 2: Chuyển sang Tab Tour Lịch Trình và tương tác thanh công cụ', () => {
    cy.visit(`${baseUrl}/admin/master-data`);

    // Click vào Tab Lịch trình Tour (Tour Schedules)
    cy.contains('.md-tab-link', 'Tour Schedules').click();

    // ❌ ĐOẠN NÀY SẼ CỐ TÌNH BÁO LỖI (ĐỎ)
    // Kiểm tra UI nút "Thêm mới" trong Lịch trình Tour, cố tình coi như nó bị ẩn (ẩn thì không click được)
    cy.get('#btn-add-new').should('not.be.visible');

    // ✅ ĐOẠN NÀY SẼ PASS (XANH)
    // cy.get('#btn-add-new').should('be.visible').and('not.be.disabled');

    // Thử tính năng UI UX: gõ tìm kiếm lịch trình tour
    // ❌ ĐOẠN NÀY SẼ CỐ TÌNH BÁO LỖI (ĐỎ)
    // Gõ tìm chữ 'Hạ Long' nhưng lại expect ô input chứa chữ 'Sapa'
    cy.get('#search-input').type('Hạ Long').should('have.value', 'Sapa');

    // ✅ ĐOẠN NÀY SẼ PASS (XANH)
    // cy.get('#search-input').clear().type('Hạ Long').should('have.value', 'Hạ Long');
  });

  it('Test 3: Tương tác mở Modal (Bảng điền dữ liệu) và đóng lại', () => {
    // Vào trang Quản lý Role (hoặc bất kỳ trang nào có nút Thêm Mới)
    cy.visit(`${baseUrl}/admin/master-data?tab=Role+Management`);

    // Click nút thêm mới để bung Modal
    cy.get('#btn-add-new').click();

    // ❌ ĐOẠN NÀY SẼ CỐ TÌNH BÁO LỖI (ĐỎ)
    // Kiểm tra xem Modal có bung ra không, nhưng cố tình kiểm tra class sai '.modal-khong-ton-tai'
    cy.get('.adm-modal').should('have.class', 'modal-khong-ton-tai');

    // ✅ ĐOẠN NÀY SẼ PASS (XANH)
    // cy.get('.adm-modal').should('have.class', 'active'); // Tuỳ thuộc UI, có thể là .active hoặc .show

    // Tắt modal
    cy.get('.btn-close-modal').first().click({ force: true });

    // ❌ ĐOẠN NÀY SẼ CỐ TÌNH BÁO LỖI (ĐỎ)
    // Kì vọng modal vẫn còn chữ "Thêm mới" sau khi đóng (thực ra nó đã bị ẩn/đóng)
    cy.get('.adm-modal').should('be.visible');

    // ✅ ĐOẠN NÀY SẼ PASS (XANH)
    // cy.get('.adm-modal').should('not.have.class', 'active');
  });
});

