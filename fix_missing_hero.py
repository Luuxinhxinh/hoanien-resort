import os

backend_dir = '05-Development/kawai-backend/src/main/resources/templates/email'

def fix_room_service():
    path = os.path.join(backend_dir, 'room-service.html')
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    old_head = """<body>

  <div class="wrapper">

    <!-- HEADER -->
    <div class="header">
      <div class="brand">H O A N I E N</div>
      <div class="tagline">Retreat &amp; Resort</div>
    </div>

    <!-- BODY -->
    <div class="body">

      <div class="badge-wrap">
        <span class="badge">XÁC NHẬN ĐƠN PHỤC VỤ TẠI PHÒNG</span>
      </div>"""

    new_head = """<body>
  <div class="email-outer">
    <div class="wrapper">

      <!-- HEADER -->
      <div class="header">
        <div class="brand">Hòa Niên</div>
        <div class="logo-text">Retreat Resort &amp; Hub</div>
      </div>

      <!-- HERO -->
      <div class="hero">
        <span class="icon">🛎️</span>
        <h1>Xác nhận phục vụ phòng</h1>
        <p>Chi tiết đơn gọi món tại phòng của Quý khách</p>
      </div>

      <!-- BODY -->
      <div class="body">"""

    old_foot = """    <!-- FOOTER -->
    <div class="footer">
      <p>&copy; 2026 HOANIEN Retreat &amp; Resort. All rights reserved.</p>
    </div>

  </div>

</body>"""

    new_foot = """      <!-- FOOTER -->
      <div class="footer">
        <div class="resort-name">Hòa Niên</div>
        <p>
          📞 <span th:text="${resortPhone}">1900 xxxx</span> &nbsp;|&nbsp;
          🌐 <a th:href="${resortWebsite}" th:text="${resortWebsite}">hoaniensorretreat.vn</a>
        </p>
        <div class="footer-divider"></div>
        <p>
          Email này được gửi tự động — vui lòng không phản hồi.<br />
          © 2026 Hòa Niên Retreat Resort & Hub. Bảo lưu mọi quyền.
        </p>
      </div>

    </div>
  </div>
</body>"""

    content = content.replace(old_head, new_head)
    content = content.replace(old_foot, new_foot)

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)


def fix_refund_success():
    path = os.path.join(backend_dir, 'refund-success.html')
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    old_head = """<body>

  <div class="wrapper">
    <!-- HEADER -->
    <div class="header">
      <div class="brand">H O A N I E N</div>
      <div class="tagline">Retreat &amp; Resort</div>
    </div>

    <!-- BODY -->
    <div class="body">
      <div class="badge-wrap">
        <span class="badge">THÔNG BÁO HOÀN TIỀN THÀNH CÔNG</span>
      </div>"""

    new_head = """<body>
  <div class="email-outer">
    <div class="wrapper">

      <!-- HEADER -->
      <div class="header">
        <div class="brand">Hòa Niên</div>
        <div class="logo-text">Retreat Resort &amp; Hub</div>
      </div>

      <!-- HERO -->
      <div class="hero">
        <span class="icon">💸</span>
        <h1>Hoàn tiền thành công</h1>
        <p>Yêu cầu hoàn tiền của Quý khách đã được xử lý</p>
      </div>

      <!-- BODY -->
      <div class="body">"""

    old_foot = """    <!-- FOOTER -->
    <div class="footer">
      <p>&copy; 2026 HOANIEN Retreat &amp; Resort. All rights reserved.</p>
    </div>
  </div>

</body>"""

    new_foot = """      <!-- FOOTER -->
      <div class="footer">
        <div class="resort-name">Hòa Niên</div>
        <p>
          📞 <span th:text="${resortPhone}">1900 xxxx</span> &nbsp;|&nbsp;
          🌐 <a th:href="${resortWebsite}" th:text="${resortWebsite}">hoaniensorretreat.vn</a>
        </p>
        <div class="footer-divider"></div>
        <p>
          Email này được gửi tự động — vui lòng không phản hồi.<br />
          © 2026 Hòa Niên Retreat Resort & Hub. Bảo lưu mọi quyền.
        </p>
      </div>

    </div>
  </div>
</body>"""

    content = content.replace(old_head, new_head)
    content = content.replace(old_foot, new_foot)

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

fix_room_service()
fix_refund_success()
print("Fixed room service and refund success")
