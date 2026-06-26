package com.kawai.dto.walkin;

import com.kawai.dto.DependentRegistrationDTO;
import java.util.List;

public class WalkInRoomSelectionDTO {
    private Long roomId;
    private List<DependentRegistrationDTO> accompaniedGuests;
    private java.math.BigDecimal allocatedCreditLimit;

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

    public java.math.BigDecimal getAllocatedCreditLimit() {
        return allocatedCreditLimit;
    }

    public void setAllocatedCreditLimit(java.math.BigDecimal allocatedCreditLimit) {
        this.allocatedCreditLimit = allocatedCreditLimit;
    }
}
