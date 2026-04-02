package com.ensurebit.ark.kvm.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.ensurebit.ark.kvm.domain.DeviceDO;
import com.ensurebit.ark.kvm.dto.ExchangeDeviceTokenDTO;
import com.ensurebit.ark.kvm.dto.UpdateDeviceDTO;
import com.ensurebit.ark.kvm.service.DeviceService;
import com.ensurebit.ark.kvm.vo.BaseResult;
import com.ensurebit.ark.kvm.vo.DeviceVO;
import com.ensurebit.ark.kvm.vo.ExchangeDeviceTokenVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@Tag(name = "Device management")
public class DeviceController {

    @Resource
    private DeviceService deviceService;

    @GetMapping("/devices/{id}")
    @Operation(summary = "Get device info")
    public BaseResult<DeviceDO> getDevice(@PathVariable String id) {
        long userId = StpUtil.getLoginIdAsLong();
        return deviceService.getDevice(id, userId);
    }

    @GetMapping("/devices")
    @Operation(summary = "Get device list")
    public BaseResult<List<DeviceVO>> getDevices() {
        long id = StpUtil.getLoginIdAsLong();
        return deviceService.getDevices(id);
    }

    @DeleteMapping("/devices/{id}")
    @SaIgnore
    @Operation(summary = "Delete device", description = "Delete device")
    public BaseResult<?> deleteDevice(HttpServletRequest request, @PathVariable String id) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return BaseResult.fail("Authorization header is missing or invalid");
        }
        String token = authorization.substring(7);
        return deviceService.unbindDevice(id, token);
    }

    @DeleteMapping("/cloud/devices/{id}")
    @Operation(summary = "Unbind device from cloud")
    public BaseResult<?> unbindDevice(@PathVariable String id) throws IOException {
        long userId = StpUtil.getLoginIdAsLong();
        return deviceService.deleteDevice(id, userId);
    }

    @PutMapping("/devices/{id}")
    @Operation(summary = "Update device info")
    public BaseResult<DeviceDO> updateDevice(@PathVariable String id, @RequestBody @Valid UpdateDeviceDTO deviceDTO) {
        long userId = StpUtil.getLoginIdAsLong();
        return deviceService.updateDevice(id, userId, deviceDTO.getName());
    }

    @PostMapping("/devices/token")
    @SaIgnore
    @Operation(summary = "Get device token")
    public ExchangeDeviceTokenVO getDeviceToken(@RequestBody ExchangeDeviceTokenDTO exchangeDeviceTokenDTO) {
        return deviceService.exchangeToken(exchangeDeviceTokenDTO.getTempToken());
    }
}
