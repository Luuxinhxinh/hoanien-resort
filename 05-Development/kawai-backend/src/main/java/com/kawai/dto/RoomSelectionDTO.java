package com.kawai.dto;

public class RoomSelectionDTO {
    private String roomNumber;
    private Integer numberOfAdults;
    private Integer numberOfChildren;

    public RoomSelectionDTO() {
    }

    public RoomSelectionDTO(String roomNumber, Integer numberOfAdults, Integer numberOfChildren) {
        this.roomNumber = roomNumber;
        this.numberOfAdults = numberOfAdults;
        this.numberOfChildren = numberOfChildren;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getNumberOfAdults() {
        return numberOfAdults;
    }

    public void setNumberOfAdults(Integer numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    public Integer getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(Integer numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }
}
