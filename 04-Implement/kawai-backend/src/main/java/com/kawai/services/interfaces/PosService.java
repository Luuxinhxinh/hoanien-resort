package com.kawai.services.interfaces;

import java.math.BigDecimal;

public interface PosService {
    void chargeToRoom(String roomNumber, BigDecimal amount);
}
