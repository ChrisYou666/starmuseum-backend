package com.starmuseum.modules.astro;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AstronomyCalculatorTest {

    @Test
    void shouldComputeAltAzInValidRange() {
        // 随便选一个时间点（UTC）
        Instant t = Instant.parse("2026-01-10T12:00:00Z");

        // 观测点：上海附近（示例）
        double lat = 31.2304;
        double lon = 121.4737;

        // Sirius（示例RA/Dec，与你表里一致：ra_deg=101.287 dec_deg=-16.716）
        var r = AstronomyCalculator.equatorialToHorizontal(t, lat, lon, 101.287, -16.716);

        assertFalse(Double.isNaN(r.altitudeDeg()));
        assertFalse(Double.isNaN(r.azimuthDeg()));

        assertTrue(r.altitudeDeg() >= -90.0 && r.altitudeDeg() <= 90.0);
        assertTrue(r.azimuthDeg() >= 0.0 && r.azimuthDeg() < 360.0);
    }
}
