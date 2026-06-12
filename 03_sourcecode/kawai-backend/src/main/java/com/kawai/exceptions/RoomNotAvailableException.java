package com.kawai.exceptions;

/**
 * Ném ra khi phòng không còn trống trong khoảng ngày đã chọn.
 * HTTP status: 409 Conflict (BR-FO-01, UC10)
 */
public class RoomNotAvailableException extends RuntimeException {

    public RoomNotAvailableException(String message) {
        super(message);
    }

    public RoomNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
