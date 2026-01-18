package com.starmuseum.modules.observation.enums;

public enum ObservationMethod {
    PHOTO,
    VISUAL,
    OTHER;

    public static ObservationMethod fromString(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty()) return null;
        return ObservationMethod.valueOf(s.toUpperCase());
    }
}
