package com.starmuseum.modules.post.vo;

import lombok.Data;

/**
 * 帖子图片条目
 */
@Data
public class PostMediaItem {

    private Long mediaId;
    private Integer sortNo;

    private String originUrl;
    private String thumbUrl;
    private String mediumUrl;

    private String mimeType;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
}
