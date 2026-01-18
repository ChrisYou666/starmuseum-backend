// src/main/java/com/starmuseum/modules/astro/device/service/impl/FovCalcServiceImpl.java
package com.starmuseum.modules.astro.device.service.impl;

import com.starmuseum.common.exception.BizException;
import com.starmuseum.modules.astro.device.dto.FovRequest;
import com.starmuseum.modules.astro.device.dto.FovResponse;
import com.starmuseum.modules.astro.device.enums.DeviceProfileType;
import com.starmuseum.modules.astro.device.service.DeviceProfileService;
import com.starmuseum.modules.astro.device.service.FovCalcService;
import org.springframework.stereotype.Service;

@Service
public class FovCalcServiceImpl implements FovCalcService {

    private final DeviceProfileService deviceProfileService;

    public FovCalcServiceImpl(DeviceProfileService deviceProfileService) {
        this.deviceProfileService = deviceProfileService;
    }

    @Override
    public FovResponse calc(FovRequest req, Long userId) {
        if (userId == null) throw new BizException(401, "未登录");
        if (req == null) throw new BizException(400, "body 不能为空");

        // 1) 如果传了 profileId，则以 profile 为准
        if (req.getProfileId() != null) {
            Long pid = req.getProfileId();
            // 通过 Service 做权限校验
            com.starmuseum.modules.astro.device.vo.DeviceProfileVO vo = deviceProfileService.getMy(userId, pid);
            DeviceProfileType type = DeviceProfileType.fromString(vo.getType());
            if (type == null) throw new BizException(400, "profile.type 非法");
            if (type == DeviceProfileType.PHOTO) {
                return calcPhoto(type, vo.getSensorWidthMm(), vo.getSensorHeightMm(), vo.getFocalLengthMm(), req.getRotationDeg());
            } else {
                return calcVisual(type, vo.getTelescopeFocalMm(), vo.getEyepieceFocalMm(), vo.getEyepieceAfovDeg(), req.getRotationDeg());
            }
        }

        // 2) 否则使用直传参数
        DeviceProfileType type = DeviceProfileType.fromString(req.getType());
        if (type == null) throw new BizException(400, "type 必须为 PHOTO 或 VISUAL");
        if (type == DeviceProfileType.PHOTO) {
            return calcPhoto(type, req.getSensorWidthMm(), req.getSensorHeightMm(), req.getFocalLengthMm(), req.getRotationDeg());
        } else {
            return calcVisual(type, req.getTelescopeFocalMm(), req.getEyepieceFocalMm(), req.getEyepieceAfovDeg(), req.getRotationDeg());
        }
    }

    private FovResponse calcPhoto(DeviceProfileType type,
                                  Double sensorW, Double sensorH, Double focal,
                                  Double rotationDeg) {
        if (sensorW == null || sensorW <= 0) throw new BizException(400, "sensorWidthMm 必须 > 0");
        if (sensorH == null || sensorH <= 0) throw new BizException(400, "sensorHeightMm 必须 > 0");
        if (focal == null || focal <= 0) throw new BizException(400, "focalLengthMm 必须 > 0");

        double h = fovDeg(sensorW, focal);
        double v = fovDeg(sensorH, focal);
        double diagMm = Math.sqrt(sensorW * sensorW + sensorH * sensorH);
        double d = fovDeg(diagMm, focal);

        FovResponse resp = new FovResponse();
        resp.setType(type.name());
        resp.setHorizontalDeg(h);
        resp.setVerticalDeg(v);
        resp.setDiagonalDeg(d);
        // MVP：PHOTO 的 TFOV 先定义为对角线视场角
        resp.setTfovDeg(d);
        resp.setMagnification(null);
        resp.setRotationDeg(rotationDeg != null ? rotationDeg : 0d);
        resp.setFrameWidthDeg(h);
        resp.setFrameHeightDeg(v);
        return resp;
    }

    private FovResponse calcVisual(DeviceProfileType type,
                                   Double telescopeFocal, Double eyepieceFocal, Double eyepieceAfov,
                                   Double rotationDeg) {
        if (telescopeFocal == null || telescopeFocal <= 0) throw new BizException(400, "telescopeFocalMm 必须 > 0");
        if (eyepieceFocal == null || eyepieceFocal <= 0) throw new BizException(400, "eyepieceFocalMm 必须 > 0");
        if (eyepieceAfov == null || eyepieceAfov <= 0) throw new BizException(400, "eyepieceAfovDeg 必须 > 0");
        if (eyepieceAfov > 180) throw new BizException(400, "eyepieceAfovDeg 最大 180");

        double magnification = telescopeFocal / eyepieceFocal;
        if (magnification <= 0) throw new BizException(400, "magnification 计算异常");

        double tfov = eyepieceAfov / magnification;

        FovResponse resp = new FovResponse();
        resp.setType(type.name());
        resp.setHorizontalDeg(tfov);
        resp.setVerticalDeg(tfov);
        resp.setDiagonalDeg(tfov);
        resp.setTfovDeg(tfov);
        resp.setMagnification(magnification);
        resp.setRotationDeg(rotationDeg != null ? rotationDeg : 0d);
        resp.setFrameWidthDeg(tfov);
        resp.setFrameHeightDeg(tfov);
        return resp;
    }

    /**
     * 视场角公式：FOV = 2 * arctan(size / (2 * focal))
     * size/focal 需使用同一单位（这里统一 mm）。
     */
    private static double fovDeg(double sizeMm, double focalMm) {
        double rad = 2.0 * Math.atan(sizeMm / (2.0 * focalMm));
        return Math.toDegrees(rad);
    }
}
