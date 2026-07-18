package com.kawai.controllers.api;

import com.kawai.dto.SystemNotificationDTO;
import com.kawai.models.SystemNotification;
import com.kawai.services.interfaces.SystemNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class SystemNotificationApiController {

    private final SystemNotificationService notificationService;

    public SystemNotificationApiController(SystemNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String username = principal.getName();
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
            username = oauthToken.getPrincipal().getAttribute("email");
        }

        List<SystemNotification> unread = notificationService.getUnreadNotifications(username);
        List<SystemNotificationDTO> dtos = unread.stream().map(n -> new SystemNotificationDTO(
                n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.getTargetUrl(), n.getIsRead(), n.getCreatedAt()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String username = principal.getName();
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
            username = oauthToken.getPrincipal().getAttribute("email");
        }

        long count = notificationService.getUnreadCount(username);
        return ResponseEntity.ok(java.util.Collections.singletonMap("count", count));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String username = principal.getName();
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
            username = oauthToken.getPrincipal().getAttribute("email");
        }

        notificationService.markAsRead(id, username);
        return ResponseEntity.ok(java.util.Collections.singletonMap("success", true));
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String username = principal.getName();
        if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken =
                    (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) principal;
            username = oauthToken.getPrincipal().getAttribute("email");
        }

        notificationService.markAllAsRead(username);
        return ResponseEntity.ok(java.util.Collections.singletonMap("success", true));
    }
}
