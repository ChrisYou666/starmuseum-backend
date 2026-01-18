// src/main/java/com/starmuseum/modules/astro/device/service/impl/DeviceProfileServiceImpl.java
package com.starmuseum.modules.astro.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starmuseum.common.exception.BizException;
import com.starmuseum.modules.astro.device.dto.DeviceProfileCreateRequest;
import com.starmuseum.modules.astro.device.dto.DeviceProfileUpdateRequest;
import com.starmuseum.modules.astro.device.entity.DeviceProfile;
import com.starmuseum.modules.astro.device.enums.DeviceProfileType;
import com.starmuseum.modules.astro.device.mapper.DeviceProfileMapper;
import com.starmuseum.modules.astro.device.service.DeviceProfileService;
import com.starmuseum.modules.astro.device.vo.DeviceProfileVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DeviceProfileServiceImpl extends ServiceImpl<DeviceProfileMapper, DeviceProfile> implements DeviceProfileService {

    @Override
    @Transactional
    public DeviceProfileVO createMy(Long userId, DeviceProfileCreateRequest req) {
        if (userId == null) throw new BizException(401, "未登录");
        if (req == null) throw new BizException(400, "body 不能为空");

        DeviceProfileType type = parseType(req.getType());
        validateByType(type, req.getSensorWidthMm(), req.getSensorHeightMm(), req.getFocalLengthMm(),
            req.getTelescopeFocalMm(), req.getEyepieceFocalMm(), req.getEyepieceAfovDeg());

        LocalDateTime now = LocalDateTime.now();

        DeviceProfile e = new DeviceProfile();
        e.setUserId(userId);
        e.setName(req.getName().trim());
        e.setType(type.name());
        e.setSensorWidthMm(req.getSensorWidthMm());
        e.setSensorHeightMm(req.getSensorHeightMm());
        e.setFocalLengthMm(req.getFocalLengthMm());
        e.setTelescopeFocalMm(req.getTelescopeFocalMm());
        e.setEyepieceFocalMm(req.getEyepieceFocalMm());
        e.setEyepieceAfovDeg(req.getEyepieceAfovDeg());
        e.setIsDefault(0);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        this.save(e);

        // 设置默认（同一 user + type 只能有一个 default）
        if (Boolean.TRUE.equals(req.getSetAsDefault())) {
            setDefaultInternal(userId, type.name(), e.getId());
            e.setIsDefault(1);
        }

        return toVO(e);
    }

    @Override
    @Transactional
    public DeviceProfileVO updateMy(Long userId, Long id, DeviceProfileUpdateRequest req) {
        if (userId == null) throw new BizException(401, "未登录");
        if (id == null) throw new BizException(400, "id 不能为空");
        if (req == null) throw new BizException(400, "body 不能为空");

        DeviceProfile e = this.getById(id);
        if (e == null) throw new BizException(404, "设备配置不存在");
        if (!Objects.equals(e.getUserId(), userId)) throw new BizException(403, "无权限");

        DeviceProfileType type = parseType(e.getType());

        // 先把变更后的值算出来，然后做一次 type 约束校验
        Double newSensorW = req.getSensorWidthMm() != null ? req.getSensorWidthMm() : e.getSensorWidthMm();
        Double newSensorH = req.getSensorHeightMm() != null ? req.getSensorHeightMm() : e.getSensorHeightMm();
        Double newFocal = req.getFocalLengthMm() != null ? req.getFocalLengthMm() : e.getFocalLengthMm();
        Double newTeleFocal = req.getTelescopeFocalMm() != null ? req.getTelescopeFocalMm() : e.getTelescopeFocalMm();
        Double newEyeFocal = req.getEyepieceFocalMm() != null ? req.getEyepieceFocalMm() : e.getEyepieceFocalMm();
        Double newEyeAfov = req.getEyepieceAfovDeg() != null ? req.getEyepieceAfovDeg() : e.getEyepieceAfovDeg();

        validateByType(type, newSensorW, newSensorH, newFocal, newTeleFocal, newEyeFocal, newEyeAfov);

        if (req.getName() != null) {
            String n = req.getName().trim();
            if (n.isEmpty()) throw new BizException(400, "name 不能为空");
            e.setName(n);
        }
        if (req.getSensorWidthMm() != null) e.setSensorWidthMm(req.getSensorWidthMm());
        if (req.getSensorHeightMm() != null) e.setSensorHeightMm(req.getSensorHeightMm());
        if (req.getFocalLengthMm() != null) e.setFocalLengthMm(req.getFocalLengthMm());
        if (req.getTelescopeFocalMm() != null) e.setTelescopeFocalMm(req.getTelescopeFocalMm());
        if (req.getEyepieceFocalMm() != null) e.setEyepieceFocalMm(req.getEyepieceFocalMm());
        if (req.getEyepieceAfovDeg() != null) e.setEyepieceAfovDeg(req.getEyepieceAfovDeg());
        e.setUpdatedAt(LocalDateTime.now());

        this.updateById(e);

        if (Boolean.TRUE.equals(req.getSetAsDefault())) {
            setDefaultInternal(userId, type.name(), e.getId());
            e.setIsDefault(1);
        }

        return toVO(e);
    }

    @Override
    @Transactional
    public void deleteMy(Long userId, Long id) {
        if (userId == null) throw new BizException(401, "未登录");
        if (id == null) throw new BizException(400, "id 不能为空");

        DeviceProfile e = this.getById(id);
        if (e == null) return; // 幂等
        if (!Objects.equals(e.getUserId(), userId)) throw new BizException(403, "无权限");
        this.removeById(id);
    }

    @Override
    public DeviceProfileVO getMy(Long userId, Long id) {
        if (userId == null) throw new BizException(401, "未登录");
        if (id == null) throw new BizException(400, "id 不能为空");

        DeviceProfile e = this.getById(id);
        if (e == null) throw new BizException(404, "设备配置不存在");
        if (!Objects.equals(e.getUserId(), userId)) throw new BizException(403, "无权限");
        return toVO(e);
    }

    @Override
    public List<DeviceProfileVO> listMy(Long userId, String type) {
        if (userId == null) throw new BizException(401, "未登录");

        LambdaQueryWrapper<DeviceProfile> qw = new LambdaQueryWrapper<DeviceProfile>()
            .eq(DeviceProfile::getUserId, userId)
            .orderByDesc(DeviceProfile::getIsDefault)
            .orderByDesc(DeviceProfile::getUpdatedAt)
            .orderByDesc(DeviceProfile::getId);

        if (type != null && !type.trim().isEmpty()) {
            DeviceProfileType t = parseType(type);
            qw.eq(DeviceProfile::getType, t.name());
        }

        List<DeviceProfile> list = this.list(qw);
        if (list == null || list.isEmpty()) return Collections.emptyList();
        return list.stream().map(DeviceProfileServiceImpl::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeviceProfileVO setDefaultMy(Long userId, Long id) {
        if (userId == null) throw new BizException(401, "未登录");
        if (id == null) throw new BizException(400, "id 不能为空");

        DeviceProfile e = this.getById(id);
        if (e == null) throw new BizException(404, "设备配置不存在");
        if (!Objects.equals(e.getUserId(), userId)) throw new BizException(403, "无权限");

        DeviceProfileType type = parseType(e.getType());
        setDefaultInternal(userId, type.name(), id);

        // 刷新一次
        DeviceProfile refreshed = this.getById(id);
        return toVO(refreshed == null ? e : refreshed);
    }

    @Override
    public DeviceProfileVO getDefaultMy(Long userId, String type) {
        if (userId == null) throw new BizException(401, "未登录");
        DeviceProfileType t = parseType(type);

        DeviceProfile e = this.getOne(new LambdaQueryWrapper<DeviceProfile>()
            .eq(DeviceProfile::getUserId, userId)
            .eq(DeviceProfile::getType, t.name())
            .eq(DeviceProfile::getIsDefault, 1)
            .orderByDesc(DeviceProfile::getUpdatedAt)
            .last("LIMIT 1"));

        if (e == null) return null;
        return toVO(e);
    }

    private static DeviceProfileVO toVO(DeviceProfile e) {
        if (e == null) return null;
        DeviceProfileVO vo = new DeviceProfileVO();
        vo.setId(e.getId());
        vo.setUserId(e.getUserId());
        vo.setName(e.getName());
        vo.setType(e.getType());
        vo.setSensorWidthMm(e.getSensorWidthMm());
        vo.setSensorHeightMm(e.getSensorHeightMm());
        vo.setFocalLengthMm(e.getFocalLengthMm());
        vo.setTelescopeFocalMm(e.getTelescopeFocalMm());
        vo.setEyepieceFocalMm(e.getEyepieceFocalMm());
        vo.setEyepieceAfovDeg(e.getEyepieceAfovDeg());
        vo.setIsDefault(e.getIsDefault());
        vo.setCreatedAt(e.getCreatedAt());
        vo.setUpdatedAt(e.getUpdatedAt());
        return vo;
    }

    private DeviceProfileType parseType(String v) {
        DeviceProfileType type = DeviceProfileType.fromString(v);
        if (type == null) throw new BizException(400, "type 必须为 PHOTO 或 VISUAL");
        return type;
    }

    private void validateByType(DeviceProfileType type,
                                Double sensorW, Double sensorH, Double focal,
                                Double telescopeFocal, Double eyepieceFocal, Double eyepieceAfov) {
        if (type == null) throw new BizException(400, "type 必须为 PHOTO 或 VISUAL");

        if (type == DeviceProfileType.PHOTO) {
            if (sensorW == null || sensorW <= 0) throw new BizException(400, "PHOTO 必须提供 sensorWidthMm（>0）");
            if (sensorH == null || sensorH <= 0) throw new BizException(400, "PHOTO 必须提供 sensorHeightMm（>0）");
            if (focal == null || focal <= 0) throw new BizException(400, "PHOTO 必须提供 focalLengthMm（>0）");
        } else {
            if (telescopeFocal == null || telescopeFocal <= 0) throw new BizException(400, "VISUAL 必须提供 telescopeFocalMm（>0）");
            if (eyepieceFocal == null || eyepieceFocal <= 0) throw new BizException(400, "VISUAL 必须提供 eyepieceFocalMm（>0）");
            if (eyepieceAfov == null || eyepieceAfov <= 0) throw new BizException(400, "VISUAL 必须提供 eyepieceAfovDeg（>0）");
            if (eyepieceAfov > 180) throw new BizException(400, "VISUAL eyepieceAfovDeg 最大 180");
        }
    }

    private void setDefaultInternal(Long userId, String type, Long id) {
        // 1) 先把同 user+type 的其他 default 清零
        this.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<DeviceProfile>()
            .eq(DeviceProfile::getUserId, userId)
            .eq(DeviceProfile::getType, type)
            .eq(DeviceProfile::getIsDefault, 1)
            .ne(DeviceProfile::getId, id)
            .set(DeviceProfile::getIsDefault, 0)
            .set(DeviceProfile::getUpdatedAt, LocalDateTime.now()));

        // 2) 把当前设置为 default
        DeviceProfile current = this.getById(id);
        if (current == null) throw new BizException(404, "设备配置不存在");
        if (!Objects.equals(current.getUserId(), userId)) throw new BizException(403, "无权限");
        if (!Objects.equals(current.getType(), type)) throw new BizException(400, "type 不匹配");

        if (!Objects.equals(current.getIsDefault(), 1)) {
            current.setIsDefault(1);
            current.setUpdatedAt(LocalDateTime.now());
            this.updateById(current);
        }
    }
}
