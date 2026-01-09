package com.starmuseum.modules.media.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starmuseum.modules.media.dto.MediaDTO;
import com.starmuseum.modules.media.dto.MediaUploadResponse;
import com.starmuseum.modules.media.entity.Media;
import com.starmuseum.modules.media.enums.MediaBizType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService extends IService<Media> {

    // A：上传
    MediaUploadResponse uploadOne(MultipartFile file, MediaBizType bizType);

    List<MediaUploadResponse> uploadBatch(List<MultipartFile> files, MediaBizType bizType);

    // B：查询/分页/删除
    MediaDTO getDtoById(Long id);

    IPage<MediaDTO> pageMy(long page, long size, MediaBizType bizType);

    void deleteMy(Long id);
}
