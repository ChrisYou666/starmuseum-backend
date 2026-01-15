package com.starmuseum.common.util;

import com.starmuseum.common.enums.LocationVisibility;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 地理坐标工具（阶段3）
 * - 坐标范围校验
 * - 降精度（fuzzy）
 * - BigDecimal(10,6) 转换
 */
public class GeoFuzzUtil {

    private GeoFuzzUtil() {}

    public static void validateLatLon(Double lat, Double lon) {
        if (lat == null || lon == null) {
            throw new IllegalArgumentException("lat/lon 不能为空");
        }
        if (lat < -90.0 || lat > 90.0) {
            throw new IllegalArgumentException("lat 超出范围[-90,90]");
        }
        if (lon < -180.0 || lon > 180.0) {
            throw new IllegalArgumentException("lon 超出范围[-180,180]");
        }
    }

    /**
     * 将 double 按指定小数位进行四舍五入（用于 fuzzy）
     */
    public static double fuzz(double value, int decimals) {
        return BigDecimal.valueOf(value)
            .setScale(decimals, RoundingMode.HALF_UP)
            .doubleValue();
    }

    /**
     * 转换成 DECIMAL(10,6) 适配的 BigDecimal（统一 6 位小数）
     */
    public static BigDecimal toDecimal6(Double value) {
        if (value == null) return null;
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * 解析可见性（非法/空 → HIDDEN）
     */
    public static LocationVisibility parseVisibility(String visibility) {
        if (visibility == null || visibility.trim().isEmpty()) {
            return LocationVisibility.HIDDEN;
        }
        try {
            return LocationVisibility.valueOf(visibility.trim().toUpperCase());
        } catch (Exception e) {
            return LocationVisibility.HIDDEN;
        }
    }
}
