package com.kawai.services.impl;

import com.kawai.models.*;
import com.kawai.repositories.*;
import com.kawai.services.interfaces.FolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FolioServiceImpl implements FolioService {

    @Autowired
    private FolioItemRepository folioItemRepository;

    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ConsolidatedInvoiceRepository consolidatedInvoiceRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private com.kawai.services.interfaces.EmailService emailService;

    @Override
    @Transactional
    public void addFolioItem(Long bookingDetailId, String department, BigDecimal amount, String description) {
        RoomBookingDetail detail = roomBookingDetailRepository.findById(bookingDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin đặt phòng"));

        FolioItem item = new FolioItem();
        item.setBooking(detail.getRoomBooking());
        item.setRoomBookingDetail(detail);
        item.setPayerCustomer(detail.getRoomBooking().getCustomer());
        item.setSourceDepartment(department);
        item.setAmount(amount);
        item.setDescription(description);
        item.setCreatedAt(LocalDateTime.now());
        folioItemRepository.save(item);
    }

    @Override
    public BigDecimal getFolioBalance(Long bookingDetailId) {
        List<FolioItem> items = folioItemRepository.findAll().stream()
                .filter(item -> item.getRoomBookingDetail() != null && item.getRoomBookingDetail().getId().equals(bookingDetailId))
                .toList();

        BigDecimal balance = BigDecimal.ZERO;
        for (FolioItem item : items) {
            if ("PAYMENT".equalsIgnoreCase(item.getSourceDepartment()) || item.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                // If it is payment, we subtract it (or if it is already negative, we just add it since addition of negative subtracts it)
                if (item.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    balance = balance.add(item.getAmount());
                } else {
                    balance = balance.subtract(item.getAmount());
                }
            } else {
                balance = balance.add(item.getAmount());
            }
        }
        return balance;
    }

    @Override
    @Transactional
    public void performNightAudit(Long staffId) {
        Employee staff = employeeRepository.findById(staffId).orElse(null);
        Account staffAccount = staff != null ? staff.getAccount() : null;

        LocalDate businessDate = LocalDate.now();

        // 1. Find all checked-in room details
        List<RoomBookingDetail> checkedInDetails = roomBookingDetailRepository.findAll().stream()
                .filter(d -> "CHECKED_IN".equalsIgnoreCase(d.getDetailStatus()))
                .toList();

        // 2. Post room charge for each room
        for (RoomBookingDetail detail : checkedInDetails) {
            FolioItem item = new FolioItem();
            item.setBooking(detail.getRoomBooking());
            item.setRoomBookingDetail(detail);
            item.setPayerCustomer(detail.getRoomBooking().getCustomer());
            item.setSourceDepartment("Rooms");
            item.setAmount(detail.getRoomCharge());
            item.setDescription("Tiền phòng đêm " + businessDate);
            item.setCreatedAt(LocalDateTime.now());
            folioItemRepository.save(item);
        }

        // 3. Advance business date by 1 day & write Audit Log
        AuditLog log = new AuditLog();
        log.setAction("NIGHT_AUDIT");
        log.setTableName("System_Config");
        log.setRecordId(1L);
        log.setOldValue(businessDate.toString());
        log.setNewValue(businessDate.plusDays(1).toString());
        log.setIpAddress("127.0.0.1");
        log.setTimestamp(LocalDateTime.now());
        if (staffAccount != null) {
            log.setAccount(staffAccount);
        }
        auditLogRepository.save(log);
    }

    @Override
    @Transactional
    public void checkOutAndSettle(Long bookingDetailId, String paymentMethod) {
        RoomBookingDetail detail = roomBookingDetailRepository.findById(bookingDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin đặt phòng"));

        BigDecimal balance = getFolioBalance(bookingDetailId);
        // BR-FIN-01: Cannot checkout if folio balance is non-zero
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("FOLIO-001: Hóa đơn chưa thanh toán hết — không thể Check-out");
        }

        // 1. Update RoomBookingDetail status
        detail.setDetailStatus("CHECKED_OUT");
        roomBookingDetailRepository.save(detail);

        // 2. Update Room status -> Dirty (BR-FO-04)
        Room room = detail.getRoom();
        if (room != null) {
            room.setRoomStatus("Dirty");
            room.setCurrentBookingDetailId(null);
            roomRepository.save(room);
        }

        // 3. Issue Consolidated Invoice
        ConsolidatedInvoice invoice = new ConsolidatedInvoice();
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setBooking(detail.getRoomBooking());
        
        // Sum total charges
        List<FolioItem> items = folioItemRepository.findAll().stream()
                .filter(item -> item.getRoomBookingDetail() != null && item.getRoomBookingDetail().getId().equals(bookingDetailId))
                .toList();
        BigDecimal totalCharges = BigDecimal.ZERO;
        for (FolioItem item : items) {
            if (!"PAYMENT".equalsIgnoreCase(item.getSourceDepartment()) && item.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalCharges = totalCharges.add(item.getAmount());
            }
        }

        BigDecimal subtotal = totalCharges.divide(new BigDecimal("1.10"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal vat = totalCharges.subtract(subtotal);

        invoice.setSubtotalBeforeVat(subtotal);
        invoice.setVatAmount(vat);
        invoice.setTotalAmount(totalCharges);
        invoice.setInvoiceStatus("Paid");
        invoice.setIssuedAt(LocalDateTime.now());
        consolidatedInvoiceRepository.save(invoice);

        // 4. Send HTML E-Invoice via EmailService
        if (detail.getRoomBooking().getCustomer() != null && detail.getRoomBooking().getCustomer().getEmail() != null) {
            try {
                emailService.sendInvoiceEmail(detail.getRoomBooking().getCustomer().getEmail(), invoice, null);
            } catch (Exception e) {
                System.err.println("Failed to send e-invoice email: " + e.getMessage());
            }
        }

        // 5. Audit Log
        AuditLog log = new AuditLog();
        log.setAction("CHECKOUT_SETTLED");
        log.setTableName("Room_Booking_Details");
        log.setRecordId(detail.getId());
        log.setOldValue("CHECKED_IN");
        log.setNewValue("CHECKED_OUT");
        log.setIpAddress("127.0.0.1");
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}
