package com.kawai.listeners;

import com.kawai.events.ManagerApprovalEvent;
import com.kawai.models.RoomBookingDetail;
import com.kawai.repositories.RoomBookingDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ApprovalInventoryListener {

    @Autowired
    private RoomBookingDetailRepository roomBookingDetailRepository;

    @Async
    @EventListener
    @Transactional
    public void handleApprovalEvent(ManagerApprovalEvent event) {
        if ("Cancellation_Fee_Waiver".equals(event.getOperationalType()) && "Completed".equals(event.getStatus())) {
            List<RoomBookingDetail> details = roomBookingDetailRepository.findByRoomBookingId(event.getBookingId());
            for (RoomBookingDetail detail : details) {
                if (!"Cancelled".equalsIgnoreCase(detail.getDetailStatus())) {
                    detail.setDetailStatus("Cancelled");
                    detail.setRoom(null);
                    roomBookingDetailRepository.save(detail);
                }
            }
        }
    }
}
