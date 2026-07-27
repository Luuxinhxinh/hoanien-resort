package com.hoanien.services.interfaces;

import com.hoanien.dto.RoomSearchRequestDTO;
import com.hoanien.dto.RoomSearchResponseDTO;

import java.util.List;

/**
 * RoomService — UC09: Tìm kiếm phòng trống thời gian thực
 */
public interface RoomService {

    /**
     * Tìm kiếm phòng trống trong khoảng ngày chỉ định (UC09).
     * 
     * @param request thông tin tìm kiếm (checkIn, checkOut, filters)
     * @return Danh sách phòng trống, rỗng nếu không có
     */
    List<RoomSearchResponseDTO> searchAvailableRooms(RoomSearchRequestDTO request);

    /**
     * Lấy sơ đồ phòng (Room Matrix) thời gian thực (UC11).
     * 
     * @return Danh sách DTO hiển thị trạng thái các phòng, rỗng nếu chưa có.
     */
    List<com.hoanien.dto.RoomDashboardDTO> getRoomDashboard();

    com.hoanien.models.Room createRoom(String roomNumber, Long categoryId);
}