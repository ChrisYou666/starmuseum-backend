package com.starmuseum.common.kv.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starmuseum.common.kv.entity.SysKvConfig;
import com.starmuseum.common.kv.mapper.SysKvConfigMapper;
import com.starmuseum.common.kv.service.SysKvConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysKvConfigServiceImpl implements SysKvConfigService {

    private final SysKvConfigMapper mapper;

    @Override
    public String get(String key) {
        if (!StringUtils.hasText(key)) return null;
        SysKvConfig row = mapper.selectOne(
            new LambdaQueryWrapper<SysKvConfig>()
                .eq(SysKvConfig::getConfigKey, key)
                .last("limit 1")
        );
        return row == null ? null : row.getConfigValue();
    }

    @Transactional
    @Override
    public void set(String key, String value, String remarkIfInsert) {
        if (!StringUtils.hasText(key)) return;

        SysKvConfig row = mapper.selectOne(
            new LambdaQueryWrapper<SysKvConfig>()
                .eq(SysKvConfig::getConfigKey, key)
                .last("limit 1")
        );

        if (row == null) {
            SysKvConfig ins = new SysKvConfig();
            ins.setConfigKey(key);
            ins.setConfigValue(value);
            ins.setRemark(remarkIfInsert);
            mapper.insert(ins);
            return;
        }

        SysKvConfig upd = new SysKvConfig();
        upd.setId(row.getId());
        upd.setConfigValue(value);
        mapper.updateById(upd);
    }

    @Transactional
    @Override
    public void setIfAbsent(String key, String value, String remarkIfInsert) {
        if (!StringUtils.hasText(key)) return;

        SysKvConfig row = mapper.selectOne(
            new LambdaQueryWrapper<SysKvConfig>()
                .eq(SysKvConfig::getConfigKey, key)
                .last("limit 1")
        );

        if (row != null) return;

        SysKvConfig ins = new SysKvConfig();
        ins.setConfigKey(key);
        ins.setConfigValue(value);
        ins.setRemark(remarkIfInsert);
        mapper.insert(ins);
    }
}
