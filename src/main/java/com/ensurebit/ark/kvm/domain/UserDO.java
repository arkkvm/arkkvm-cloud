package com.ensurebit.ark.kvm.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "User information")
@TableName("user")
public class UserDO implements Serializable {

    @Schema(description = "User ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "Google Id")
    private String googleId;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "User avatar")
    private String picture;

    @Schema(description = "Google OIDC Token")
    private String oidcToken;
}
