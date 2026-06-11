package com.kawai.services.interfaces;

import com.kawai.dto.RoomSearchRequestDTO;
import com.kawai.dto.RoomSearchResponseDTO;

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
}