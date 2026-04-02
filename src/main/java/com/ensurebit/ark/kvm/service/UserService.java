package com.ensurebit.ark.kvm.service;

import com.ensurebit.ark.kvm.domain.UserDO;
import com.ensurebit.ark.kvm.vo.BaseResult;

import java.text.ParseException;

public interface UserService {

    Long upsertUserByGoogleAuth(String idToken) throws ParseException;

    BaseResult<UserDO> getUserById(Long userId);

}
