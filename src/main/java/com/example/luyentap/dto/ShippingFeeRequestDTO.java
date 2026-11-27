package com.example.luyentap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingFeeRequestDTO {
    private Integer toDistrictId;
    private String toWardCode;
    @Builder.Default
    private Integer weight = 300;
}
