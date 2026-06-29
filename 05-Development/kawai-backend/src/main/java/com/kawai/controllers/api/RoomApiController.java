package com.kawai.controllers.api;

import com.kawai.dto.RoomInfoDto;
import com.kawai.models.Room;
import com.kawai.models.RoomBookingDetail;
import com.kawai.repositories.RoomRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kawai.dto.RoomSearchRequestDTO;
import com.kawai.dto.RoomSearchResponseDTO;
import com.kawai.models.Customer;
import com.kawai.models.FolioItem;
import com.kawai.repositories.FolioItemRepository;
import com.kawai.services.interfaces.RoomService;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomApiController {

    private final RoomRepository roomRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final RoomService roomService;
    private final FolioItemRepository folioItemRepository;

    @Autowired
    private com.kawai.repositories.AccountRepository accountRepository;

    @Autowired
    public RoomApiController(RoomRepository roomRepository,
            RoomBookingDetailRepository roomBookingDetailRepository,
            RoomService roomService,
            FolioItemRepository folioItemRepository) {
        this.roomRepository = roomRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.roomService = roomService;
        this.folioItemRepository = folioItemRepository;
    }

    @GetMapping("/{roomNumber}/info")
    public ResponseEntity<RoomInfoDto> getRoomInfo(@PathVariable String roomNumber) {
        Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);

        if (roomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Room room = roomOpt.get();
        boolean isOccupied = "OCCUPIED".equalsIgnoreCase(room.getRoomStatus());

        RoomInfoDto dto = RoomInfoDto.builder()
                .roomNumber(room.getRoomNumber())
                .status(room.getRoomStatus())
                .occupied(isOccupied)
                .build();

        if (isOccupied && room.getCurrentBookingDetailId() != null) {
            Optional<RoomBookingDetail> detailOpt = roomBookingDetailRepository
                    .findById(room.getCurrentBookingDetailId());
            if (detailOpt.isPresent()) {
                RoomBookingDetail detail = detailOpt.get();
                if (detail.getRoomBooking() != null && detail.getRoomBooking().getCustomer() != null) {
                    dto.setGuestName(detail.getRoomBooking().getCustomer().getFullName());
                    dto.setCustomerId(detail.getRoomBooking().getCustomer().getId());
                }
                
                // Calculate dynamic credit limit remaining
                BigDecimal limit = detail.getSubCreditLimit() != null ? detail.getSubCreditLimit() : (detail.getRoomBooking() != null && detail.getRoomBooking().getCreditLimit() != null ? detail.getRoomBooking().getCreditLimit() : BigDecimal.ZERO);
                BigDecimal used = folioItemRepository.findByRoomBookingDetailId(detail.getId()).stream()
                        .filter(f -> !Boolean.TRUE.equals(f.getIsSettledSeparately()))
                        .map(FolioItem::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                dto.setLimitRemaining(limit.subtract(used));
            }
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RoomSearchResponseDTO>> searchRooms(
            @RequestParam("checkIn") String checkInStr,
            @RequestParam("checkOut") String checkOutStr,
            @RequestParam(value = "categoryName", required = false) String categoryName,
            @RequestParam(value = "capacity", required = false) Integer capacity,
            @RequestParam(value = "minRooms", required = false) Integer minRooms) {

        LocalDate checkIn = LocalDate.parse(checkInStr);
        LocalDate checkOut = LocalDate.parse(checkOutStr);

        RoomSearchRequestDTO request = new RoomSearchRequestDTO(checkIn, checkOut);
        request.setCategoryName(categoryName);
        request.setMinCapacity(capacity);
        request.setMinRooms(minRooms);

        List<RoomSearchResponseDTO> availableRooms = roomService.searchAvailableRooms(request);
        return ResponseEntity.ok(availableRooms);
    }

    @GetMapping("/by-cccd")
    public ResponseEntity<RoomInfoDto> getRoomInfoByCccd(@RequestParam("cccd") String cccd) {
        if (cccd == null || cccd.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        String encryptedCccd = cccd.trim();
        try {
            encryptedCccd = com.kawai.utils.EncryptionUtils.encrypt(cccd.trim());
        } catch (Exception e) {
            // Keep original if encryption fails
        }
        
        List<Room> occupiedRooms = roomRepository.findOccupiedRoomsByCustomerCccd(encryptedCccd);
        if (occupiedRooms.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Room room = occupiedRooms.get(0);
        RoomInfoDto dto = RoomInfoDto.builder()
                .roomNumber(room.getRoomNumber())
                .status(room.getRoomStatus())
                .occupied(true)
                .build();

        if (room.getCurrentBookingDetailId() != null) {
            Optional<RoomBookingDetail> detailOpt = roomBookingDetailRepository
                    .findById(room.getCurrentBookingDetailId());
            if (detailOpt.isPresent()) {
                RoomBookingDetail detail = detailOpt.get();
                if (detail.getCustomer() != null) {
                    dto.setGuestName(detail.getCustomer().getFullName());
                    dto.setCustomerId(detail.getCustomer().getId());
                } else if (detail.getRoomBooking() != null && detail.getRoomBooking().getCustomer() != null) {
                    dto.setGuestName(detail.getRoomBooking().getCustomer().getFullName());
                    dto.setCustomerId(detail.getRoomBooking().getCustomer().getId());
                }
            }
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/my-active")
    public ResponseEntity<?> getMyActiveRooms(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("message", "Vui lòng đăng nhập"));
        }
        
        String username = principal.getName();
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = 
                (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
            username = oauthToken.getPrincipal().getAttribute("email");
        }
        
        java.util.Optional<com.kawai.models.Account> accountOpt = accountRepository.findByUsername(username);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of("message", "Không tìm thấy tài khoản"));
        }
        
        List<RoomBookingDetail> activeDetails = roomBookingDetailRepository.findActiveDetailsByUserId(accountOpt.get().getId());
        List<java.util.Map<String, Object>> responseList = new java.util.ArrayList<>();
        
        for (RoomBookingDetail rbd : activeDetails) {
            if (rbd == null) continue;
            java.util.Map<String, Object> rMap = new java.util.HashMap<>();
            Room r = rbd.getRoom();
            if (r != null) {
                rMap.put("id", r.getRoomNumber());
                rMap.put("roomNumber", r.getRoomNumber());
                rMap.put("roomStatus", r.getRoomStatus());
                rMap.put("roomType", r.getCategory() != null ? r.getCategory().getCategoryName() : "Standard");
            } else {
                rMap.put("id", "Virtual_" + rbd.getId());
                rMap.put("roomNumber", "Đang chờ nhận phòng (" + rbd.getCategory().getCategoryName() + ")");
                rMap.put("roomStatus", "Confirmed");
                rMap.put("roomType", rbd.getCategory().getCategoryName());
            }
            responseList.add(rMap);
        }
        
        return ResponseEntity.ok(responseList);
    }

    /**
     * API để nhân viên xác thực 4 số cuối CCCD/Passport của khách khi gọi đặt đồ ăn lên phòng (Room Service)
     */
    @GetMapping("/{roomNumber}/verify-guest")
    public ResponseEntity<?> verifyGuest(
            @PathVariable String roomNumber,
            @RequestParam String last4Digits) {

        // 1. Lấy thông tin phòng
        Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Phòng không tồn tại."));
        }

        Room room = roomOpt.get();

        // Kiểm tra phòng có đang có khách ở không (có booking detail)
        if (room.getCurrentBookingDetailId() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Phòng hiện không có khách lưu trú."));
        }

        // 2. Lấy thông tin Booking Detail hiện tại của phòng
        Optional<RoomBookingDetail> detailOpt = roomBookingDetailRepository.findById(room.getCurrentBookingDetailId());
        if (detailOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi dữ liệu: Không tìm thấy booking detail."));
        }

        RoomBookingDetail bookingDetail = detailOpt.get();
        Customer customer = null;
        
        // Theo logic hiện tại, Customer có thể nằm ở Detail hoặc Booking
        if (bookingDetail.getCustomer() != null) {
            customer = bookingDetail.getCustomer();
        } else if (bookingDetail.getRoomBooking() != null && bookingDetail.getRoomBooking().getCustomer() != null) {
            customer = bookingDetail.getRoomBooking().getCustomer();
        }

        if (customer == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi dữ liệu: Không tìm thấy hồ sơ khách hàng."));
        }

        // 3. Lấy CCCD đã mã hóa và giải mã
        String encryptedCccd = customer.getCccdPassportEncrypted();
        if (encryptedCccd == null || encryptedCccd.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Hồ sơ khách hàng thiếu thông tin CCCD/Passport."));
        }

        try {
            String realCccd = com.kawai.utils.EncryptionUtils.decrypt(encryptedCccd);
            
            if (realCccd == null || realCccd.length() < 4) {
                 return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Chuỗi CCCD quá ngắn để xác thực."));
            }
            
            // Cắt 4 số cuối
            String actualLast4 = realCccd.substring(realCccd.length() - 4);
            
            if (actualLast4.equals(last4Digits)) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "guestName", customer.getFullName(),
                    "message", "Xác thực thành công!"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "4 số cuối CCCD/Passport không khớp!"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi giải mã CCCD: " + e.getMessage()));
        }
    }
}
