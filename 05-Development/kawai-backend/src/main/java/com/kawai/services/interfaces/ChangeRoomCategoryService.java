package com.kawai.services.interfaces;

import com.kawai.dto.roomchange.ChangeRoomCategoryRequest;
import com.kawai.dto.roomchange.ChangeRoomCategoryResponse;
import com.kawai.exceptions.BusinessException;

public interface ChangeRoomCategoryService {
    /**
     * Executes the room category change based on the request.
     * @param request the request containing details of the change
     * @return response with folioItemId if applicable
     * @throws BusinessException if business rules are violated
     */
    ChangeRoomCategoryResponse changeCategory(ChangeRoomCategoryRequest request) throws BusinessException;

    /**
     * Cancels a pending room category change.
     * @param bookingDetailId the ID of the room booking detail
     * @param receptionistAccountId the ID of the receptionist cancelling the change
     */
    void cancelPendingChange(Long bookingDetailId, Long receptionistAccountId);
}
