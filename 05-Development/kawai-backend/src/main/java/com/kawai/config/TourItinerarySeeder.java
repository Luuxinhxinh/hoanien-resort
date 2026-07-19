package com.kawai.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TourItinerarySeeder implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // Guard: chỉ chạy khi Tours đã có data (data.sql đã seed xong)
        Integer tourCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Tours", Integer.class);
        if (tourCount == null || tourCount < 10) {
            System.out.println("[ItinerarySeeder] ⚠️ Chưa đủ 10 Tours trong DB (hiện có: " + tourCount + "). Bỏ qua seeder lần này.");
            return;
        }
        System.out.println("[ItinerarySeeder] Khởi chạy đồng bộ dữ liệu 10 Tour và Chi tiết hành trình...");

        // Đảm bảo sửa lỗi chính tả cho Tour số 8 (Mũ Né -> Mũi Né) nếu DB cũ đã tồn tại record
        jdbcTemplate.execute("UPDATE Tours SET tour_name = 'Bình Minh Cồn Cát – Mũi Né' WHERE tour_id = 8 AND (tour_name LIKE '%Mũ Né%' OR tour_name LIKE '%Mu Né%')");

        // Đảm bảo toàn bộ 10 Tours hiện tại đều có is_insurance_required = TRUE và insurance_price = 50000
        jdbcTemplate.execute("UPDATE Tours SET is_insurance_required = TRUE, insurance_price = 50000");

        // 1. Chèn thông tin cơ bản cho 10 Tours (nếu chưa có)
        jdbcTemplate.execute("INSERT IGNORE INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, duration, duration_hours, is_insurance_required, insurance_price, short_quote, description, is_active) VALUES (1, 'Hành trình Cố đô', 'doantu', 7500000, 15, '10 Giờ', 10.0, TRUE, 50000, 'Tìm về hơi ấm vẹn nguyên của lòng biết ơn và sự gắn kết.', 'Chuyến đi đưa bạn ngược dòng thời gian về với nét đẹp trầm mặc của cố đô, khơi mở những câu chuyện di sản và thưởng thức phong vị ẩm thực cung đình xưa.', TRUE)");
        jdbcTemplate.execute("INSERT IGNORE INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, duration, duration_hours, is_insurance_required, insurance_price, short_quote, description, is_active) VALUES (2, 'Hồn Quê Xứ Quảng', 'dongnoi', 5800000, 12, '10 Giờ', 10.0, TRUE, 50000, 'Lắng nghe nhịp điệu mộc mạc của đất mẹ và hồn quê xứ Quảng.', 'Hành trình chạm vào những điều bình dị mà sâu lắng của đất Quảng: từ những con phố rêu phong ở Hội An, hương đất nung bên dòng sông Thu Bồn đến vị mặn mòi của một làng rau lâu đời.', TRUE)");
        jdbcTemplate.execute("INSERT IGNORE INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, duration, duration_hours, is_insurance_required, insurance_price, short_quote, description, is_active) VALUES (3, 'Hồn Đất Văn Lâm', 'disan', 6500000, 12, '10 Giờ', 10.0, TRUE, 50000, 'Chạm vào hồn cốt của thời gian qua những tạo tác từ đôi bàn tay nghệ nhân.', 'Hành trình tìm về vẻ đẹp sơn thủy hữu tình của vùng đất cố đô cổ kính, nơi bạn được trò chuyện và cùng các nghệ nhân lưu giữ những làng nghề truyền thống trăm năm.', TRUE)");
        jdbcTemplate.execute("INSERT IGNORE INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, duration, duration_hours, is_insurance_required, insurance_price, short_quote, description, is_active) VALUES (4, 'Thiền Giữa Hương Sen', 'tinhlang', 8200000, 8, '10 Giờ', 10.0, TRUE, 50000, 'Sự thanh lọc thuần khiết cho thân - tâm - trí giữa vùng sông nước mờ sương.', 'Một ngày trốn khỏi phố thị để về với đại đầm sen, mượn hương hoa và sóng nước miền Tây làm dịu lại tâm hồn, tìm lại sự bình an sâu lắng bên trong bạn.', TRUE)");
        jdbcTemplate.execute("INSERT IGNORE INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, duration, duration_hours, is_insurance_required, insurance_price, short_quote, description, is_active) VALUES (5, 'Vịnh Xanh Kì Vĩ', 'halong', 7500000, 15, '10 Giờ', 10.0, TRUE, 50000, 'Du thuyền lướt nhẹ giữa ngàn khơi, thưởng ngoạn kỳ quan thiên nhiên rực rỡ.', 'Chuyến hải trình thong dong đưa bạn lướt qua những đảo đá nhấp nhô của vịnh Bắc Bộ, khám phá thế giới thạch nhũ kỳ vĩ được tạo hóa giấu kín ngàn năm và đón hoàng hôn buông trên mặt biển.', TRUE)");
        jdbcTemplate.execute("INSERT IGNORE INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, duration, duration_hours, is_insurance_required, insurance_price, short_quote, description, is_active) VALUES (6, 'Sapa Mây Mù', 'sapa', 8200000, 10, '11 Giờ', 11.0, TRUE, 50000, 'Gặp gỡ những nụ cười hồn hậu giữa vùng mây trời sương phủ.', 'Hành trình dạo bước qua những nấc thang ruộng bậc thang xanh mướt tại bản Cát Cát, tìm hiểu cuộc sống mộc mạc của đồng bào người H''Mông và lắng lại tâm hồn giữa núi rừng Tây Bắc.', TRUE)");
        jdbcTemplate.execute("INSERT IGNORE INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, duration, duration_hours, is_insurance_required, insurance_price, short_quote, description, is_active) VALUES (7, 'Khám Phá Rừng Xanh', 'cattien', 9500000, 8, '14 Giờ', 14.0, TRUE, 50000, 'Lắng nghe tiếng gọi thì thầm từ đại ngàn xanh thẳm.', 'Chuyến băng rừng rậm Nam Cát Tiên đầy cảm xúc, đưa bạn ghé thăm đầm lầy bảo tồn tự nhiên, thưởng thức bữa tối bên rừng và trải nghiệm ngắm thú đêm hoang dã.', TRUE)");
        jdbcTemplate.execute("INSERT IGNORE INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, duration, duration_hours, is_insurance_required, insurance_price, short_quote, description, is_active) VALUES (8, 'Cát Vàng Mũi Né', 'muine', 6500000, 10, '11 Giờ', 11.0, TRUE, 50000, 'Đón những vệt nắng đầu ngày rực rỡ trên sa mạc cát mênh mông.', 'Trải nghiệm cảm giác phấn khích vượt đồi cát bằng xe ATV đón bình minh, khám phá dòng suối Tiên huyền thoại và thưởng thức mỹ vị biển khơi.', TRUE)");
        jdbcTemplate.execute("INSERT IGNORE INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, duration, duration_hours, is_insurance_required, insurance_price, short_quote, description, is_active) VALUES (9, 'Đại Dương Phú Quốc', 'phuquoc', 8500000, 12, '10 Giờ', 10.0, TRUE, 50000, 'Hòa mình vào làn nước xanh lục bảo và vũ điệu rực rỡ dưới lòng đại dương.', 'Đồng hành cùng cano cao cấp lướt qua những hòn đảo hoang sơ, lặn ngắm rạn san hô đa sắc màu và tận hưởng bữa chiều ngắm hoàng hôn lãng mạn trên biển.', TRUE)");
        jdbcTemplate.execute("INSERT IGNORE INTO Tours (tour_id, tour_name, tour_type, base_price, max_capacity, duration, duration_hours, is_insurance_required, insurance_price, short_quote, description, is_active) VALUES (10, 'Sông Nước Cần Thơ', 'cantho', 6800000, 12, '10 Giờ', 10.0, TRUE, 50000, 'Tròn vị ngọt ngào của trái chín trĩu cành miền sông nước.', 'Đón bình minh trên chiếc ghe máy mộc mạc phục vụ riêng, hòa mình vào chợ nổi Cái Răng sầm uất, khám phá miệt vườn trĩu quả và học làm bánh dân gian Nam Bộ.', TRUE)");

        // 2. Đồng bộ bảng Tour_Prices
        jdbcTemplate.execute("DELETE FROM Tour_Prices");
        jdbcTemplate.execute("""
            INSERT INTO Tour_Prices (tour_price_id, tour_id, age_from, age_to, ticket_price, combo_discount_price, is_active) VALUES 
            (1, 1, 12, 100, 7500000, 6800000, TRUE),
            (2, 1, 2, 11, 3750000, 3400000, TRUE),
            (3, 2, 12, 100, 5800000, 5200000, TRUE),
            (4, 2, 2, 11, 2900000, 2600000, TRUE),
            (5, 3, 12, 100, 6500000, 5800000, TRUE),
            (6, 3, 2, 11, 3250000, 2900000, TRUE),
            (7, 4, 12, 100, 8200000, 7500000, TRUE),
            (8, 4, 2, 11, 4100000, 3750000, TRUE),
            (9, 5, 12, 100, 7500000, 6800000, TRUE),
            (10, 5, 2, 11, 3750000, 3400000, TRUE),
            (11, 6, 12, 100, 8200000, 7500000, TRUE),
            (12, 6, 2, 11, 4100000, 3750000, TRUE),
            (13, 7, 12, 100, 9500000, 8600000, TRUE),
            (14, 7, 2, 11, 4750000, 4300000, TRUE),
            (15, 8, 12, 100, 6500000, 5800000, TRUE),
            (16, 8, 2, 11, 3250000, 2900000, TRUE),
            (17, 9, 12, 100, 8500000, 7800000, TRUE),
            (18, 9, 2, 11, 4250000, 3900000, TRUE),
            (19, 10, 12, 100, 6800000, 6000000, TRUE),
            (20, 10, 2, 11, 3400000, 3000000, TRUE)
            """);

        // 3. Xoá dữ liệu các bảng liên quan để tránh xung đột khoá ngoại và nạp lại Itinerary
        jdbcTemplate.execute("DELETE FROM Checkpoint_Attendance");
        jdbcTemplate.execute("DELETE FROM Run_Itinerary_Status");
        jdbcTemplate.execute("DELETE FROM Tour_Itinerary_Details");
        jdbcTemplate.execute("DELETE FROM Tour_Itineraries");

        // 4. Chèn lại 10 Itineraries cho 10 Tour
        jdbcTemplate.execute("""
            INSERT INTO Tour_Itineraries (itinerary_id, tour_id, day_number, day_title, summary) VALUES 
            (1, 1, 1, 'Hành trình Cố đô', 'Khám phá Đại Nội Huế và lăng tẩm hoàng cung triều Nguyễn.'),
            (2, 2, 1, 'Hồn Quê Xứ Quảng', 'Tham quan Hội An cổ kính và trải nghiệm cày cấy làng Trà Quế.'),
            (3, 3, 1, 'Hồn Đất Văn Lâm', 'Đi thuyền Vân Long ngắm cảnh sơn thuỷ và xem dệt thêu thủ công.'),
            (4, 4, 1, 'Thiền Giữa Hương Sen', 'Ngắm sen nở mờ sương và tập yoga thiền trên sông nước.'),
            (5, 5, 1, 'Vịnh Xanh Kì Vĩ', 'Tàu sang lướt sóng ngắm hang luồn, hòn trống mái.'),
            (6, 6, 1, 'Sapa Mây Mù', 'Thăm bản H’mông cổ xưa, tìm hiểu nghệ thuật nhuộm chàm.'),
            (7, 7, 1, 'Khám Phá Rừng Xanh', 'Đi bộ xuyên rừng ngắm bằng lăng cổ thụ kì vĩ.'),
            (8, 8, 1, 'Cát Vàng Mũi Né', 'Ngắm bình minh cồn cát trắng, tham quan Suối Tiên.'),
            (9, 9, 1, 'Đại Dương Phú Quốc', 'Lặn cano 4 đảo nhỏ hoang sơ hoà vào san hô rực rỡ.'),
            (10, 10, 1, 'Sông Nước Cần Thơ', 'Ăn sáng hủ tiếu chợ nổi Cái Răng sôi động.')
            """);

        // 5. Chèn 63 hoạt động chi tiết (Tour Itinerary Details) bao gồm cả các hoạt động đón/trả khách
        jdbcTemplate.execute("""
            INSERT INTO Tour_Itinerary_Details (detail_id, itinerary_id, start_time, end_time, location_id, activity_title, activity_description, meal_type) VALUES 
            -- 1. Đoàn Tụ (Huế)
            (1, 1, '07:30:00', '08:00:00', NULL, 'Đón khách & Làm thủ tục', 'Đón khách tại sảnh chính Resort, làm thủ tục nhận diện khuôn mặt FaceID và kiểm tra hành lý trước khi khởi hành.', NULL),
            (2, 1, '08:00:00', '11:30:00', NULL, 'Dấu xưa rêu phong – Khám phá Đại Nội', 'Dạo bước qua những lầu son gác tía, lắng nghe tiếng vọng của thời gian tại Điện Thái Hòa, Thế Miếu – nơi lưu giữ trọn vẹn nét uy nghi một thuở hoàng kim.', NULL),
            (3, 1, '11:30:00', '14:00:00', NULL, 'Vị đượm cung đình – Thưởng yến và nếm trà', 'Thưởng thức bữa trưa được tái hiện theo phong cách cung đình. Sau đó, ghé thăm Duyệt Thị Đường để nhấp ngụm trà thảo mộc thanh mát, được pha chế theo công thức tiến vua ngày trước.', NULL),
            (4, 1, '14:00:00', '18:00:00', NULL, 'Sông Hương bảng lảng – Thuyền rồng đón hoàng hôn', 'Ngồi trên mạn thuyền rồng, thong dong ngắm dòng sông Hương êm đềm, thả hồn theo điệu hò xứ Huế và đón ánh chiều tà buông chậm xuống những mái ngói cổ kính.', NULL),
            (5, 1, '18:00:00', '18:30:00', NULL, 'Trả khách & Kết thúc hành trình', 'Đưa đoàn khách quay trở về an toàn tại Resort, hỗ trợ nhận hành lý và chào tạm biệt khách.', NULL),
            
            -- 2. Đồng Nội (Quảng Nam)
            (6, 2, '08:00:00', '08:30:00', NULL, 'Đón khách & Làm thủ tục', 'Chào đón khách, thực hiện điểm danh FaceID và chuẩn bị các vật dụng như nón lá, nước uống giải khát.', NULL),
            (7, 2, '08:30:00', '11:00:00', NULL, 'Hồn của đất – Làng gốm Thanh Hà', 'Bên dòng sông Thu Bồn lộng gió, bạn sẽ được ngắm nhìn các nghệ nhân thổi hồn vào đất và tự tay xoay vần, nhào nặn nên một món quà kỷ niệm của riêng mình.', NULL),
            (8, 2, '11:00:00', '13:00:00', NULL, 'Hương đồng cỏ nội – Làng rau Trà Quế', 'Hòa mình vào không gian xanh mướt của một trong những vườn rau lâu đời nhất. Trải nghiệm một ngày làm nông nhẹ nhàng, xới đất gieo mầm và thưởng thức ngụm nước hạt sen thanh mát.', NULL),
            (9, 2, '13:00:00', '15:30:00', NULL, 'Vị quê đậm đà – Ẩm thực sân vườn', 'Thưởng thức bữa trưa ấm cúng với những món ăn đặc sản xứ Quảng, được chế biến từ chính nguồn nông sản tươi ngon vừa thu hoạch tại vườn.', NULL),
            (10, 2, '15:30:00', '18:30:00', NULL, 'Hoài phố rêu phong – Tản bộ và thưởng trà chiều', 'Di chuyển về trung tâm Phố cổ Hội An, thong thả dạo bước qua những nếp nhà vàng cổ kính và dừng chân thưởng thức trà thảo mộc ngắm hoàng hôn buông trên dòng sông Hoài.', NULL),
            (11, 2, '18:30:00', '19:00:00', NULL, 'Trả khách & Kết thúc hành trình', 'Xe đưa khách trở về lại Resort, bàn giao hành lý và lắng nghe ý kiến phản hồi nhanh của khách.', NULL),
            
            -- 3. Di Sản (Ninh Bình)
            (12, 3, '08:30:00', '09:00:00', NULL, 'Đón khách & Làm thủ tục', 'Tập trung khách tại bến đón, check-in FaceID và nhắc nhở hướng dẫn an toàn cơ bản cho khách.', NULL),
            (13, 3, '09:00:00', '11:30:00', NULL, 'Sợi cói dệt nắng – Làng chiếu Kim Sơn', 'Ngắm nhìn quy trình dệt nên những chiếc chiếu cói tự nhiên đượm hương đồng nội. Bạn sẽ hiểu vì sao những sợi cỏ mềm mại qua tay người thợ lại trở thành những tác phẩm tinh xảo đến vậy.', NULL),
            (14, 3, '11:30:00', '14:00:00', NULL, 'Hương vị cố đô – Thưởng thức đặc sản', 'Ghé thăm nhà hàng cao cấp ven sông để thưởng thức bữa trưa đậm đà vị núi rừng với thịt dê truyền thống, cơm cháy giòn rụm và các sản vật địa phương.', NULL),
            (15, 3, '14:00:00', '16:30:00', NULL, 'Đốm hoa trên gấm – Làng thêu Văn Lâm', 'Thu mình vào không gian yên ắng của làng thêu cổ, lắng nghe những câu chuyện xưa và học cách đưa thoi, điểm nhụy để tự tay dệt nên những nét hoa văn truyền thống tinh tế.', NULL),
            (16, 3, '16:30:00', '19:00:00', NULL, 'Sơn thủy hữu tình – Thuyền nan lướt bóng hoàng hôn', 'Di chuyển đến đầm bảo tồn Vân Long, ngồi trên chiếc thuyền nan mộc mạc rẽ nước qua những hốc đá, ngắm nhìn đàn voọc quý hiếm và đón chiều tà buông xuống lòng thung lũng tĩnh mịch.', NULL),
            (17, 3, '19:00:00', '19:30:00', NULL, 'Trả khách & Kết thúc hành trình', 'Xe trung chuyển đón khách đưa về an toàn tại khu nghỉ dưỡng.', NULL),
            
            -- 4. Tĩnh Lặng (Tháp Mười)
            (18, 4, '07:30:00', '08:00:00', NULL, 'Đón khách & Làm thủ tục', 'Thực hiện thủ tục FaceID nhanh chóng, nhắc nhở khách chuyển thiết bị di động sang chế độ rung để bắt đầu tour thiền hành.', NULL),
            (19, 4, '08:00:00', '11:00:00', NULL, 'Đón sương sớm – Thả thuyền ngắm sen', 'Ngồi trên chiếc xuồng ba lá lướt nhẹ giữa đầm sương, ngắm nhìn những đóa sen hồng bung nở trong nắng sớm và hít hà hương thơm tinh khôi của đất trời.', NULL),
            (20, 4, '11:00:00', '13:30:00', NULL, 'Tâm an giữa đầm – Thiền hành và thưởng trà', 'Thong thả dạo bước trên những cây cầu gỗ nhỏ giữa lòng đầm, rũ bỏ những lo toan. Sau đó, tĩnh lặng thưởng thức ngụm trà sen ủ qua đêm ngọt hậu, thơm dịu.', NULL),
            (21, 4, '13:30:00', '16:00:00', NULL, 'Hương sen kết tinh – Mỹ tiệc bách hoa', 'Thưởng thức bữa trưa độc đáo với thực đơn sáng tạo trọn vẹn từ sen: từ cơm hấp lá sen dẻo thơm, súp hạt sen bổ dưỡng cho đến đĩa gỏi ngó sen thanh mát, giòn rụm.', NULL),
            (22, 4, '16:00:00', '18:00:00', NULL, 'Sông nước tịch mịch – Nghỉ ngơi tĩnh tại', 'Thư giãn tự do bên hiên nhà tre lộng gió, ngắm hoàng hôn nhuộm đỏ cánh đồng sen và lắng nghe tiếng chim bay về tổ, khép lại ngày bình yên.', NULL),
            (23, 4, '18:00:00', '18:30:00', NULL, 'Trả khách & Kết thúc hành trình', 'Đưa đoàn khách thiền hành trở về an toàn tại Resort, khép lại ngày bình yên.', NULL),
            
            -- 5. Hạ Long
            (24, 5, '07:30:00', '08:00:00', NULL, 'Đón khách & Làm thủ tục', 'Hướng dẫn khách lên du thuyền, điểm danh AI FaceID và phát túi chống nước bảo vệ thiết bị cá nhân cho khách.', NULL),
            (25, 5, '08:00:00', '09:30:00', NULL, 'Hải trình sóng vỗ – Rẽ nước thưởng ngoạn', 'Du thuyền hạng sang khởi hành đưa bạn hòa mình vào không gian bao la của vịnh Hạ Long, ngắm nhìn những đảo đá vôi mang muôn vàn hình dáng kỳ thú.', NULL),
            (26, 5, '09:30:00', '12:00:00', NULL, 'Tuyệt tác giấu kín – Khám phá động Thiên Cung', 'Dạo bước vào lòng động cùng hướng dẫn viên riêng, chiêm ngưỡng những khối thạch nhũ lấp lánh và lắng nghe những câu chuyện huyền thoại cổ xưa.', NULL),
            (27, 5, '12:00:00', '14:30:00', NULL, 'Mỹ vị giữa trùng khơi – Thưởng thức tiệc trưa', 'Dùng bữa trưa hải sản cao cấp được chế biến tinh tế bởi đầu bếp trên tàu, trong khi du thuyền chầm chậm lướt qua những vùng vịnh tĩnh lặng, hoang sơ.', NULL),
            (28, 5, '14:30:00', '16:30:00', NULL, 'Góc nhỏ tĩnh lặng – Chèo thuyền Kayak & Tắm biển', 'Tàu neo lại tại một bãi biển hoang vắng, bạn tự do chèo thuyền kayak len lỏi qua các hang luồn hoặc ngâm mình trong làn nước xanh mát.', NULL),
            (29, 5, '16:30:00', '18:00:00', NULL, 'Khúc ca lộng gió – Tiệc trà ngắm hoàng hôn', 'Thư giãn trên boong tàu đón gió biển mát lành, thưởng thức tiệc trà chiều, bánh ngọt và ngắm ánh chiều tà vàng rực buông chậm xuống vịnh biển ngàn năm.', NULL),
            (30, 5, '18:00:00', '18:30:00', NULL, 'Trả khách & Kết thúc hành trình', 'Du thuyền cập bến bãi, xe đưa khách quay trở về khu nghỉ dưỡng an toàn.', NULL),
            
            -- 6. Sapa
            (31, 6, '07:30:00', '08:00:00', NULL, 'Đón khách & Làm thủ tục', 'Đón đoàn, quét vân FaceID điểm danh hành trình leo núi dã ngoại.', NULL),
            (32, 6, '08:00:00', '11:30:00', NULL, 'Nẻo đường mây phủ – Thăm bản Cát Cát', 'Thong thả đi bộ dọc theo những lối mòn dẫn vào bản, ngắm nhìn những chiếc cối xay nước khổng lồ quay đều bên suối và trải nghiệm nghệ thuật nhuộm chàm cùng nghệ nhân bản địa.', NULL),
            (33, 6, '11:30:00', '14:00:00', NULL, 'Bếp lửa vùng cao – Ẩm thực Tây Bắc', 'Dừng chân nghỉ ngơi tại không gian nhà hàng cao cấp view toàn cảnh thung lũng, thưởng thức bữa trưa ấm cúng với những món ăn đậm đà hương vị núi rừng.', NULL),
            (34, 6, '14:00:00', '16:30:00', NULL, 'Tiếng hát của dòng suối – Thác Tiên Sa', 'Check-in dòng thác Tiên Sa bọt tung trắng xóa, tản bộ dọc bờ suối và lắng nghe những giai điệu khèn môi mộc mạc của người bản địa.', NULL),
            (35, 6, '16:30:00', '19:00:00', NULL, 'Tĩnh lặng đại ngàn – Thưởng trà chiều ngắm hoàng hôn', 'Di chuyển về một quán trà bản địa không gian mở trên đỉnh đồi, thưởng thức ngụm trà cổ thụ, ngắm nhìn thung lũng Mường Hoa chìm dần vào sương mờ hoàng hôn.', NULL),
            (36, 6, '19:00:00', '19:30:00', NULL, 'Trả khách & Kết thúc hành trình', 'Đưa hành khách quay trở lại điểm tập kết của Resort, kết thúc ngày trekking.', NULL),
            
            -- 7. Cát Tiên
            (37, 7, '07:30:00', '08:00:00', NULL, 'Đón khách & Làm thủ tục', 'Điểm danh FaceID hành khách, phát thuốc chống vắt và kem xịt côn trùng chuyên dụng cho khách.', NULL),
            (38, 7, '08:00:00', '12:00:00', NULL, 'Dưới bóng đại ngàn – Trekking Bàu Sấu', 'Lội bộ dưới những tán cây cổ thụ trăm tuổi, hít hà bầu không khí trong lành của rừng nguyên sinh để đến với vùng đầm lầy Bàu Sấu hoang sơ.', NULL),
            (39, 7, '12:00:00', '14:30:00', NULL, 'Bên bờ đầm vắng – Trưa xanh tĩnh lặng', 'Thưởng thức bữa trưa tinh tế được chuẩn bị riêng bên trạm kiểm lâm, ngắm nhìn những chú chim quý hiếm và cá sấu tự nhiên bơi lội dưới làn nước.', NULL),
            (40, 7, '14:30:00', '18:00:00', NULL, 'Khúc giao mùa rừng già – Tản bộ lượt về', 'Quay trở lại trung tâm bằng đường rừng, đón ánh nắng chiều xuyên qua những kẽ lá và nghỉ ngơi, ngâm chân thảo mộc thư giãn tại resort bìa rừng.', NULL),
            (41, 7, '18:00:00', '20:00:00', NULL, 'Hương vị đại ngàn – Bữa tối lãng mạn', 'Thưởng thức bữa tối ấm cúng tại nhà hàng ven sông của resort với các món ăn mang phong vị địa phương, lắng nghe tiếng côn trùng reo vang.', NULL),
            (42, 7, '20:00:00', '22:00:00', NULL, 'Thanh âm bóng tối – Đi xe mui trần xem thú đêm', 'Lên xe chuyên dụng mui trần len lỏi vào bìa rừng, dưới ánh đèn chuyên dụng, bạn sẽ được tận mắt ngắm nhìn cuộc sống kiếm ăn ban đêm đầy thú vị của các loài thú hoang dã.', NULL),
            (43, 7, '22:00:00', '22:30:00', NULL, 'Trả khách & Kết thúc hành trình', 'Xe trung chuyển đưa đoàn về phòng an toàn nghỉ ngơi.', NULL),
            
            -- 8. Mũi Né
            (44, 8, '04:00:00', '04:30:00', NULL, 'Đón khách & Làm thủ tục', 'Khởi hành sớm, điểm danh FaceID khách lên xe sang trọng.', NULL),
            (45, 8, '04:30:00', '05:30:00', NULL, 'Đường chạy ban mai – Hành trình ra đồi cát', 'Xe hạng sang đón bạn từ sớm khi trời còn đẫm sương để kịp di chuyển ra Đồi Cát Trắng xinh đẹp.', NULL),
            (46, 8, '05:30:00', '07:30:00', NULL, 'Nắng ấm sa mạc – Xe địa hình & Đón bình minh', 'Riêng tư trải nghiệm xe địa hình ATV lao vút qua những đụn cát nhấp nhô, ngắm mặt trời nhô lên từ phía biển và chơi trượt cát bằng máng từ đỉnh đồi.', NULL),
            (47, 8, '07:30:00', '09:30:00', NULL, 'Bức họa của gió – Đồi Cát Đỏ & Suối Tiên', 'Tiếp tục check-in Đồi Cát Đỏ lộng gió và dạo bước chân trần dưới làn nước mát rượi của dòng Suối Tiên, ngắm nhìn những bức vách đất sét màu cam rực rỡ.', NULL),
            (48, 8, '09:30:00', '12:30:00', NULL, 'Sóng vỗ rì rào – Thư giãn tại Beach Club', 'Di chuyển về một resort sát biển cao cấp, tự do tắm hồ bơi, tắm biển hoặc nằm nghỉ ngơi dưới bóng mát, thưởng thức ly nước dừa mát lạnh.', NULL),
            (49, 8, '12:30:00', '15:30:00', NULL, 'Vị mặn biển khơi – Bữa trưa hải sản thượng hạng', 'Thưởng thức bữa trưa với thực đơn hải sản tươi sống được đánh bắt trong ngày tại nhà hàng view biển sang trọng trước khi xe đưa bạn về lại điểm đón ban đầu.', NULL),
            (50, 8, '15:30:00', '16:00:00', NULL, 'Trả khách & Kết thúc hành trình', 'Xe đưa khách về lại điểm Resort ban đầu an toàn.', NULL),
            
            -- 9. Phú Quốc
            (51, 9, '08:00:00', '08:30:00', NULL, 'Đón khách & Làm thủ tục', 'Check-in FaceID hành khách tại cầu cảng, phát áo phao an toàn cho từng khách.', NULL),
            (52, 9, '08:30:00', '10:30:00', NULL, 'Lướt sóng trùng khơi – Khám phá các hòn đảo hoang', 'Cano tốc độ cao thế mới đưa bạn rời bến cảng, băng qua làn nước xanh ngắt để ghé thăm những hòn đảo nhỏ còn giữ nguyên nét hoang sơ.', NULL),
            (53, 9, '10:30:00', '13:00:00', NULL, 'Vũ điệu đại dương – Bơi lặn ngắm san hô', 'Đeo ống thở và kính lặn chuyên dụng, thỏa thích hòa mình vào làn nước trong vắt để ngắm nhìn những rạn san hô đung đưa cùng từng đàn cá nhỏ rực rỡ sắc màu.', NULL),
            (54, 9, '13:00:00', '15:30:00', NULL, 'Vị mặn của biển – Bữa trưa trên đảo hoang', 'Thưởng thức bữa trưa hải sản cao cấp tươi ngon ngay trên bãi biển cát trắng mịn, ngả lưng dưới bóng dừa mát rượi.', NULL),
            (55, 9, '15:30:00', '18:30:00', NULL, 'Đảo ngọc hoàng hôn – Tiệc cocktail chiều tà', 'Cano đưa bạn đến một hòn đảo ngắm hoàng hôn đẹp nhất. Tại đây, bạn có thể thong thả đi dạo, thưởng thức ly cocktail mát lạnh và đón khoảnh khắc mặt trời lặn nhuộm hồng cả đường chân trời trước khi trở về bến cảng lúc 18:30.', NULL),
            (56, 9, '18:30:00', '19:00:00', NULL, 'Trả khách & Kết thúc hành trình', 'Bàn giao và trả khách tại điểm đón của khu nghỉ dưỡng.', NULL),
            
            -- 10. Cần Thơ
            (57, 10, '05:00:00', '05:30:00', NULL, 'Đón khách & Làm thủ tục', 'Chào đón đoàn khách lúc bình minh, check-in FaceID và hỗ trợ khách bước xuống ghe máy an toàn.', NULL),
            (58, 10, '05:30:00', '06:00:00', NULL, 'Sương sớm trên sông – Xuống bến xuống tàu', 'Đón ngày mới khi trời còn mờ sương, xuống chiếc ghe máy truyền thống được chuẩn bị riêng để bắt đầu chuyến hành trình dọc theo dòng sông Hậu.', NULL),
            (59, 10, '06:00:00', '08:30:00', NULL, 'Âm thanh ngày mới – Chợ nổi Cái Răng', 'Len lỏi qua những chiếc ghe đơm đầy hoa trái, lắng nghe tiếng rao của người dân miền Tây và thưởng thức bữa sáng với tô hủ tiếu nóng hổi, chòng chành ngay trên mặt nước từ ghe hậu cần riêng của tour.', NULL),
            (60, 10, '08:30:00', '11:30:00', NULL, 'Trái ngọt trĩu cành – Thăm vườn cây ăn trái', 'Ghé thăm một miệt vườn cây ăn trái sum suê, thong thả dạo bước dưới bóng mát, tự tay hái và thưởng thức quả chín mọng ngọt lịm ngay tại vườn.', NULL),
            (61, 10, '11:30:00', '13:30:00', NULL, 'Trải nghiệm làm bánh dân gian', 'Đến với gian nhà cổ Nam Bộ, cùng các nghệ nhân miệt vườn học cách đổ bánh xèo, làm bánh tằm se tay và lắng nghe những câu chuyện đời sống mộc mạc.', NULL),
            (62, 10, '13:30:00', '15:30:00', NULL, 'Phong vị sông nước – Bữa trưa điền dã', 'Thưởng thức bữa trưa đậm chất miền Tây với cá lóc nướng trui, lẩu mắm đậm đà tại nhà hàng lộng gió ven sông, nghỉ ngơi tĩnh tại trên những chiếc võng trước khi kết thúc hành trình.', NULL),
            (63, 10, '15:30:00', '16:00:00', NULL, 'Trả khách & Kết thúc hành trình', 'Ghe cập bến tàu, xe đưa khách quay trở về Resort an toàn.', NULL)
            """);

        System.out.println("[ItinerarySeeder] ✅ Đã hoàn tất đồng bộ 10 Tours, bảng Giá Tour, Itineraries và 63 hoạt động chi tiết!");
    }
}
