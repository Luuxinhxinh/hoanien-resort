package com.kawai.controllers.api;

import com.kawai.models.AuthorizedDevice;
import com.kawai.repositories.AuthorizedDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin/api/v1/devices")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AuthorizedDeviceApiController {

    @Autowired
    private AuthorizedDeviceRepository authorizedDeviceRepository;

    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        Optional<AuthorizedDevice> optDevice = authorizedDeviceRepository.findById(id);
        if (optDevice.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiết bị không tồn tại"));
        }
        AuthorizedDevice device = optDevice.get();
        device.setIsApproved(!device.getIsApproved());
        authorizedDeviceRepository.save(device);
        return ResponseEntity.ok(Map.of("success", true, "message", "Trạng thái thiết bị đã được cập nhật", "isApproved", device.getIsApproved()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable Long id) {
        if (!authorizedDeviceRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiết bị không tồn tại"));
        }
        authorizedDeviceRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Thiết bị đã được xóa thành công"));
    }
}
