package com.kawai.controllers.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/remote-scan")
public class RemoteScanApiController {

    // Store SseEmitters with sessionId as key
    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

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
