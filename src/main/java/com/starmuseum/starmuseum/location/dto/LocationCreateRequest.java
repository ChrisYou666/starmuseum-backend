package com.starmuseum.starmuseum.location.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建地点请求
 */
@Data
public class LocationCreateRequest {

    @NotBlank(message = "name 不能为空")
    @Size(max = 128, message = "name 最长 128")
    private String name;

    @Size(max = 64, message = "country 最长 64")
    private String country;

    @Size(max = 64, message = "province 最长 64")
    private String province;

    @Size(max = 64, message = "city 最长 64")
    private String city;

    @DecimalMin(value = "-90.0", message = "latitude 必须在 -90 ~ 90")
    @DecimalMax(value = "90.0", message = "latitude 必须在 -90 ~ 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "longitude 必须在 -180 ~ 180")
    @DecimalMax(value = "180.0", message = "longitude 必须在 -180 ~ 180")
    private Double longitude;

    @NotBlank(message = "timezone 不能为空")
    @Size(max = 64, message = "timezone 最长 64")
    private String timezone = "Asia/Shanghai";

    private Integer altitudeM;

    @Size(max = 255, message = "remark 最长 255")
    private String remark;
}