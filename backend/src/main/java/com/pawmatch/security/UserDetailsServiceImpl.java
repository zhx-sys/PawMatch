package com.pawmatch.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pawmatch.entity.Shelter;
import com.pawmatch.entity.User;
import com.pawmatch.mapper.ShelterMapper;
import com.pawmatch.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final ShelterMapper shelterMapper;

    public UserDetailsServiceImpl(UserMapper userMapper, ShelterMapper shelterMapper) {
        this.userMapper = userMapper;
        this.shelterMapper = shelterMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // This method format: "userId:userType"
        String[] parts = username.split(":");
        Long id = Long.parseLong(parts[0]);
        Integer userType = Integer.parseInt(parts[1]);

        if (userType == 1) {
            Shelter shelter = shelterMapper.selectById(id);
            if (shelter == null) {
                throw new UsernameNotFoundException("Shelter not found: " + id);
            }
            return new org.springframework.security.core.userdetails.User(
                    id + ":1",
                    shelter.getPassword(),
                    shelter.getStatus() == 1,
                    true, true, true,
                    Collections.singletonList(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SHELTER"))
            );
        } else {
            User user = userMapper.selectById(id);
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + id);
            }
            return new org.springframework.security.core.userdetails.User(
                    id + ":0",
                    user.getPassword(),
                    user.getStatus() == 1,
                    true, true, true,
                    Collections.singletonList(
                            new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
        }
    }
}
