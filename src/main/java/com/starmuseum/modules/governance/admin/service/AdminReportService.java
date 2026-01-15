package com.starmuseum.modules.governance.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.modules.governance.admin.dto.AdminReportReviewRequest;
import com.starmuseum.modules.governance.admin.vo.AdminReportDetailVO;
import com.starmuseum.modules.governance.admin.vo.AdminReportListItemVO;

public interface AdminReportService {

    IPage<AdminReportListItemVO> page(int page, int size, String status, String targetType, String reasonCode);

    AdminReportDetailVO detail(Long reportId);

    AdminReportDetailVO start(Long reportId, Long adminUserId);

    AdminReportDetailVO review(Long reportId, Long adminUserId, AdminReportReviewRequest req);
}
