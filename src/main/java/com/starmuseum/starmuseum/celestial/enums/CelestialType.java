package com.starmuseum.starmuseum.celestial.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 天体类型。
 *
 * 设计说明：
 * - 数据库存储为字符串（如 STAR/PLANET），便于直接读写与扩展。
 * - 接口入参/出参同样使用字符串，前端可直接使用。
 * - 使用 MyBatis-Plus 的 @EnumValue 让枚举按 value 落库。
 */
public enum CelestialType {

    STAR("STAR"),
    PLANET("PLANET"),
    MOON("MOON"),
    CONSTELLATION("CONSTELLATION"),
    NEBULA("NEBULA"),
    GALAXY("GALAXY"),
    CLUSTER("CLUSTER"),
    OTHER("OTHER");

    @EnumValue
    private final String value;

    CelestialType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CelestialType from(String value) {
        if (value == null) {
            return null;
        }
        for (CelestialType t : values()) {
            if (t.value.equalsIgnoreCase(value) || t.name().equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unsupported CelestialType: " + value);
    }
}