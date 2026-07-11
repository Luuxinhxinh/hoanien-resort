package com.kawai.listeners;

import com.kawai.events.ManagerApprovalEvent;
import com.kawai.models.Booking;
import com.kawai.models.Customer;
import com.kawai.models.FolioItem;
import com.kawai.repositories.BookingRepository;
import com.kawai.repositories.FolioItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class ApprovalFinancialListener {

    @Autowired
    private FolioItemRepository folioItemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Async
    @EventListener
    @Transactional
    public void handleApprovalEvent(ManagerApprovalEvent event) {
        if (!"Completed".equals(event.getStatus())) return;

        Optional<Booking> optBk = bookingRepository.findById(event.getBookingId());
        if (optBk.isEmpty()) return;

        Booking bk = optBk.get();
        Customer payer = bk.getCustomer();

        if ("Late_Checkout_Waiver".equals(event.getOperationalType())) {
            FolioItem discount = new FolioItem();
            discount.setBooking(bk);
            discount.setPayerCustomer(payer);
            discount.setSourceDepartment("RECEPTION");
            discount.setAmount(new BigDecimal("-500000.00"));
            discount.setDescription("Waiver: Mien tru phi tra phong tre");
            discount.setIsSettledSeparately(false);
            discount.setCreatedAt(LocalDateTime.now());
            folioItemRepository.save(discount);
        } else if ("Room_Downgrade_Refund".equals(event.getOperationalType())) {
            FolioItem refund = new FolioItem();
            refund.setBooking(bk);
            refund.setPayerCustomer(payer);
            refund.setSourceDepartment("RECEPTION");
            refund.setAmount(new BigDecimal("-1000000.00"));
            refund.setDescription("Refund: Hoan tien chenh lech doi hang phong");
            refund.setIsSettledSeparately(false);
            refund.setCreatedAt(LocalDateTime.now());
            folioItemRepository.save(refund);
        }
    }
}
