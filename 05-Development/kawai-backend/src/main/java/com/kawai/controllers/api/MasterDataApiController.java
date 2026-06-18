package com.kawai.controllers.api;

import com.kawai.models.*;
import com.kawai.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin/api/v1")
@RequiredArgsConstructor
public class MasterDataApiController {

    private final RoomRepository roomRepository;
    private final RoomCategoryRepository roomCategoryRepository;
    private final FoodItemRepository foodItemRepository;
    private final TourRepository tourRepository;
    private final PromotionRepository promotionRepository;
    private final BookingRepository bookingRepository;

    @PostMapping("/{entityType}")
    public ResponseEntity<?> createEntity(@PathVariable String entityType, @RequestBody Map<String, Object> payload) {
        System.out.println("========== CREATE ENTITY API HIT! Type: " + entityType + " ==========");
        System.out.println("Payload: " + payload);

        try {
            switch (entityType) {
                case "room-categories":
                    RoomCategory rc = new RoomCategory();
                    rc.setCategoryName((String) payload.get("name"));
                    String priceStr = (String) payload.get("price");
                    if (priceStr != null) {
                        rc.setBasePrice(new java.math.BigDecimal(priceStr.replaceAll("[^\\d.]", "")));
                    } else {
                        rc.setBasePrice(java.math.BigDecimal.ZERO);
                    }
                    if (payload.get("rooms") != null) {
                        rc.setCapacity(Integer.parseInt(payload.get("rooms").toString()));
                    } else {
                        rc.setCapacity(2);
                    }
                    rc.setIsActive("Active".equals(payload.get("status")));
                    roomCategoryRepository.save(rc);
                    break;

                case "rooms":
                    Room room = new Room();
                    String rn = (String) payload.get("name");
                    if (rn != null) {
                        if (rn.startsWith("Phòng "))
                            rn = rn.substring(6);
                        rn = rn.trim();
                        if (!rn.matches("^[1-9](0[1-9]|1[0-7])$")) {
                            return ResponseEntity.badRequest().body(Map.of("error",
                                    "Số phòng không hợp lệ! Tầng phải từ 1-9 và số phòng mỗi tầng từ 01-17 (VD: 101, 917)."));
                        }
                        room.setRoomNumber(rn);
                    }
                    String catName = (String) payload.get("category");
                    if (catName != null) {
                        roomCategoryRepository.findAll().stream()
                                .filter(c -> c.getCategoryName().equals(catName))
                                .findFirst().ifPresent(room::setCategory);
                    }
                    room.setRoomStatus((String) payload.get("status"));
                    roomRepository.save(room);
                    break;

                case "menu-items":
                    MenuItem item = new MenuItem();
                    item.setItemName((String) payload.get("name"));
                    item.setCategory((String) payload.get("category"));
                    String pStr = (String) payload.get("price");
                    if (pStr != null) {
                        item.setPrice(new java.math.BigDecimal(pStr.replaceAll("[^\\d.]", "")));
                    } else {
                        item.setPrice(java.math.BigDecimal.ZERO);
                    }
                    item.setIsAvailable("Available".equals(payload.get("status")));
                    foodItemRepository.save(item);
                    break;

                case "tours":
                    Tour tour = new Tour();
                    tour.setTourName((String) payload.get("name"));
                    tour.setTourType((String) payload.get("category"));
                    String tpStr = (String) payload.get("price");
                    if (tpStr != null) {
                        tour.setBasePrice(new java.math.BigDecimal(tpStr.replaceAll("[^\\d.]", "")));
                    } else {
                        tour.setBasePrice(java.math.BigDecimal.ZERO);
                    }
                    if (payload.get("duration") != null) {
                        tour.setDuration(payload.get("duration").toString());
                    }
                    tour.setIsActive("Active".equals(payload.get("status")));
                    tourRepository.save(tour);
                    break;

                case "promotions":
                    Promotion promo = new Promotion();
                    promo.setPromoCode((String) payload.get("code"));
                    promo.setDescription((String) payload.get("description"));
                    String dStr = (String) payload.get("discount");
                    if (dStr != null) {
                        promo.setDiscountValue(new java.math.BigDecimal(dStr.replaceAll("[^\\d.]", "")));
                    } else {
                        promo.setDiscountValue(java.math.BigDecimal.ZERO);
                    }
                    promo.setDiscountType("PERCENTAGE");
                    promo.setValidFrom(java.time.LocalDateTime.now());
                    promo.setValidTo(java.time.LocalDate.now().plusYears(1));
                    promo.setMaxUses(100);
                    promo.setIsActive("Active".equals(payload.get("status")));
                    promotionRepository.save(promo);
                    break;

                default:
                    // Other entities will be mocked for now
                    break;
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Created successfully", "data", payload));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{entityType}/{id}")
    public ResponseEntity<?> updateEntity(@PathVariable String entityType, @PathVariable String id,
            @RequestBody Map<String, Object> payload) {
        System.out.println("========== UPDATE ENTITY API HIT! Type: " + entityType + " | ID: " + id + " ==========");
        System.out.println("Payload: " + payload);

        try {
            String rawId = id;
            if (id.startsWith("RC-") || id.startsWith("RM-") || id.startsWith("MI-") || id.startsWith("T-")
                    || id.startsWith("PR-")) {
                rawId = id.substring(id.indexOf("-") + 1);
            }
            Long entityId = Long.parseLong(rawId);

            switch (entityType) {
                case "room-categories":
                    RoomCategory rc = roomCategoryRepository.findById(entityId).orElse(null);
                    if (rc != null) {
                        rc.setCategoryName((String) payload.get("name"));
                        String priceStr = (String) payload.get("price");
                        if (priceStr != null) {
                            String c = priceStr.replaceAll("[^\\d]", "");
                            if (!c.isEmpty())
                                rc.setBasePrice(new java.math.BigDecimal(c));
                        }
                        if (payload.get("rooms") != null)
                            rc.setCapacity(Integer.parseInt(payload.get("rooms").toString()));
                        if (payload.get("baseAdults") != null && !payload.get("baseAdults").toString().isEmpty())
                            rc.setBaseAdults(Integer.parseInt(payload.get("baseAdults").toString()));
                        if (payload.get("baseChildren") != null && !payload.get("baseChildren").toString().isEmpty())
                            rc.setBaseChildren(Integer.parseInt(payload.get("baseChildren").toString()));
                        if (payload.get("maxAdults") != null && !payload.get("maxAdults").toString().isEmpty())
                            rc.setMaxAdults(Integer.parseInt(payload.get("maxAdults").toString()));
                        if (payload.get("maxChildren") != null && !payload.get("maxChildren").toString().isEmpty())
                            rc.setMaxChildren(Integer.parseInt(payload.get("maxChildren").toString()));
                        if (payload.get("extraAdultSurcharge") != null
                                && !payload.get("extraAdultSurcharge").toString().isEmpty())
                            rc.setExtraAdultSurcharge(new java.math.BigDecimal(
                                    payload.get("extraAdultSurcharge").toString().replaceAll("[^\\d.]", "")));
                        if (payload.get("extraChildSurcharge") != null
                                && !payload.get("extraChildSurcharge").toString().isEmpty())
                            rc.setExtraChildSurcharge(new java.math.BigDecimal(
                                    payload.get("extraChildSurcharge").toString().replaceAll("[^\\d.]", "")));
                        if (payload.get("description") != null)
                            rc.setDescription((String) payload.get("description"));
                        if (payload.get("coverImgUrl") != null)
                            rc.setCoverImgUrl((String) payload.get("coverImgUrl"));

                        if (rc.getBaseAdults() > rc.getMaxAdults()) {
                            return ResponseEntity.badRequest()
                                    .body(Map.of("error", "Số người lớn tiêu chuẩn (" + rc.getBaseAdults()
                                            + ") không được vượt quá tối đa (" + rc.getMaxAdults() + ")."));
                        }
                        if (rc.getBaseChildren() > rc.getMaxChildren()) {
                            return ResponseEntity.badRequest()
                                    .body(Map.of("error", "Số trẻ em tiêu chuẩn (" + rc.getBaseChildren()
                                            + ") không được vượt quá tối đa (" + rc.getMaxChildren() + ")."));
                        }

                        rc.setIsActive("Active".equals(payload.get("status")));
                        roomCategoryRepository.save(rc);
                    }
                    break;

                case "rooms":
                    Room roomUpdate = roomRepository.findById(entityId).orElse(null);
                    if (roomUpdate != null) {
                        String rnUpd = (String) payload.get("name");
                        if (rnUpd != null) {
                            if (rnUpd.startsWith("Phòng "))
                                rnUpd = rnUpd.substring(6);
                            rnUpd = rnUpd.trim();
                            if (!rnUpd.matches("^[1-9](0[1-9]|1[0-7])$")) {
                                return ResponseEntity.badRequest().body(Map.of("error",
                                        "Số phòng không hợp lệ! Tầng phải từ 1-9 và số phòng mỗi tầng từ 01-17 (VD: 101, 917)."));
                            }
                            roomUpdate.setRoomNumber(rnUpd);
                        }
                        String catNameUpd = (String) payload.get("category");
                        if (catNameUpd != null) {
                            roomCategoryRepository.findAll().stream()
                                    .filter(c -> c.getCategoryName().equals(catNameUpd))
                                    .findFirst().ifPresent(roomUpdate::setCategory);
                        }
                        roomUpdate.setRoomStatus((String) payload.get("status"));
                        roomRepository.save(roomUpdate);
                    }
                    break;

                case "menu-items":
                    MenuItem item = foodItemRepository.findById(entityId).orElse(null);
                    if (item != null) {
                        item.setItemName((String) payload.get("name"));
                        item.setCategory((String) payload.get("category"));
                        String pStr = (String) payload.get("price");
                        if (pStr != null) {
                            String c = pStr.replaceAll("[^\\d]", "");
                            if (!c.isEmpty())
                                item.setPrice(new java.math.BigDecimal(c));
                        }
                        if (payload.get("description") != null)
                            item.setDescription((String) payload.get("description"));
                        if (payload.get("imageUrl") != null)
                            item.setImageUrl((String) payload.get("imageUrl"));
                        item.setIsAvailable("Available".equals(payload.get("status")));
                        foodItemRepository.save(item);
                    }
                    break;

                case "tours":
                    Tour tour = tourRepository.findById(entityId).orElse(null);
                    if (tour != null) {
                        tour.setTourName((String) payload.get("name"));
                        tour.setTourType((String) payload.get("category"));
                        String tpStr = (String) payload.get("price");
                        if (tpStr != null) {
                            String c = tpStr.replaceAll("[^\\d]", "");
                            if (!c.isEmpty())
                                tour.setBasePrice(new java.math.BigDecimal(c));
                        }
                        if (payload.get("duration") != null) {
                            tour.setDuration(payload.get("duration").toString());
                        }
                        if (payload.get("maxCapacity") != null && !payload.get("maxCapacity").toString().isEmpty())
                            tour.setMaxCapacity(Integer.parseInt(payload.get("maxCapacity").toString()));
                        if (payload.get("shortQuote") != null)
                            tour.setShortQuote((String) payload.get("shortQuote"));
                        if (payload.get("description") != null)
                            tour.setDescription((String) payload.get("description"));
                        tour.setIsActive("Active".equals(payload.get("status")));
                        tourRepository.save(tour);
                    }
                    break;
                case "promotions":
                    Promotion promo = promotionRepository.findById(entityId).orElse(null);
                    if (promo != null) {
                        promo.setPromoCode((String) payload.get("code"));
                        if (payload.get("description") != null)
                            promo.setDescription((String) payload.get("description"));
                        if (payload.get("comboConfig") != null)
                            promo.setComboConfig((String) payload.get("comboConfig"));
                        if (payload.get("discountType") != null)
                            promo.setDiscountType((String) payload.get("discountType"));
                        String dStr = (String) payload.get("value");
                        if (dStr != null) {
                            String c = dStr.replaceAll("[^\\d]", "");
                            if (!c.isEmpty())
                                promo.setDiscountValue(new java.math.BigDecimal(c));
                        }
                        if (payload.get("maxUses") != null && !payload.get("maxUses").toString().isEmpty())
                            promo.setMaxUses(Integer.parseInt(payload.get("maxUses").toString()));
                        if (payload.get("validFrom") != null && !payload.get("validFrom").toString().isEmpty())
                            promo.setValidFrom(java.time.LocalDateTime.parse(payload.get("validFrom").toString()));
                        if (payload.get("validTo") != null && !payload.get("validTo").toString().isEmpty())
                            promo.setValidTo(java.time.LocalDate.parse(payload.get("validTo").toString()));

                        if (promo.getValidFrom() != null && promo.getValidTo() != null) {
                            if (promo.getValidFrom().toLocalDate().isAfter(promo.getValidTo())) {
                                return ResponseEntity.badRequest()
                                        .body(Map.of("error", "Ngày bắt đầu không được lớn hơn ngày kết thúc!"));
                            }
                        }

                        promo.setIsActive("Active".equals(payload.get("status")));
                        promotionRepository.save(promo);
                    }
                    break;
            }

            return ResponseEntity.ok(Map.of("message", "Updated successfully", "data", payload));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{entityType}/{id}")
    public ResponseEntity<?> deleteEntity(@PathVariable String entityType, @PathVariable String id) {
        System.out.println("========== DELETE ENTITY API HIT! Type: " + entityType + " | ID: " + id + " ==========");

        try {
            String rawId = id;
            if (id.startsWith("RC-") || id.startsWith("RM-") || id.startsWith("MI-") || id.startsWith("T-")
                    || id.startsWith("PR-")) {
                rawId = id.substring(id.indexOf("-") + 1);
            }
            Long entityId = Long.parseLong(rawId);

            switch (entityType) {
                case "room-categories":
                    RoomCategory rc = roomCategoryRepository.findById(entityId).orElse(null);
                    if (rc != null) {
                        rc.setIsActive(false);
                        roomCategoryRepository.save(rc);
                    }
                    break;
                case "rooms":
                    Room room = roomRepository.findById(entityId).orElse(null);
                    if (room != null) {
                        room.setRoomStatus("OutOfOrder");
                        roomRepository.save(room);
                    }
                    break;
                case "menu-items":
                    MenuItem item = foodItemRepository.findById(entityId).orElse(null);
                    if (item != null) {
                        item.setIsAvailable(false);
                        foodItemRepository.save(item);
                    }
                    break;
                case "tours":
                    Tour tour = tourRepository.findById(entityId).orElse(null);
                    if (tour != null) {
                        tour.setIsActive(false);
                        tourRepository.save(tour);
                    }
                    break;
                case "promotions":
                    Promotion promo = promotionRepository.findById(entityId).orElse(null);
                    if (promo != null) {
                        promo.setIsActive(false);
                        promotionRepository.save(promo);
                    }
                    break;
            }
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
