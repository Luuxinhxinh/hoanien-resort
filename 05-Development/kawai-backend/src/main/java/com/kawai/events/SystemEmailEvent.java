package com.kawai.events;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

public class SystemEmailEvent extends ApplicationEvent {
    private final String toEmail;
    private final String subject;
    private final String templateName;
    private final Map<String, Object> context;

    public SystemEmailEvent(Object source, String toEmail, String subject, String templateName, Map<String, Object> context) {
        super(source);
        this.toEmail = toEmail;
        this.subject = subject;
        this.templateName = templateName;
        this.context = context;
    }

    public String getToEmail() {
        return toEmail;
    }

    public String getSubject() {
        return subject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
