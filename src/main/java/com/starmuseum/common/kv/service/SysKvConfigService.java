package com.starmuseum.common.kv.service;

public interface SysKvConfigService {

    /**
     * 获取配置值（不存在返回 null）
     */
    String get(String key);

    /**
     * 写入配置（不存在则插入，存在则更新）
     */
    void set(String key, String value, String remarkIfInsert);

    /**
     * 只有在 key 不存在时才插入（存在则不动）
     */
    void setIfAbsent(String key, String value, String remarkIfInsert);
}
