package com.starmuseum.iam.vo;

import lombok.Data;

/**
 * /me 返回当前用户信息
 */
@Data
public class MeResponse {

    private Long id;
    private String email;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String status;

    private String postVisibilityDefault;

}
