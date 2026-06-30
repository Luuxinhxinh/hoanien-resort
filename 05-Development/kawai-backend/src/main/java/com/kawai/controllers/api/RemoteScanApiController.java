package com.kawai.controllers.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@RequestMapping("/api/v1/remote-scan")
public class RemoteScanApiController {

    // Store SseEmitters with sessionId as key
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping("/host-ip")
    public ResponseEntity<?> getHostIp() {
        try {
            String fallbackIp = InetAddress.getLocalHost().getHostAddress();
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                java.util.Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                        if (addr.getHostAddress().startsWith("192.168.")) {
                            return ResponseEntity.ok(Map.of("ip", addr.getHostAddress()));
                        }
                    }
                }
            }
            return ResponseEntity.ok(Map.of("ip", fallbackIp));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("ip", "localhost"));
        }
    }

    @GetMapping("/{sessionId}/subscribe")
    public SseEmitter subscribe(@PathVariable String sessionId) {
        // Set timeout to 5 minutes (300000ms)
        SseEmitter emitter = new SseEmitter(300000L);
        emitters.put(sessionId, emitter);

        emitter.onCompletion(() -> emitters.remove(sessionId));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(sessionId);
        });
        emitter.onError((e) -> emitters.remove(sessionId));

        // Send an initial event to keep connection alive
        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected to " + sessionId));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @PostMapping("/{sessionId}/submit")
    public ResponseEntity<?> submitScan(@PathVariable String sessionId, @RequestBody Map<String, String> payload) {
        String qrData = payload.get("qrData");
        if (qrData == null || qrData.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "qrData is missing"));
        }

        SseEmitter emitter = emitters.get(sessionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("SCAN_RESULT").data(qrData));
                emitter.complete(); // close connection after receiving the result
                return ResponseEntity.ok(Map.of("message", "Scan result sent successfully"));
            } catch (IOException e) {
                emitters.remove(sessionId);
                return ResponseEntity.internalServerError().body(Map.of("message", "Failed to send data to PC"));
            }
        }

        return ResponseEntity.notFound().build(); // No active PC listener for this session
    }
}
