package com.hoanien.services.interfaces;

import com.hoanien.dto.TableReservationRequest;
import com.hoanien.models.TableReservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface TableReservationService {
    List<Long> getAvailableTables(LocalDate date, LocalTime start, LocalTime end);

    TableReservation createReservation(TableReservationRequest request, java.security.Principal principal);

    List<Map<String, Object>> getTableReservations(Long tableId, LocalDate date);

    List<Map<String, Object>> getAllTablesWithReservations(LocalDate date);

    void checkInReservation(Long reservationId);

    TableReservation holdReservation(Long reservationId, int holdMinutes);

    void cancelReservation(Long reservationId, java.security.Principal principal, String reason);
}