package com.starmuseum.modules.governance.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.starmuseum.modules.governance.report.dto.ReportCreateRequest;
import com.starmuseum.modules.governance.report.vo.ReportVO;

public interface ReportService {

    ReportVO create(ReportCreateRequest req, Long reporterUserId);

    IPage<ReportVO> myPage(int page, int size, Long reporterUserId, String status);

    ReportVO myDetail(Long reportId, Long reporterUserId);

    void withdraw(Long reportId, Long reporterUserId);
}
