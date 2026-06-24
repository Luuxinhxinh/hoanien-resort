package com.kawai.services.impl;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.MasterDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MasterDataServiceImpl implements MasterDataService {

    private final RoomRepository roomRepository;
    private final RoomCategoryRepository roomCategoryRepository;
    private final FoodItemRepository foodItemRepository;
    private final TourRepository tourRepository;
    private final PromotionRepository promotionRepository;
    private final BookingRepository bookingRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public Map<String, Object> createEntity(String entityType, Map<String, Object> payload) throws Exception {
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
                rc.setIsActive(payload.get("status") == null || "Active".equals(payload.get("status")));
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
                        throw new IllegalArgumentException("Số phòng không hợp lệ! Tầng phải từ 1-9 và số phòng mỗi tầng từ 01-17 (VD: 101, 917).");
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
                if (payload.get("description") != null)
                    item.setDescription((String) payload.get("description"));
                if (payload.get("imageUrl") != null)
                    item.setImageUrl((String) payload.get("imageUrl"));
                item.setIsAvailable(payload.get("status") == null || "Available".equals(payload.get("status")));
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
                if (payload.get("maxCapacity") != null && !payload.get("maxCapacity").toString().isEmpty())
                    tour.setMaxCapacity(Integer.parseInt(payload.get("maxCapacity").toString()));
                if (payload.get("shortQuote") != null)
                    tour.setShortQuote((String) payload.get("shortQuote"));
                if (payload.get("description") != null)
                    tour.setDescription((String) payload.get("description"));
                if (payload.get("imageUrl") != null)
                    tour.setImageUrl((String) payload.get("imageUrl"));
                tour.setIsActive(payload.get("status") == null || "Active".equals(payload.get("status")));
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
                
                if (payload.get("maxDiscountValueVnd") != null && !payload.get("maxDiscountValueVnd").toString().isEmpty()) {
                    promo.setMaxDiscountValueVnd(new java.math.BigDecimal(payload.get("maxDiscountValueVnd").toString().replaceAll("[^\\d.]", "")));
                }
                if (payload.get("maxUsesPerCustomer") != null && !payload.get("maxUsesPerCustomer").toString().isEmpty()) {
                    promo.setMaxUsesPerCustomer(Integer.parseInt(payload.get("maxUsesPerCustomer").toString()));
                }
                if (payload.get("managerApprovalThresholdPct") != null && !payload.get("managerApprovalThresholdPct").toString().isEmpty()) {
                    promo.setManagerApprovalThresholdPct(Integer.parseInt(payload.get("managerApprovalThresholdPct").toString()));
                }

                promo.setIsActive(payload.get("status") == null || "Active".equals(payload.get("status")));
                promotionRepository.save(promo);
                break;
            case "roles":
                Role newRole = new Role();
                newRole.setRoleName((String) payload.get("name"));
                if (payload.get("permissions") != null) {
                    newRole.setPermissions(payload.get("permissions").toString());
                }
                roleRepository.save(newRole);
                break;
        }
        return payload;
    }

    @Override
    @Transactional
    public Map<String, Object> updateEntity(String entityType, String id, Map<String, Object> payload) throws Exception {
        String rawId = id;
        if (id.startsWith("RC-") || id.startsWith("RM-") || id.startsWith("MI-") || id.startsWith("T-")
                || id.startsWith("PR-") || id.startsWith("RL-")) {
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
                    if (payload.get("extraAdultSurcharge") != null && !payload.get("extraAdultSurcharge").toString().isEmpty())
                        rc.setExtraAdultSurcharge(new java.math.BigDecimal(payload.get("extraAdultSurcharge").toString().replaceAll("[^\\d.]", "")));
                    if (payload.get("extraChildSurcharge") != null && !payload.get("extraChildSurcharge").toString().isEmpty())
                        rc.setExtraChildSurcharge(new java.math.BigDecimal(payload.get("extraChildSurcharge").toString().replaceAll("[^\\d.]", "")));
                    if (payload.get("description") != null)
                        rc.setDescription((String) payload.get("description"));
                    if (payload.get("coverImgUrl") != null)
                        rc.setCoverImgUrl((String) payload.get("coverImgUrl"));

                    if (rc.getBaseAdults() > rc.getMaxAdults()) {
                        throw new IllegalArgumentException("Số người lớn tiêu chuẩn (" + rc.getBaseAdults()
                                        + ") không được vượt quá tối đa (" + rc.getMaxAdults() + ").");
                    }
                    if (rc.getBaseChildren() > rc.getMaxChildren()) {
                        throw new IllegalArgumentException("Số trẻ em tiêu chuẩn (" + rc.getBaseChildren()
                                        + ") không được vượt quá tối đa (" + rc.getMaxChildren() + ").");
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
                            throw new IllegalArgumentException("Số phòng không hợp lệ! Tầng phải từ 1-9 và số phòng mỗi tầng từ 01-17 (VD: 101, 917).");
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
                    if (payload.get("imageUrl") != null)
                        tour.setImageUrl((String) payload.get("imageUrl"));
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

                    if (payload.get("maxDiscountValueVnd") != null && !payload.get("maxDiscountValueVnd").toString().isEmpty()) {
                        promo.setMaxDiscountValueVnd(new java.math.BigDecimal(payload.get("maxDiscountValueVnd").toString().replaceAll("[^\\d.]", "")));
                    } else {
                        promo.setMaxDiscountValueVnd(null);
                    }
                    if (payload.get("maxUsesPerCustomer") != null && !payload.get("maxUsesPerCustomer").toString().isEmpty()) {
                        promo.setMaxUsesPerCustomer(Integer.parseInt(payload.get("maxUsesPerCustomer").toString()));
                    } else {
                        promo.setMaxUsesPerCustomer(null);
                    }
                    if (payload.get("managerApprovalThresholdPct") != null && !payload.get("managerApprovalThresholdPct").toString().isEmpty()) {
                        promo.setManagerApprovalThresholdPct(Integer.parseInt(payload.get("managerApprovalThresholdPct").toString()));
                    } else {
                        promo.setManagerApprovalThresholdPct(null);
                    }

                    if (promo.getValidFrom() != null && promo.getValidTo() != null) {
                        if (promo.getValidFrom().toLocalDate().isAfter(promo.getValidTo())) {
                            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc!");
                        }
                    }

                    promo.setIsActive("Active".equals(payload.get("status")));
                    promotionRepository.save(promo);
                }
                break;
            case "roles":
                Role role = roleRepository.findById(entityId).orElse(null);
                if (role != null) {
                    if ("Admin".equalsIgnoreCase(role.getRoleName())) {
                        throw new IllegalArgumentException("Không thể sửa vai trò Admin");
                    }
                    role.setRoleName((String) payload.get("name"));
                    if (payload.get("permissions") != null) {
                        role.setPermissions(payload.get("permissions").toString());
                    }
                    roleRepository.save(role);
                }
                break;
        }
        return payload;
    }

    @Override
    @Transactional
    public void deleteEntity(String entityType, String id) throws Exception {
        String rawId = id;
        if (id.startsWith("RC-") || id.startsWith("RM-") || id.startsWith("MI-") || id.startsWith("T-") || id.startsWith("PR-") || id.startsWith("RL-")) {
            rawId = id.substring(id.indexOf("-") + 1);
        }
        Long entityId = Long.parseLong(rawId);

        switch (entityType) {
            case "room-categories":
                roomCategoryRepository.findById(entityId).ifPresent(rc -> {
                    rc.setIsActive(false);
                    roomCategoryRepository.save(rc);
                });
                break;
            case "rooms":
                roomRepository.findById(entityId).ifPresent(room -> {
                    room.setRoomStatus("OutOfOrder");
                    roomRepository.save(room);
                });
                break;
            case "menu-items":
                foodItemRepository.findById(entityId).ifPresent(item -> {
                    item.setIsAvailable(false);
                    foodItemRepository.save(item);
                });
                break;
            case "tours":
                tourRepository.findById(entityId).ifPresent(tour -> {
                    tour.setIsActive(false);
                    tourRepository.save(tour);
                });
                break;
            case "promotions":
                promotionRepository.findById(entityId).ifPresent(promo -> {
                    promo.setIsActive(false);
                    promotionRepository.save(promo);
                });
                break;
            case "roles":
                roleRepository.findById(entityId).ifPresent(r -> {
                    if ("Admin".equalsIgnoreCase(r.getRoleName())) {
                        throw new IllegalArgumentException("Không thể xóa vai trò Admin");
                    }
                    roleRepository.delete(r);
                });
                break;
        }
    }

    @Override
    @Transactional
    public void toggleEntityStatus(String entityType, String id, Boolean newStatus) throws Exception {
        String rawId = id;
        if (id.contains("-")) {
            rawId = id.substring(id.indexOf("-") + 1);
        }
        Long entityId = null;
        try {
            entityId = Long.parseLong(rawId);
        } catch (NumberFormatException e) {
            // ignore
        }

        switch (entityType) {
            case "room-categories":
                if (entityId != null) {
                    roomCategoryRepository.findById(entityId).ifPresent(rc -> {
                        rc.setIsActive(newStatus);
                        roomCategoryRepository.save(rc);
                        // Cascade
                        roomRepository.findAll().stream()
                            .filter(r -> r.getCategory() != null && r.getCategory().getId().equals(rc.getId()))
                            .forEach(r -> {
                                r.setRoomStatus(newStatus ? "Vacant_Clean" : "OutOfOrder");
                                roomRepository.save(r);
                            });
                    });
                }
                break;
            case "rooms":
                if (entityId != null) {
                    roomRepository.findById(entityId).ifPresent(room -> {
                        room.setRoomStatus(newStatus ? "Occupied" : "OutOfOrder");
                        roomRepository.save(room);
                    });
                }
                break;
            case "menu-items":
                if (entityId != null) {
                    foodItemRepository.findById(entityId).ifPresent(item -> {
                        item.setIsAvailable(newStatus);
                        foodItemRepository.save(item);
                    });
                }
                break;
            case "menu-categories":
                if (entityId != null) {
                    int idx = entityId.intValue() - 1;
                    java.util.List<String> cats = foodItemRepository.findDistinctCategories();
                    if (cats != null && idx >= 0 && idx < cats.size()) {
                        String catName = cats.get(idx);
                        foodItemRepository.findAll().stream()
                            .filter(f -> catName.equals(f.getCategory()))
                            .forEach(f -> {
                                f.setIsAvailable(newStatus);
                                foodItemRepository.save(f);
                            });
                    }
                }
                break;
            case "tours":
                if (entityId != null) {
                    tourRepository.findById(entityId).ifPresent(tour -> {
                        tour.setIsActive(newStatus);
                        tourRepository.save(tour);
                    });
                }
                break;
            case "tour-categories":
                if (entityId != null) {
                    int idx = entityId.intValue() - 1;
                    java.util.List<String> cats = tourRepository.findDistinctCategories();
                    if (cats != null && idx >= 0 && idx < cats.size()) {
                        String catName = cats.get(idx);
                        tourRepository.findAll().stream()
                            .filter(t -> catName.equals(t.getTourType()))
                            .forEach(t -> {
                                t.setIsActive(newStatus);
                                tourRepository.save(t);
                            });
                    }
                }
                break;
            case "promotions":
                if (entityId != null) {
                    promotionRepository.findById(entityId).ifPresent(promo -> {
                        promo.setIsActive(newStatus);
                        promotionRepository.save(promo);
                    });
                }
                break;
            case "roles":
                // Roles don't have isActive flag currently, so just ignore or throw error
                break;
        }
    }
}
