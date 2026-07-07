package com.kawai.events;

import com.kawai.models.Customer;
import com.kawai.models.TableReservation;
import com.kawai.models.RoomBookingDetail;
import com.kawai.models.Booking;
import com.kawai.models.RoomBooking;
import com.kawai.repositories.TableReservationRepository;
import com.kawai.repositories.RoomBookingDetailRepository;
import com.kawai.services.interfaces.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class CustomerCheckedOutEventListener {

    private final TableReservationRepository tableReservationRepository;
    private final RoomBookingDetailRepository roomBookingDetailRepository;
    private final EmailService emailService;

    public CustomerCheckedOutEventListener(TableReservationRepository tableReservationRepository,
                                           RoomBookingDetailRepository roomBookingDetailRepository,
                                           EmailService emailService) {
        this.tableReservationRepository = tableReservationRepository;
        this.roomBookingDetailRepository = roomBookingDetailRepository;
        this.emailService = emailService;
    }

    @EventListener
    @Transactional
    public void handleCustomerCheckedOutEvent(CustomerCheckedOutEvent event) {
        Customer customer = event.getCustomer();
        if (customer == null) return;

        // Check if customer STILL has any active Checked_In rooms (either as direct guest or main booker)
        boolean hasActiveRooms = false;
        List<RoomBookingDetail> detailsByCustomer = roomBookingDetailRepository.findByAnyCustomerId(customer.getId());
        if (detailsByCustomer != null) {
            for (RoomBookingDetail d : detailsByCustomer) {
                if ("Checked_In".equalsIgnoreCase(d.getDetailStatus())) {
                    hasActiveRooms = true;
                    break;
                }
            }
        }

        // If they have no other Checked_In rooms, cancel their future table reservations
        if (!hasActiveRooms) {
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            List<TableReservation> reservations = tableReservationRepository.findByCustomerOrderByIdDesc(customer);
            if (reservations != null) {
                for (TableReservation res : reservations) {
                    if ("Pending".equalsIgnoreCase(res.getStatus()) || "Confirmed".equalsIgnoreCase(res.getStatus())) {
                        boolean isFuture = res.getReserveDate().isAfter(today) || 
                                          (res.getReserveDate().isEqual(today) && res.getReserveTime().isAfter(now));
                        
                        if (isFuture) {
                            res.setStatus("Cancelled");
                            tableReservationRepository.save(res);
                            
                            // Send email
                            try {
                                emailService.sendTableCancellationDueToCheckoutEmail(res, customer);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
}
