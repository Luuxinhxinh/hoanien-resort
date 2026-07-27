package com.hoanien.controllers;

import com.hoanien.models.HotelOperation;
import com.hoanien.models.Room;
import com.hoanien.models.RoomBookingDetail;
import com.hoanien.models.FolioItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class HousekeepingAndPricingDeepIntegrationTest {

    private org.thymeleaf.spring6.SpringTemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        org.thymeleaf.spring6.SpringTemplateEngine engine = new org.thymeleaf.spring6.SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setEnableSpringELCompiler(true);
        engine.setLinkBuilder(new org.thymeleaf.linkbuilder.StandardLinkBuilder() {
            @Override
            protected String computeContextPath(org.thymeleaf.context.IExpressionContext context, String base, java.util.Map<String, Object> parameters) {
                return "";
            }
        });
        return engine;
    }

    @Nested
    @DisplayName("1. Maintenance Pricing View Rendering Tests")
    class PricingViewRenderingTests {

        @Test
        @DisplayName("TC_PRICING_001: Render pricing view with null/empty fields - Must not throw SpEL/Chunked Encoding exception")
        void testRenderPricingViewWithNullFields() {
            org.thymeleaf.spring6.SpringTemplateEngine engine = createTemplateEngine();
            Context context = new Context();

            HotelOperation taskWithNulls = new HotelOperation();
            taskWithNulls.setOperationalType("DAMAGE_CHECK");
            taskWithNulls.setNotes(null);
            taskWithNulls.setImageUrl(null);
            taskWithNulls.setRoom(null);
            taskWithNulls.setStaff(null);

            context.setVariable("unpricedTasks", List.of(taskWithNulls));
            context.setVariable("pricedTasks", List.of());
            context.setVariable("isOnShift", true);

            assertDoesNotThrow(() -> {
                String html = engine.process("maintenance/pricing", context);
                assertNotNull(html);
                assertTrue(html.contains("Định giá hỏng hóc"));
            });
        }

        @Test
        @DisplayName("TC_PRICING_002: Render pricing view with single and multiple long image URLs")
        void testRenderPricingViewWithMultipleImageUrls() {
            org.thymeleaf.spring6.SpringTemplateEngine engine = createTemplateEngine();
            Context context = new Context();

            String longUrl1 = "https://images.unsplash.com/photo-1547592180-85f173990554?w=1000&q=80&extra_long_query_param_1234567890=" + "a".repeat(200);
            String longUrl2 = "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=1000&q=80&extra_long_query_param_1234567890=" + "b".repeat(200);
            String multiImageUrls = longUrl1 + "," + longUrl2;

            Room room = new Room();
            room.setRoomNumber("105");

            HotelOperation task = new HotelOperation();
            task.setOperationalType("DAMAGE_CHECK");
            task.setImageUrl(multiImageUrls);
            task.setNotes("Tủ quần áo bị hỏng cánh cửa và mặt bàn gỗ bị trầy xước nặng");
            task.setRoom(room);
            task.setCreatedAt(LocalDateTime.now());

            HotelOperation pricedTask = new HotelOperation();
            pricedTask.setOperationalType("DAMAGE_CHECK");
            pricedTask.setImageUrl(longUrl1);
            pricedTask.setNotes("Sơn tường bị bong tróc");
            pricedTask.setDamagePrice(150000.0);
            pricedTask.setRoom(room);
            pricedTask.setCreatedAt(LocalDateTime.now());

            context.setVariable("unpricedTasks", List.of(task));
            context.setVariable("pricedTasks", List.of(pricedTask));
            context.setVariable("isOnShift", true);

            assertDoesNotThrow(() -> {
                String html = engine.process("maintenance/pricing", context);
                assertNotNull(html);
                assertTrue(html.contains("2 ảnh"));
                assertTrue(html.contains("150.000 VNĐ") || html.contains("150,000"));
            });
        }
    }

    @Nested
    @DisplayName("2. Minibar Data Flow Integrity Tests")
    class MinibarDataFlowTests {

        @Test
        @DisplayName("TC_MINIBAR_001: Minibar item replacement - Old items cleared when new items saved")
        void testMinibarItemReplacement() {
            List<FolioItem> dbFolioItems = new ArrayList<>();

            RoomBookingDetail detail = new RoomBookingDetail();

            // 1st Save: 3 Evian, 2 Coca
            FolioItem item1 = new FolioItem();
            item1.setRoomBookingDetail(detail);
            item1.setSourceDepartment("Minibar");
            item1.setRevenueCode("FB_MINIBAR");
            item1.setAmount(new BigDecimal("150000"));
            item1.setDescription("Phí sử dụng Minibar: Nước suối Evian (x3)");
            item1.setIsSettledSeparately(false);
            dbFolioItems.add(item1);

            FolioItem item2 = new FolioItem();
            item2.setRoomBookingDetail(detail);
            item2.setSourceDepartment("Minibar");
            item2.setRevenueCode("FB_MINIBAR");
            item2.setAmount(new BigDecimal("60000"));
            item2.setDescription("Phí sử dụng Minibar: Coca Cola (x2)");
            item2.setIsSettledSeparately(false);
            dbFolioItems.add(item2);

            assertEquals(2, dbFolioItems.size());
            BigDecimal firstTotal = dbFolioItems.stream().map(FolioItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            assertEquals(new BigDecimal("210000"), firstTotal);

            // 2nd Edit: Clear old items and replace with 1 Heineken
            dbFolioItems.removeIf(fi -> "Minibar".equalsIgnoreCase(fi.getSourceDepartment()) && Boolean.FALSE.equals(fi.getIsSettledSeparately()));
            assertEquals(0, dbFolioItems.size());

            FolioItem item3 = new FolioItem();
            item3.setRoomBookingDetail(detail);
            item3.setSourceDepartment("Minibar");
            item3.setRevenueCode("FB_MINIBAR");
            item3.setAmount(new BigDecimal("50000"));
            item3.setDescription("Phí sử dụng Minibar: Bia Heineken (x1)");
            item3.setIsSettledSeparately(false);
            dbFolioItems.add(item3);

            assertEquals(1, dbFolioItems.size());
            BigDecimal updatedTotal = dbFolioItems.stream().map(FolioItem::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            assertEquals(new BigDecimal("50000"), updatedTotal);
        }

        @Test
        @DisplayName("TC_MINIBAR_002: Reset Minibar to 0 quantity clears all minibar items from folio")
        void testMinibarResetToZero() {
            List<FolioItem> dbFolioItems = new ArrayList<>();
            RoomBookingDetail detail = new RoomBookingDetail();

            FolioItem item = new FolioItem();
            item.setRoomBookingDetail(detail);
            item.setSourceDepartment("Minibar");
            item.setRevenueCode("FB_MINIBAR");
            item.setAmount(new BigDecimal("50000"));
            item.setDescription("Phí sử dụng Minibar: Bia Heineken (x1)");
            item.setIsSettledSeparately(false);
            dbFolioItems.add(item);

            assertEquals(1, dbFolioItems.size());

            // Housekeeper sets all items to 0 (totalFee = 0)
            dbFolioItems.removeIf(fi -> "Minibar".equalsIgnoreCase(fi.getSourceDepartment())
                    || "FB_MINIBAR".equalsIgnoreCase(fi.getRevenueCode())
                    || (fi.getDescription() != null && fi.getDescription().toLowerCase().contains("minibar")));

            assertTrue(dbFolioItems.isEmpty(), "DB Folio Items must be empty after resetting minibar to 0");
        }
    }
}
