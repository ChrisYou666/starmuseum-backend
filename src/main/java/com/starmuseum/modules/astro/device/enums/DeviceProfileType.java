// src/main/java/com/starmuseum/modules/astro/device/enums/DeviceProfileType.java
package com.starmuseum.modules.astro.device.enums;

/**
 * 设备配置类型
 * - PHOTO: 摄影（相机/镜头/传感器）
 * - VISUAL: 目视（望远镜/目镜/视场）
 */
public enum DeviceProfileType {

    PHOTO,
    VISUAL;

    public static DeviceProfileType fromString(String v) {
        if (v == null) return null;
        String s = v.trim();
        if (s.isEmpty()) return null;
        return DeviceProfileType.valueOf(s.toUpperCase());
    }
}
