package com.kawai.dto;

/**
 * DTO dung tai check-in form: map tung TourBooking chua phan bo -> phong vat ly.
 * Chieu: Tour -> Phong (le tan chon phong cho tung tour, khong phai nguoc lai).
 * Mot phong co the nhan nhieu tour.
 */
public class TourRoomAllocationDTO {

    /** ID cua TourBooking da ton tai (roomBookingDetail == null) */
    private Long tourBookingId;

    /** So phong vat ly (roomNumber) duoc chon de chiu chi phi tour nay */
    private String roomNumber;

    public TourRoomAllocationDTO() {}

    public Long getTourBookingId() {
        return tourBookingId;
    }

    public void setTourBookingId(Long tourBookingId) {
        this.tourBookingId = tourBookingId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}
