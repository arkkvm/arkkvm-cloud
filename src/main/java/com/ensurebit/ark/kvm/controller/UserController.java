package com.ensurebit.ark.kvm.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.ensurebit.ark.kvm.domain.UserDO;
import com.ensurebit.ark.kvm.service.UserService;
import com.ensurebit.ark.kvm.vo.BaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Tag(name = "User")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    public BaseResult<UserDO> me() {
        return userService.getUserById(StpUtil.getLoginIdAsLong());
    }

    @GetMapping("/logout")
    @Operation(summary = "Log out")
    public BaseResult<?> logout() {
        StpUtil.logout();
        return BaseResult.success();
    }
}
