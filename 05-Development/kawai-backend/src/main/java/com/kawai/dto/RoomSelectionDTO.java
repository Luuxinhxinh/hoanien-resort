package com.kawai.dto;

public class RoomSelectionDTO {
    private String roomNumber;
    private String categoryName;
    private Integer numberOfAdults;
    private Integer numberOfChildren;
    private java.util.List<Integer> childrenAges;

    public RoomSelectionDTO() {
    }

    public RoomSelectionDTO(String roomNumber, Integer numberOfAdults, Integer numberOfChildren) {
        this.roomNumber = roomNumber;
        this.numberOfAdults = numberOfAdults;
        this.numberOfChildren = numberOfChildren;
    }

    public RoomSelectionDTO(String roomNumber, String categoryName, Integer numberOfAdults, Integer numberOfChildren) {
        this.roomNumber = roomNumber;
        this.categoryName = categoryName;
        this.numberOfAdults = numberOfAdults;
        this.numberOfChildren = numberOfChildren;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public java.util.List<Integer> getChildrenAges() {
        return childrenAges;
    }

    public void setChildrenAges(java.util.List<Integer> childrenAges) {
        this.childrenAges = childrenAges;
    }
}
