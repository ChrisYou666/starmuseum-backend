package com.starmuseum.modules.astro.service;

import com.starmuseum.modules.astro.entity.CelestialBody;
import com.starmuseum.modules.astro.service.model.AltAzResult;

/**
 * 星空计算服务（阶段2 MVP）
 * 负责：给定 time + lat/lon + (ra/dec) 计算 Alt/Az、可见性等。
 */
public interface AstronomyService {

    /**
     * 计算某天体在某时间/地点的高度角/方位角（单位：deg）
     *
     * @param time ISO-8601 且带时区，例如：2026-01-10T12:00:00Z
     */
    AltAzResult calcAltAz(String time, double lat, double lon, double raDeg, double decDeg);

    /**
     * 便捷重载：直接传 CelestialBody（使用其 raDeg/decDeg）
     */
    default AltAzResult calcAltAz(String time, double lat, double lon, CelestialBody body) {
        if (body == null) {
            return AltAzResult.of(null, null, false);
        }
        return calcAltAz(time, lat, lon, latSafe(body.getRaDeg()), latSafe(body.getDecDeg()));
    }

    private static Double latSafe(Double v) {
        return v;
    }
}
