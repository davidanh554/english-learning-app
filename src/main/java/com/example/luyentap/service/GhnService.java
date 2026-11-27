package com.example.luyentap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
public class GhnService {

    @Value("${ghn.base-url:https://dev-online-gateway.ghn.vn/shiip/public-api}")
    private String baseUrl;

    @Value("${ghn.token:53ea2c90-aa92-11f1-a973-aee5264794df}")
    private String token;

    @Value("${ghn.shop-id:242482}")
    private Integer shopId;

    @Value("${ghn.from-district-id:1450}")
    private Integer fromDistrictId;

    @Value("${ghn.from-ward-code:1B1501}")
    private String fromWardCode;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 1. Lấy danh sách Tỉnh / Thành phố từ GHN
     */
    public String getProvinces() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/master-data/province"))
                    .header("Token", token)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body().contains("\"data\"")) {
                return response.body();
            }
            log.warn("GHN Province API returned non-200: {} -> Using fallback provinces", response.statusCode());
        } catch (Exception e) {
            log.warn("Unable to connect to GHN Province API: {} -> Using fallback provinces", e.getMessage());
        }
        return getFallbackProvinces();
    }

    /**
     * 2. Lấy danh sách Quận / Huyện theo Tỉnh
     */
    public String getDistricts(int provinceId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/master-data/district?province_id=" + provinceId))
                    .header("Token", token)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body().contains("\"data\"")) {
                return response.body();
            }
            log.warn("GHN District API returned non-200: {} -> Using fallback districts", response.statusCode());
        } catch (Exception e) {
            log.warn("Unable to connect to GHN District API: {} -> Using fallback districts", e.getMessage());
        }
        return getFallbackDistricts(provinceId);
    }

    /**
     * 3. Lấy danh sách Phường / Xã theo Quận
     */
    public String getWards(int districtId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/master-data/ward?district_id=" + districtId))
                    .header("Token", token)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body().contains("\"data\"")) {
                return response.body();
            }
            log.warn("GHN Ward API returned non-200: {} -> Using fallback wards", response.statusCode());
        } catch (Exception e) {
            log.warn("Unable to connect to GHN Ward API: {} -> Using fallback wards", e.getMessage());
        }
        return getFallbackWards(districtId);
    }

    /**
     * 4. Tính cước phí giao hàng chuẩn GHN
     */
    public Double calculateShippingFee(int toDistrictId, String toWardCode, int weightGram) {
        try {
            String payload = String.format("""
                {
                    "service_type_id": 2,
                    "from_district_id": %d,
                    "from_ward_code": "%s",
                    "to_district_id": %d,
                    "to_ward_code": "%s",
                    "weight": %d
                }
            """, fromDistrictId, fromWardCode, toDistrictId, toWardCode, Math.max(weightGram, 200));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v2/shipping-order/fee"))
                    .header("Token", token)
                    .header("ShopId", String.valueOf(shopId))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.has("data") && root.path("data").has("total")) {
                    double totalFee = root.path("data").path("total").asDouble();
                    log.info("GHN Calculated fee for district {}: {} VND", toDistrictId, totalFee);
                    return totalFee;
                }
            }
            log.warn("GHN Fee API returned unexpected body: {} -> Using calculated estimate", response.body());
        } catch (Exception e) {
            log.warn("Unable to connect to GHN Fee API: {} -> Using estimated fee", e.getMessage());
        }

        // Ước tính cước thực tế nếu GHN test timeout: Nội thành 22k, liên tỉnh 35k
        if (toDistrictId == fromDistrictId || toDistrictId == 1450 || toDistrictId == 1442) {
            return 22000.0;
        }
        return 35000.0;
    }

    // --- FALLBACK MOCK DATA NẾU API GHN DEV ĐANG BẢO TRÌ HOẶC TOKEN HẾT HẠN ---
    private String getFallbackProvinces() {
        return """
        {
            "code": 200,
            "message": "Success (Fallback)",
            "data": [
                {"ProvinceID": 201, "ProvinceName": "Hà Nội"},
                {"ProvinceID": 202, "ProvinceName": "Hồ Chí Minh"},
                {"ProvinceID": 203, "ProvinceName": "Đà Nẵng"},
                {"ProvinceID": 204, "ProvinceName": "Hải Phòng"},
                {"ProvinceID": 205, "ProvinceName": "Cần Thơ"}
            ]
        }
        """;
    }

    private String getFallbackDistricts(int provinceId) {
        if (provinceId == 201) {
            return """
            {
                "code": 200,
                "message": "Success",
                "data": [
                    {"DistrictID": 1450, "DistrictName": "Quận Đống Đa"},
                    {"DistrictID": 1442, "DistrictName": "Quận Cầu Giấy"},
                    {"DistrictID": 1443, "DistrictName": "Quận Ba Đình"},
                    {"DistrictID": 1444, "DistrictName": "Quận Hoàn Kiếm"}
                ]
            }
            """;
        }
        return """
        {
            "code": 200,
            "message": "Success",
            "data": [
                {"DistrictID": 1451, "DistrictName": "Quận 1"},
                {"DistrictID": 1452, "DistrictName": "Quận Bình Thạnh"},
                {"DistrictID": 1453, "DistrictName": "Thành phố Thủ Đức"}
            ]
        }
        """;
    }

    private String getFallbackWards(int districtId) {
        return """
        {
            "code": 200,
            "message": "Success",
            "data": [
                {"WardCode": "1B1501", "WardName": "Phường Láng Thượng"},
                {"WardCode": "1B1502", "WardName": "Phường Ô Chợ Dừa"},
                {"WardCode": "1B1503", "WardName": "Phường Cát Linh"},
                {"WardCode": "1B1504", "WardName": "Phường Trung Tự"}
            ]
        }
        """;
    }
}
