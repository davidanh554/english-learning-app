package com.example.luyentap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {
    private String customerName;
    private String phone;
    private String address;

    private Integer provinceId;
    private String provinceName;
    private Integer toDistrictId;
    private String districtName;
    private String toWardCode;
    private String wardName;

    private Double shippingFee;

    @Builder.Default
    private String paymentMethod = "COD";

    private List<Long> selectedProductIds;
}
