package com.kawai.events;

import org.springframework.context.ApplicationEvent;
import com.kawai.models.Customer;

public class CustomerCheckedOutEvent extends ApplicationEvent {
    private final Customer customer;

    public CustomerCheckedOutEvent(Object source, Customer customer) {
        super(source);
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }
}
