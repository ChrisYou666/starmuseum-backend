package com.starmuseum.modules.astro;

import java.time.Instant;

/**
 * 阶段2 MVP：把赤道坐标(RA/Dec)转换为水平坐标(Altitude/Azimuth)
 * 约定：输入输出全部用“度”。
 */
public class AstronomyCalculator {

    private AstronomyCalculator() {}

    public static AltAz equatorialToHorizontal(Instant instantUtc,
                                               double latitudeDeg,
                                               double longitudeDeg,
                                               double raDeg,
                                               double decDeg) {
        double jd = julianDay(instantUtc);
        double gmstDeg = gmstDegrees(jd);
        double lstDeg = normalize360(gmstDeg + longitudeDeg);

        // 时角 HA = LST - RA
        double haDeg = normalize180(lstDeg - raDeg);

        // 转弧度
        double latRad = Math.toRadians(latitudeDeg);
        double decRad = Math.toRadians(decDeg);
        double haRad = Math.toRadians(haDeg);

        // altitude
        double sinAlt = Math.sin(decRad) * Math.sin(latRad)
            + Math.cos(decRad) * Math.cos(latRad) * Math.cos(haRad);
        sinAlt = clamp(sinAlt, -1.0, 1.0);
        double altRad = Math.asin(sinAlt);

        // azimuth（从北向东：0~360）
        // 公式：az = atan2( -sin(HA), tan(dec)*cos(lat) - sin(lat)*cos(HA) )
        double y = -Math.sin(haRad);
        double x = Math.tan(decRad) * Math.cos(latRad) - Math.sin(latRad) * Math.cos(haRad);
        double azRad = Math.atan2(y, x);
        double azDeg = normalize360(Math.toDegrees(azRad));

        double altDeg = Math.toDegrees(altRad);

        return new AltAz(altDeg, azDeg, altDeg > 0);
    }

    /**
     * 儒略日 JD（基于UTC instant）
     * JD at Unix epoch (1970-01-01T00:00:00Z) = 2440587.5
     */
    public static double julianDay(Instant instantUtc) {
        double epochSeconds = instantUtc.getEpochSecond() + instantUtc.getNano() / 1_000_000_000.0;
        return 2440587.5 + epochSeconds / 86400.0;
    }

    /**
     * GMST（格林尼治平恒星时），单位：度（0~360）
     * 近似公式（MVP足够用）
     */
    public static double gmstDegrees(double jd) {
        double T = (jd - 2451545.0) / 36525.0;
        double gmst = 280.46061837
            + 360.98564736629 * (jd - 2451545.0)
            + 0.000387933 * T * T
            - (T * T * T) / 38710000.0;
        return normalize360(gmst);
    }

    private static double normalize360(double deg) {
        double x = deg % 360.0;
        if (x < 0) x += 360.0;
        return x;
    }

    /**
     * 归一化到 (-180, 180]
     */
    private static double normalize180(double deg) {
        double x = normalize360(deg);
        if (x > 180.0) x -= 360.0;
        return x;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public record AltAz(double altitudeDeg, double azimuthDeg, boolean visible) {}
}
