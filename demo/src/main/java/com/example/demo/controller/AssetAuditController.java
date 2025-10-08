package com.example.demo.controller;

import com.example.demo.entity.Asset;
import com.example.demo.entity.Audit;
import com.example.demo.service.AssetAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@Slf4j
public class AssetAuditController {
    
    private final AssetAuditService assetAuditService;

    public AssetAuditController(AssetAuditService assetAuditService) {
        this.assetAuditService = assetAuditService;
    }

    @GetMapping("/audit")
    public String auditDashboard(Model model) {
        List<Asset> assets = assetAuditService.getAllAssets();
        List<Audit> audits = assetAuditService.getAllAudits();
        
        model.addAttribute("assets", assets);
        model.addAttribute("audits", audits);
        
        return "audit-dashboard";
    }
    
    @GetMapping("/audit/capture")
    public String captureAssetPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "capture-asset";
    }
    
    @PostMapping("/audit/capture")
    public String processAssetCapture(@RequestParam("imageFile") MultipartFile imageFile,
                                    @RequestParam("auditorName") String auditorName,
                                    @RequestParam(value = "latitude", required = false) Double latitude,
                                    @RequestParam(value = "longitude", required = false) Double longitude,
                                    Model model) throws UnsupportedEncodingException {
        try {
            Asset asset = assetAuditService.processAssetImage(
                imageFile, auditorName, latitude, longitude);
            
            model.addAttribute("asset", asset);
            model.addAttribute("message", "Asset captured successfully! Device Number: " + asset.getDeviceNumber());
            
            return "audit-complete";
            
        } catch (IOException e) {
            return "redirect:/audit/capture?error=" + java.net.URLEncoder.encode("Failed to process image: " + e.getMessage(), "UTF-8");
        } catch (Exception e) {
            return "redirect:/audit/capture?error=" + java.net.URLEncoder.encode("Error processing asset: " + e.getMessage(), "UTF-8");
        }
    }
    
    @GetMapping("/audit/complete/{auditId}")
    public String completeAuditPage(@PathVariable Long auditId, Model model) {
        Optional<Audit> auditOpt = assetAuditService.getAuditById(auditId);
        if (auditOpt.isEmpty()) {
            model.addAttribute("error", "Audit not found");
            return "error";
        }
        
        model.addAttribute("audit", auditOpt.get());
        return "complete-audit";
    }
    
    @PostMapping("/audit/complete/{auditId}")
    public String completeAudit(@PathVariable Long auditId,
                               @RequestParam("condition") String condition,
                               @RequestParam("notes") String notes,
                               @RequestParam("status") String status,
                               Model model) {
        try {
            Audit audit = assetAuditService.completeAudit(auditId, condition, notes, status);
            model.addAttribute("audit", audit);
            model.addAttribute("message", "Audit completed successfully!");
            
            return "audit-complete";
            
        } catch (Exception e) {
            model.addAttribute("error", "Failed to complete audit: " + e.getMessage());
            return "complete-audit";
        }
    }
    
    @GetMapping("/audit/assets")
    public String assetsList(Model model) {
        List<Asset> assets = assetAuditService.getAllAssets();
        model.addAttribute("assets", assets);
        return "assets-list";
    }
    
    @GetMapping("/audit/audits")
    public String auditsList(Model model) {
        List<Audit> audits = assetAuditService.getAllAudits();
        model.addAttribute("audits", audits);
        return "audits-list";
    }
    
    @GetMapping("/audit/asset/{id}")
    public String assetDetail(@PathVariable Long id, Model model) {
        Optional<Asset> assetOpt = assetAuditService.getAssetById(id);
        if (assetOpt.isEmpty()) {
            model.addAttribute("error", "Asset not found");
            return "error";
        }
        
        model.addAttribute("asset", assetOpt.get());
        return "asset-detail";
    }
    
    // REST API endpoints
    
    @GetMapping("/api/assets")
    public ResponseEntity<List<Asset>> getAllAssets() {
        return ResponseEntity.ok(assetAuditService.getAllAssets());
    }
    
    @GetMapping("/api/audits")
    public ResponseEntity<List<Audit>> getAllAudits() {
        return ResponseEntity.ok(assetAuditService.getAllAudits());
    }
    
    @GetMapping("/api/assets/{id}")
    public ResponseEntity<Asset> getAsset(@PathVariable Long id) {
        Optional<Asset> asset = assetAuditService.getAssetById(id);
        return asset.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/audits/{id}")
    public ResponseEntity<Audit> getAudit(@PathVariable Long id) {
        Optional<Audit> audit = assetAuditService.getAuditById(id);
        return audit.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/api/assets/department/{department}")
    public ResponseEntity<List<Asset>> getAssetsByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(assetAuditService.getAssetsByDepartment(department));
    }
    
    @GetMapping("/api/audits/auditor/{auditorName}")
    public ResponseEntity<List<Audit>> getAuditsByAuditor(@PathVariable String auditorName) {
        return ResponseEntity.ok(assetAuditService.getAuditsByAuditor(auditorName));
    }
    
    @GetMapping("/images/assets/{filename:.+}")
    public ResponseEntity<byte[]> getAssetImage(@PathVariable String filename) {
        try {
            byte[] imageBytes = assetAuditService.getAssetImage(filename);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/api/audits/{auditId}/complete")
    public ResponseEntity<Map<String, Object>> completeAuditApi(@PathVariable Long auditId,
                                                                @RequestParam("condition") String condition,
                                                                @RequestParam("notes") String notes,
                                                                @RequestParam("status") String status) {
        try {
            Audit audit = assetAuditService.completeAudit(auditId, condition, notes, status);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Audit completed successfully!");
            response.put("auditId", audit.getId());
            response.put("status", audit.getStatus());
            response.put("completedAt", audit.getCompletedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to complete audit: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
