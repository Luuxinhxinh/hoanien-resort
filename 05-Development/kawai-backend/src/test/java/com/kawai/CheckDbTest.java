package com.kawai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Commit;
import com.kawai.repositories.RoomRepository;
import com.kawai.models.Room;
import java.util.List;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CheckDbTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private com.kawai.repositories.RoomBookingDetailRepository roomBookingDetailRepository;

    @Test
    @Transactional
    @Commit
    public void testCount() {
        System.out.println("====== SYSTEM CHECK FOR ROOM 104 ======");
        java.util.Optional<Room> optRoom = roomRepository.findByRoomNumber("104");
        if (optRoom.isPresent()) {
            Room r = optRoom.get();
            System.out.println("ROOM_104_STATUS: " + r.getRoomStatus());
            System.out.println("ROOM_104_BOOKING_ID: " + r.getCurrentBookingDetailId());
            if (r.getCurrentBookingDetailId() != null) {
                roomBookingDetailRepository.findById(r.getCurrentBookingDetailId()).ifPresentOrElse(detail -> {
                    System.out.println("BOOKING_DETAIL_STATUS: " + detail.getDetailStatus());
                    if (detail.getRoomBooking() != null) {
                        System.out.println("BOOKING_PARENT_STATUS: " + detail.getRoomBooking().getBookingStatus());
                        if (detail.getRoomBooking().getCustomer() != null) {
                            System.out.println("GUEST_NAME: " + detail.getRoomBooking().getCustomer().getFullName());
                        }
                    }
                }, () -> {
                    System.out.println("BOOKING_DETAIL: Not found in database! -> Auto cleaning dirty reference...");
                    r.setCurrentBookingDetailId(null);
                    roomRepository.save(r);
                    System.out.println("CLEANED: set currentBookingDetailId to null successfully.");
                });
            }
        } else {
            System.out.println("ROOM_104_NOT_FOUND");
        }
        System.out.println("=======================================");
    }
}
