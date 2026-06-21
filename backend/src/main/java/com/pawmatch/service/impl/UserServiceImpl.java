package com.pawmatch.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pawmatch.entity.User;
import com.pawmatch.entity.Pet;
import com.pawmatch.entity.Notification;
import com.pawmatch.entity.Shelter;
import com.pawmatch.mapper.UserMapper;
import com.pawmatch.mapper.ShelterMapper;
import com.pawmatch.mapper.PetMapper;
import com.pawmatch.mapper.NotificationMapper;
import com.pawmatch.service.UserService;
import com.pawmatch.dto.request.UpdateUserRequest;
import com.pawmatch.dto.response.UserResponse;
import com.pawmatch.dto.response.PetResponse;
import com.pawmatch.dto.response.NotificationResponse;
import com.pawmatch.dto.request.MatchingProfileRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PetMapper petMapper;
    private final NotificationMapper notificationMapper;
    private final ShelterMapper shelterMapper;
    private final CreditService creditService;
    private final GrowthServiceImpl growthService;

    public UserServiceImpl(PetMapper petMapper, NotificationMapper notificationMapper,
                           ShelterMapper shelterMapper, CreditService creditService,
                           GrowthServiceImpl growthService) {
        this.petMapper = petMapper;
        this.notificationMapper = notificationMapper;
        this.shelterMapper = shelterMapper;
        this.creditService = creditService;
        this.growthService = growthService;
    }

    @Override
    public void updateUserInfo(UpdateUserRequest request, Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new com.pawmatch.exception.BusinessException(404, "用户不存在");
        }

        // 密码修改
        if (request.getOldPassword() != null && request.getNewPassword() != null) {
            if (request.getNewPassword().length() < 6) {
                throw new com.pawmatch.exception.BusinessException(400, "新密码至少6位");
            }
            String encodedNew = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(request.getNewPassword());
            user.setPassword(encodedNew);
        }

        // 判断之前是否已完成实名认证
        boolean wasVerified = user.getRealName() != null && user.getIdCard() != null;

        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getRealName() != null) user.setRealName(request.getRealName());
        if (request.getIdCard() != null) user.setIdCard(request.getIdCard());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getProvince() != null) user.setProvince(request.getProvince());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getAddressDetail() != null) user.setAddressDetail(request.getAddressDetail());

        // 生日
        if (request.getBirthday() != null) {
            try {
                user.setBirthday(java.time.LocalDate.parse(request.getBirthday()));
            } catch (Exception e) {
                throw new com.pawmatch.exception.BusinessException(400, "生日格式错误，请使用YYYY-MM-DD格式");
            }
        }

        boolean infoComplete = user.getRealName() != null && user.getIdCard() != null;
        user.setInfoComplete(infoComplete);
        this.updateById(user);

        // 首次完成实名认证加分
        if (!wasVerified && infoComplete) {
            creditService.changeCredit(userId, 0, 5, "REAL_NAME_VERIFY", "完成实名认证", null);
        }

        // 积分：首次完善个人资料+15（一次性）
        if (!wasVerified && infoComplete) {
            growthService.awardPoints(userId, 15, "PROFILE_COMPLETE", "完善个人资料");
        }
    }

    @Override
    public UserResponse getUserInfo(Long userId, Integer userType) {
        UserResponse response = new UserResponse();
        if (userType != null && userType == 1) {
            Shelter shelter = shelterMapper.selectById(userId);
            if (shelter == null) {
                throw new com.pawmatch.exception.BusinessException(404, "救助站不存在");
            }
            response.setId(shelter.getId());
            response.setAccount(shelter.getAccount());
            response.setNickname(shelter.getNickname());
            response.setPhone(shelter.getPhone());
            response.setProvince(shelter.getProvince());
            response.setCity(shelter.getCity());
            response.setAddressDetail(shelter.getAddressDetail());
            response.setUserType(1);
            response.setCreditScore(shelter.getCreditScore());
            response.setInfoComplete(shelter.getInfoComplete());
            response.setCreateTime(shelter.getCreateTime());
        } else {
            User user = this.getById(userId);
            if (user == null) {
                throw new com.pawmatch.exception.BusinessException(404, "用户不存在");
            }
            response.setId(user.getId());
            response.setAccount(user.getAccount());
            response.setNickname(user.getNickname());
            response.setRealName(user.getRealName());
            response.setPhone(user.getPhone());
            response.setProvince(user.getProvince());
            response.setCity(user.getCity());
            response.setAddressDetail(user.getAddressDetail());
            response.setPetPreference(user.getPetPreference());
            response.setLivingSpace(user.getLivingSpace());
            response.setHasChildren(user.getHasChildren());
            response.setHasOtherPets(user.getHasOtherPets());
            response.setPetExperience(user.getPetExperience());
            response.setDailyRoutine(user.getDailyRoutine());
            response.setBudgetRange(user.getBudgetRange());
            response.setMatchingProfileComplete(user.getMatchingProfileComplete());
            if (user.getBirthday() != null) response.setBirthday(user.getBirthday().toString());
            response.setUserType(0);
            response.setCreditScore(user.getCreditScore());
            response.setInfoComplete(user.getInfoComplete());
            response.setCreateTime(user.getCreateTime());
        }
        return response;
    }

    @Override
    public boolean checkInfoComplete(Long userId) {
        User user = this.getById(userId);
        return user != null && Boolean.TRUE.equals(user.getInfoComplete());
    }

    @Override
    public List<PetResponse> getMyPets(Long userId) {
        LambdaQueryWrapper<Pet> wrapper = Wrappers.lambdaQuery(Pet.class)
                .eq(Pet::getStatus, 1);
        return petMapper.selectList(wrapper).stream().map(pet -> {
            PetResponse r = new PetResponse();
            r.setId(pet.getId());
            r.setName(pet.getName());
            r.setType(pet.getType());
            r.setBreed(pet.getBreed());
            r.setGender(pet.getGender());
            r.setAge(pet.getAge());
            r.setColor(pet.getColor());
            r.setHealthStatus(pet.getHealthStatus());
            r.setVaccinated(pet.getVaccinated());
            r.setSterilized(pet.getSterilized());
            r.setImages(pet.getImages());
            r.setStatus(pet.getStatus());
            r.setShelterId(pet.getShelterId());
            r.setCreateTime(pet.getCreateTime());
            return r;
        }).collect(Collectors.toList());
    }

    @Override
    public IPage<NotificationResponse> getNotifications(Long userId, Integer userType, long pageNum, long pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Notification> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = Wrappers.lambdaQuery(Notification.class)
                .eq(Notification::getUserId, userId)
                .eq(Notification::getUserType, userType)
                .orderByDesc(Notification::getCreateTime);
        IPage<Notification> result = notificationMapper.selectPage(page, wrapper);
        com.baomidou.mybatisplus.core.metadata.IPage<NotificationResponse> responsePage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                        result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream().map(n -> {
            NotificationResponse r = new NotificationResponse();
            r.setId(n.getId());
            r.setType(n.getType());
            r.setTitle(n.getTitle());
            r.setContent(n.getContent());
            r.setRelatedId(n.getRelatedId());
            r.setIsRead(n.getIsRead());
            r.setCreateTime(n.getCreateTime());
            return r;
        }).collect(java.util.stream.Collectors.toList()));
        return responsePage;
    }

    @Override
    public void markNotificationRead(Long notificationId, Long userId) {
        Notification n = notificationMapper.selectById(notificationId);
        if (n == null || !n.getUserId().equals(userId)) {
            throw new com.pawmatch.exception.BusinessException(404, "通知不存在");
        }
        n.setIsRead(true);
        notificationMapper.updateById(n);
    }

    @Override
    public void markAllNotificationsRead(Long userId, Integer userType) {
        LambdaQueryWrapper<Notification> wrapper = Wrappers.lambdaQuery(Notification.class)
                .eq(Notification::getUserId, userId)
                .eq(Notification::getUserType, userType)
                .eq(Notification::getIsRead, false);
        Notification entity = new Notification();
        entity.setIsRead(true);
        notificationMapper.update(entity, wrapper);
    }

    @Override
    public void updateMatchingProfile(Long userId, MatchingProfileRequest request) {
        User user = this.getById(userId);
        if (user == null) {
            throw new com.pawmatch.exception.BusinessException(404, "用户不存在");
        }
        boolean wasComplete = Boolean.TRUE.equals(user.getMatchingProfileComplete());
        if (request.getLivingSpace() != null) user.setLivingSpace(request.getLivingSpace());
        if (request.getHasChildren() != null) user.setHasChildren(request.getHasChildren());
        if (request.getHasOtherPets() != null) user.setHasOtherPets(request.getHasOtherPets());
        if (request.getPetExperience() != null) user.setPetExperience(request.getPetExperience());
        if (request.getDailyRoutine() != null) user.setDailyRoutine(request.getDailyRoutine());
        if (request.getBudgetRange() != null) user.setBudgetRange(request.getBudgetRange());
        if (request.getPetPreference() != null) user.setPetPreference(request.getPetPreference());
        user.setMatchingProfileComplete(true);
        this.updateById(user);

        // 积分：首次完善匹配画像+10（一次性）
        if (!wasComplete) {
            growthService.awardPoints(userId, 10, "MATCHING_PROFILE_COMPLETE", "完善匹配画像");
        }
    }
}
