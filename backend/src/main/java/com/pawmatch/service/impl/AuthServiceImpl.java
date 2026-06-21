package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pawmatch.entity.Shelter;
import com.pawmatch.entity.User;
import com.pawmatch.mapper.ShelterMapper;
import com.pawmatch.mapper.UserMapper;
import com.pawmatch.security.JwtTokenUtil;
import com.pawmatch.service.AuthService;
import com.pawmatch.dto.request.RegisterUserRequest;
import com.pawmatch.dto.request.LoginRequest;
import com.pawmatch.dto.request.ForgotPasswordRequest;
import com.pawmatch.dto.response.LoginResponse;
import com.pawmatch.dto.response.RegisterResponse;
import com.pawmatch.exception.BusinessException;
import com.pawmatch.exception.ErrorCode;
import com.pawmatch.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthServiceImpl extends ServiceImpl<ShelterMapper, Shelter> implements AuthService {

    private final UserMapper userMapper;
    private final JwtTokenUtil jwtTokenUtil;

    public AuthServiceImpl(UserMapper userMapper, JwtTokenUtil jwtTokenUtil) {
        this.userMapper = userMapper;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    @Transactional
    public RegisterResponse registerUser(RegisterUserRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "两次密码不一致");
        }
        if (!PasswordUtil.isStrongEnough(request.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_WEAK);
        }
        String account = request.getAccount() != null && !request.getAccount().isBlank()
                ? request.getAccount() : generateUniqueUserAccount();
        User user = new User();
        user.setAccount(account);
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setCreditScore(100);
        user.setStatus(1);
        user.setInfoComplete(false);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        RegisterResponse resp = new RegisterResponse();
        resp.setId(user.getId());
        resp.setAccount(account);
        return resp;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        if (request.getUserType() == 1) {
            return loginAsShelter(request);
        } else {
            return loginAsUser(request);
        }
    }

    private LoginResponse loginAsShelter(LoginRequest request) {
        LambdaQueryWrapper<Shelter> wrapper = Wrappers.lambdaQuery(Shelter.class)
                .eq(Shelter::getAccount, request.getAccount());
        Shelter shelter = getOne(wrapper);
        if (shelter == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (shelter.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!PasswordUtil.matches(request.getPassword(), shelter.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        String token = jwtTokenUtil.generateToken(shelter.getId(), 1);
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenUtil.getExpiration() / 1000);
        response.setUserId(shelter.getId());
        response.setUserType(1);
        return response;
    }

    private LoginResponse loginAsUser(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getAccount, request.getAccount());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        String token = jwtTokenUtil.generateToken(user.getId(), 0);
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenUtil.getExpiration() / 1000);
        response.setUserId(user.getId());
        response.setUserType(0);
        return response;
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // Simplified: find by account and reset password
        LambdaQueryWrapper<User> userWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getAccount, request.getAccount())
                .eq(User::getPhone, request.getPhone());
        User user = userMapper.selectOne(userWrapper);
        if (user == null) {
            LambdaQueryWrapper<Shelter> shelterWrapper = Wrappers.lambdaQuery(Shelter.class)
                    .eq(Shelter::getAccount, request.getAccount())
                    .eq(Shelter::getPhone, request.getPhone());
            Shelter shelter = getOne(shelterWrapper);
            if (shelter == null) {
                throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
            }
            if (!PasswordUtil.isStrongEnough(request.getNewPassword())) {
                throw new BusinessException(ErrorCode.PASSWORD_TOO_WEAK);
            }
            shelter.setPassword(PasswordUtil.encode(request.getNewPassword()));
            updateById(shelter);
        } else {
            if (!PasswordUtil.isStrongEnough(request.getNewPassword())) {
                throw new BusinessException(ErrorCode.PASSWORD_TOO_WEAK);
            }
            user.setPassword(PasswordUtil.encode(request.getNewPassword()));
            userMapper.updateById(user);
        }
    }

    private final Random random = new Random();

    private String generateUniqueUserAccount() {
        String account;
        do {
            account = String.format("%06d", random.nextInt(1000000));
        } while (userMapper.selectCount(Wrappers.lambdaQuery(User.class).eq(User::getAccount, account)) > 0);
        return account;
    }
}
