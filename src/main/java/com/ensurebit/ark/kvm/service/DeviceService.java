package com.ensurebit.ark.kvm.service;

import com.ensurebit.ark.kvm.domain.DeviceDO;
import com.ensurebit.ark.kvm.vo.BaseResult;
import com.ensurebit.ark.kvm.vo.DeviceVO;
import com.ensurebit.ark.kvm.vo.ExchangeDeviceTokenVO;

import java.io.IOException;
import java.util.List;

public interface DeviceService {

    boolean bindDevice(String deviceId, Long userId);

    boolean hasDevice(String deviceId, Long userId);

    BaseResult<List<DeviceVO>> getDevices(Long userId);

    BaseResult<DeviceDO> getDevice(String deviceId, Long userId);

    BaseResult<?> unbindDevice(String deviceId, String secretToken);

    BaseResult<?> deleteDevice(String deviceId, Long userId) throws IOException;

    BaseResult<DeviceDO> updateDevice(String deviceId, Long userId, String name);

    void updateLastSeen(String deviceId);

    ExchangeDeviceTokenVO exchangeToken(String tempToken);
}
