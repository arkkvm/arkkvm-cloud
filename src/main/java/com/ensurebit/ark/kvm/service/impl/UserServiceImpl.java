package com.ensurebit.ark.kvm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ensurebit.ark.kvm.domain.UserDO;
import com.ensurebit.ark.kvm.mapper.UserMapper;
import com.ensurebit.ark.kvm.service.UserService;
import com.ensurebit.ark.kvm.vo.BaseResult;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Override
    public Long upsertUserByGoogleAuth(String idToken) throws ParseException {
        SignedJWT signedJWT = SignedJWT.parse(idToken);
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
        String googleId = claims.getSubject();
        String email = claims.getStringClaim("email");
        String picture = claims.getStringClaim("picture");
        log.debug("email: {}, sub: {}, picture: {}", email, googleId, picture);


        UserDO userDO = userMapper.selectOne(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getGoogleId, googleId)
                .eq(UserDO::getEmail, email));


        if (userDO != null) {
            userDO.setPicture(picture);
            userDO.setEmail(email);
            userDO.setOidcToken(idToken);
            userMapper.updateById(userDO);
        } else {
            userDO = new UserDO();
            userDO.setGoogleId(googleId);
            userDO.setEmail(email);
            userDO.setPicture(picture);
            userDO.setOidcToken(idToken);
            userMapper.insert(userDO);
        }

        return userDO.getId();
    }

    @Override
    public BaseResult<UserDO> getUserById(Long userId) {
        return BaseResult.success(userMapper.selectById(userId));
    }
}
