package com.kawai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.kawai.controllers.api.FolioRestController;
import com.kawai.services.interfaces.ChangeRoomCategoryService;
import com.kawai.dto.roomchange.ChangeRoomCategoryRequest;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@SpringBootTest
public class CheckBookingDetailTest {

    @Autowired
    private ChangeRoomCategoryService changeRoomCategoryService;

    @Autowired
    private FolioRestController folioRestController;

    @Test
    public void testGetFolioAfterUpgrade() {
        System.out.println("=== START FOLIO REST CONTROLLER TEST (UPGRADED) ===");
        
        // 1. Upgrade Room 201 (Detail 5051) to Room 408 (ID 38)
        ChangeRoomCategoryRequest req = new ChangeRoomCategoryRequest();
        req.setBookingDetailId(5051L);
        req.setSelectedRoomId(38L); // Room 408 (Wellness Retreats)
        req.setReceptionistAccountId(1L);

        try {
            changeRoomCategoryService.changeCategory(req);
            System.out.println("--- Upgrade room 201 to 408 successful ---");
        } catch (Exception e) {
            System.out.println("--- Upgrade failed: " + e.getMessage() + " ---");
        }

        // 2. Call the controller directly for detail ID 5051
        try {
            ResponseEntity<?> responseEntity = folioRestController.getFolioByRoom(5051L);
            System.out.println("STATUS: " + responseEntity.getStatusCode());
            if (responseEntity.getBody() instanceof Map) {
                Map<?, ?> body = (Map<?, ?>) responseEntity.getBody();
                for (Map.Entry<?, ?> entry : body.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }
            } else {
                System.out.println("BODY: " + responseEntity.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("=== END FOLIO REST CONTROLLER TEST (UPGRADED) ===");
    }
}
