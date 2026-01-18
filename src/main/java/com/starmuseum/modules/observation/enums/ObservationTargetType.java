package com.starmuseum.modules.observation.enums;

public enum ObservationTargetType {
    CELESTIAL_BODY,
    TEXT;

    public static ObservationTargetType fromString(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty()) return null;
        return ObservationTargetType.valueOf(s.toUpperCase());
    }
}
