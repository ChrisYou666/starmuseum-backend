package com.starmuseum.modules.post.vo;

import lombok.Data;

@Data
public class PostMediaVO {

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
