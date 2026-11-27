package com.example.luyentap.controller;

import com.example.luyentap.dto.ShippingFeeRequestDTO;
import com.example.luyentap.service.GhnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@CrossOrigin
public class ShippingController {

    private final GhnService ghnService;

    /**
     * 1. Lấy danh sách Tỉnh / Thành phố
     * GET /api/shipping/provinces
     */
    @GetMapping(value = "/provinces", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getProvinces() {
        return ResponseEntity.ok(ghnService.getProvinces());
    }

    /**
     * 2. Lấy danh sách Quận / Huyện theo Tỉnh
     * GET /api/shipping/districts/{provinceId}
     */
    @GetMapping(value = "/districts/{provinceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDistricts(@PathVariable int provinceId) {
        return ResponseEntity.ok(ghnService.getDistricts(provinceId));
    }

    /**
     * 3. Lấy danh sách Phường / Xã theo Quận
     * GET /api/shipping/wards/{districtId}
     */
    @GetMapping(value = "/wards/{districtId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getWards(@PathVariable int districtId) {
        return ResponseEntity.ok(ghnService.getWards(districtId));
    }

    /**
     * 4. Tính cước phí giao hàng chuẩn GHN
     * POST /api/shipping/calculate-fee
     */
    @PostMapping("/calculate-fee")
    public ResponseEntity<Map<String, Object>> calculateFee(@RequestBody ShippingFeeRequestDTO dto) {
        if (dto.getToDistrictId() == null || dto.getToWardCode() == null) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "Thiếu mã quận/huyện hoặc phường/xã"));
        }
        Double fee = ghnService.calculateShippingFee(dto.getToDistrictId(), dto.getToWardCode(), dto.getWeight());
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "shippingFee", fee,
                "formattedFee", String.format("%,.0f VNĐ", fee)
        ));
    }
}
