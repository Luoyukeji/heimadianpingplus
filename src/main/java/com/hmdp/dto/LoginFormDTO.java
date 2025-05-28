package com.hmdp.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginFormDTO {
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;
    /**
     * 验证码
     */
    private String code;
    /**
     * 密码
     */
    private String password;
}
