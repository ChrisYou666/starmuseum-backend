package com.starmuseum.modules.astro.util;

import java.time.Instant;

public class AstronomyCalculator {

    public record AltAz(double altitudeDeg, double azimuthDeg, boolean visible) {}

    public static AltAz equatorialToHorizontal(Instant timeUtc,
                                               double latDeg,
                                               double lonDeg,
                                               double raDeg,
                                               double decDeg) {
        // 1) 计算儒略日 JD
        double jd = toJulianDay(timeUtc);

        // 2) 计算格林尼治恒星时 GMST（度）
        double gmstDeg = greenwichMeanSiderealTimeDeg(jd);

        // 3) 计算本地恒星时 LST（度）
        // 注意：lonDeg 东经为正；如果你库里 lon 是东经为正，直接加即可
        double lstDeg = normalizeDeg(gmstDeg + lonDeg);

        // 4) 时角 HA = LST - RA
        double haDeg = normalizeDeg(lstDeg - raDeg);

        // 5) 转弧度
        double ha = Math.toRadians(haDeg);
        double dec = Math.toRadians(decDeg);
        double lat = Math.toRadians(latDeg);

        // 6) 高度角 alt
        double sinAlt = Math.sin(dec) * Math.sin(lat) + Math.cos(dec) * Math.cos(lat) * Math.cos(ha);
        sinAlt = clamp(sinAlt, -1.0, 1.0);
        double alt = Math.asin(sinAlt);

        // 7) 方位角 az（0~360，北=0，东=90）
        double cosAz = (Math.sin(dec) - Math.sin(alt) * Math.sin(lat)) / (Math.cos(alt) * Math.cos(lat));
        cosAz = clamp(cosAz, -1.0, 1.0);
        double az = Math.acos(cosAz);

        // 根据 sin(HA) 判断象限
        if (Math.sin(ha) > 0) {
            az = 2 * Math.PI - az;
        }

        double altDegOut = Math.toDegrees(alt);
        double azDegOut = normalizeDeg(Math.toDegrees(az));

        boolean visible = altDegOut > 0;

        return new AltAz(altDegOut, azDegOut, visible);
    }

    private static double toJulianDay(Instant t) {
        // Unix epoch (1970-01-01T00:00:00Z) 对应 JD 2440587.5
        double epochSeconds = t.getEpochSecond() + t.getNano() / 1_000_000_000.0;
        return 2440587.5 + epochSeconds / 86400.0;
    }

    private static double greenwichMeanSiderealTimeDeg(double jd) {
        double T = (jd - 2451545.0) / 36525.0;
        // 近似公式（阶段2够用）
        double gmst = 280.46061837
            + 360.98564736629 * (jd - 2451545.0)
            + 0.000387933 * T * T
            - (T * T * T) / 38710000.0;
        return normalizeDeg(gmst);
    }

    private static double normalizeDeg(double deg) {
        double x = deg % 360.0;
        if (x < 0) x += 360.0;
        return x;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
