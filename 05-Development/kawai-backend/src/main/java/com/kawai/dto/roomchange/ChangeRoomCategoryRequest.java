package com.kawai.dto.roomchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoomCategoryRequest {
    private Long bookingDetailId;
    private Long selectedRoomId;
    private String targetCategoryName;
    private Long receptionistAccountId;
}
