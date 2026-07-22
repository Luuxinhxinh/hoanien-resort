package com.kawai.services;

import com.kawai.dto.CancelOrderRequestDTO;
import com.kawai.exceptions.BusinessException;
import com.kawai.models.*;
import com.kawai.repositories.FolioItemRepository;
import com.kawai.repositories.FoodOrderRepository;
import com.kawai.repositories.RefundRequestRepository;
import com.kawai.services.impl.PosServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UC23 — Hủy đơn hàng")
class PosServiceUC23Test {

    @InjectMocks
    private PosServiceImpl posService;

    @Mock
    private FoodOrderRepository foodOrderRepository;

    @Mock
    private FolioItemRepository folioItemRepository;

    @Mock
    private RefundRequestRepository refundRequestRepository;

    @Test
    @DisplayName("TC-UC23-01 | Hủy đơn Cash Pending -> Thành công, cập nhật trạng thái các món")
    void testCancelOrder_CashPending_Success() {
        // Arrange
        Long orderId = 1L;
        FoodOrder order = new FoodOrder();
        order.setId(orderId);
        order.setOrderStatus("Pending");
        order.setPaymentType("CASH");

        FoodOrderDetail detail1 = new FoodOrderDetail();
        detail1.setKotStatus("Pending");
        order.setDetails(Arrays.asList(detail1));

        when(foodOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        posService.cancelOrder(orderId, null, null, null);

        // Assert
        assertEquals("Cancelled", order.getOrderStatus(), "Trạng thái đơn phải là Cancelled");
        assertEquals("Cancelled", detail1.getKotStatus(), "Các món trong đơn phải được hủy");
        verify(foodOrderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("TC-UC23-02 | Hủy đơn Charge to Room -> Xóa FolioItem liên quan")
    void testCancelOrder_ChargeToRoom_Success() {
        // Arrange
        Long orderId = 2L;
        FoodOrder order = new FoodOrder();
        order.setId(orderId);
        order.setOrderStatus("Pending");
        order.setPaymentType("CHARGE_TO_ROOM");
        order.setDetails(Arrays.asList());

        RoomBookingDetail roomBookingDetail = new RoomBookingDetail();
        roomBookingDetail.setId(10L);
        order.setRoomBookingDetail(roomBookingDetail);

        FolioItem targetFolio = new FolioItem();
        targetFolio.setId(100L);
        targetFolio.setDescription("Ký bill đồ ăn F&B (Order #" + orderId + ")");

        FolioItem otherFolio = new FolioItem();
        otherFolio.setId(101L);
        otherFolio.setDescription("Ký bill đồ ăn F&B (Order #99)");

        when(foodOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(folioItemRepository.findByRoomBookingDetailId(10L)).thenReturn(Arrays.asList(targetFolio, otherFolio));

        // Act
        posService.cancelOrder(orderId, null, null, null);

        // Assert
        verify(folioItemRepository, times(1)).delete(targetFolio);
        verify(folioItemRepository, never()).delete(otherFolio);
    }

    @Test
    @DisplayName("TC-UC23-03 | Hủy đơn Online -> Tạo Refund Request")
    void testCancelOrder_OnlinePayment_Success() {
        // Arrange
        Long orderId = 3L;
        FoodOrder order = spy(new FoodOrder());
        order.setId(orderId);
        order.setOrderStatus("Pending");
        order.setPaymentType("VNPAY");
        order.setDetails(Arrays.asList());
        doReturn(new BigDecimal("500000")).when(order).getTotalAmount();

        when(foodOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        CancelOrderRequestDTO dto = new CancelOrderRequestDTO();
        dto.setAccountNumber("123456789");
        dto.setBankName("VCB");
        dto.setAccountName("Nguyen Van A");

        // Act
        posService.cancelOrder(orderId, dto, null, null);

        // Assert
        verify(refundRequestRepository, times(1)).save(argThat(req -> 
            req.getAmount().compareTo(new BigDecimal("500000")) == 0 &&
            req.getAccountNumber().equals("123456789") &&
            req.getStatus().equals("Pending")
        ));
    }

    @Test
    @DisplayName("TC-UC23-04 | Lỗi: Không thể hủy đơn khác Pending")
    void testCancelOrder_NotPending_ThrowsException() {
        // Arrange
        Long orderId = 4L;
        FoodOrder order = new FoodOrder();
        order.setId(orderId);
        order.setOrderStatus("Preparing");

        when(foodOrderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            posService.cancelOrder(orderId, null, null, null);
        });

        assertEquals("POS-007", exception.getErrorCode());
        assertTrue(exception.getMessage().contains("Pending"));
    }
}
