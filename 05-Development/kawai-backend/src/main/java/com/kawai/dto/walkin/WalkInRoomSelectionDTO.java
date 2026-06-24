package com.kawai.dto.walkin;

import com.kawai.dto.DependentRegistrationDTO;
import java.util.List;

public class WalkInRoomSelectionDTO {
    private Long roomId;
    private List<DependentRegistrationDTO> accompaniedGuests;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public List<DependentRegistrationDTO> getAccompaniedGuests() {
        return accompaniedGuests;
    }

    public void setAccompaniedGuests(List<DependentRegistrationDTO> accompaniedGuests) {
        this.accompaniedGuests = accompaniedGuests;
    }
}
