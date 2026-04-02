package com.ensurebit.ark.kvm.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ensurebit.ark.kvm.domain.DeviceDO;
import com.ensurebit.ark.kvm.handle.DeviceSessionManager;
import com.ensurebit.ark.kvm.mapper.DeviceMapper;
import com.ensurebit.ark.kvm.service.DeviceService;
import com.ensurebit.ark.kvm.vo.BaseResult;
import com.ensurebit.ark.kvm.vo.DeviceVO;
import com.ensurebit.ark.kvm.vo.ExchangeDeviceTokenVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private DeviceSessionManager deviceSessionManager;

    @Override
    public boolean bindDevice(String deviceId, Long userId) {
        DeviceDO deviceDO = deviceMapper.selectById(deviceId);
        if (deviceDO != null) {
            return deviceDO.getUserId().equals(userId);
        }
        deviceDO = new DeviceDO();
        deviceDO.setId(deviceId);
        deviceDO.setUserId(userId);
        deviceMapper.insert(deviceDO);
        return true;
    }

    @Override
    public boolean hasDevice(String deviceId, Long userId) {
        return deviceMapper.selectCount(new LambdaQueryWrapper<DeviceDO>()
                .eq(DeviceDO::getId, deviceId)
                .eq(DeviceDO::getUserId, userId)) > 0;
    }

    @Override
    public BaseResult<List<DeviceVO>> getDevices(Long userId) {
        List<DeviceDO> deviceList = deviceMapper.selectList(
                new LambdaQueryWrapper<DeviceDO>().eq(DeviceDO::getUserId, userId)
        );
        if (CollectionUtils.isEmpty(deviceList)) {
            return BaseResult.success(Collections.emptyList());
        }

        List<DeviceVO> voList = deviceList.stream().map(deviceDO -> {
            DeviceVO deviceVO = new DeviceVO();
            deviceVO.setId(deviceDO.getId());
            deviceVO.setName(deviceDO.getName());
            deviceVO.setLastSeen(deviceDO.getLastSeen());
            deviceVO.setOnline(deviceSessionManager.hasDevice(deviceDO.getId()));
            deviceVO.setCloudVersion(deviceDO.getCloudVersion());
            return deviceVO;
        }).collect(Collectors.toList());

        return BaseResult.success(voList);
    }

    @Override
    public BaseResult<DeviceDO> getDevice(String deviceId, Long userId) {
        DeviceDO deviceDO = deviceMapper.selectOne(new LambdaQueryWrapper<DeviceDO>()
                .eq(DeviceDO::getId, deviceId)
                .eq(DeviceDO::getUserId, userId));
        if (deviceDO == null) {
            return BaseResult.fail("Device not found");
        }
        return BaseResult.success(deviceDO);
    }

    @Override
    public BaseResult<?> unbindDevice(String id, String secretToken) {
        DeviceDO deviceDO = deviceMapper.selectOne(new LambdaQueryWrapper<DeviceDO>()
                .eq(DeviceDO::getId, id)
                .eq(DeviceDO::getSecretToken, secretToken));
        if (deviceDO == null) {
            return BaseResult.fail("Device not found");
        }
        deviceMapper.deleteById(deviceDO);
        return BaseResult.success();
    }

    @Override
    public BaseResult<?> deleteDevice(String deviceId, Long userId) throws IOException {
        DeviceDO deviceDO = deviceMapper.selectOne(new LambdaQueryWrapper<DeviceDO>()
                .eq(DeviceDO::getId, deviceId)
                .eq(DeviceDO::getUserId, userId));
        if (deviceDO == null) {
            return BaseResult.fail("Device not found");
        }
        deviceMapper.deleteById(deviceDO);
        WebSocketSession session = deviceSessionManager.getDeviceSession(deviceId);
        if (session != null) {
            session.close();
            deviceSessionManager.removeDeviceSession(deviceId);
        }
        return BaseResult.success();
    }

    @Override
    public BaseResult<DeviceDO> updateDevice(String deviceId, Long userId, String name) {
        DeviceDO deviceDO = deviceMapper.selectOne(new LambdaQueryWrapper<DeviceDO>()
                .eq(DeviceDO::getId, deviceId)
                .eq(DeviceDO::getUserId, userId));
        if (deviceDO == null) {
            return BaseResult.fail("Device not found");
        }
        deviceDO.setName(name);
        deviceMapper.updateById(deviceDO);
        return BaseResult.success(deviceDO);
    }

    @Override
    public void updateLastSeen(String deviceId) {
        deviceMapper.update(new LambdaUpdateWrapper<DeviceDO>()
                .eq(DeviceDO::getId, deviceId)
                .set(DeviceDO::getLastSeen, new Date()));
    }

    @Override
    public ExchangeDeviceTokenVO exchangeToken(String tempToken) {
        String deviceId = StpUtil.getLoginDeviceTypeByToken(tempToken);
        DeviceDO deviceDO = deviceMapper.selectById(deviceId);
        if (deviceDO == null) {
            throw new RuntimeException("Device not found");
        }
        String secretToken = UUID.fastUUID().toString();
        log.info("device secret token: {},{}", deviceId, secretToken);
        deviceDO.setSecretToken(secretToken);
        deviceMapper.updateById(deviceDO);
        ExchangeDeviceTokenVO vo = new ExchangeDeviceTokenVO();
        vo.setSecretToken(secretToken);
        return vo;
    }
}
