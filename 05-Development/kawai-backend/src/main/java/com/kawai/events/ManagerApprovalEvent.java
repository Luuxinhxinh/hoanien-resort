package com.kawai.events;

import org.springframework.context.ApplicationEvent;

public class ManagerApprovalEvent extends ApplicationEvent {
    private final Long bookingId;
    private final String operationalType;
    private final String status;
    private final Long taskId;

    public ManagerApprovalEvent(Object source, Long bookingId, String operationalType, String status, Long taskId) {
        super(source);
        this.bookingId = bookingId;
        this.operationalType = operationalType;
        this.status = status;
        this.taskId = taskId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getOperationalType() {
        return operationalType;
    }

    public String getStatus() {
        return status;
    }

    public Long getTaskId() {
        return taskId;
    }
}

