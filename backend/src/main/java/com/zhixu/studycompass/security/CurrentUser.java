package com.zhixu.studycompass.security;

import com.zhixu.studycompass.common.BusinessException;
import com.zhixu.studycompass.mapper.UserMapper;
import com.zhixu.studycompass.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    private final UserMapper userMapper;

    public CurrentUser(UserMapper userMapper) { this.userMapper = userMapper; }

    public User require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return userMapper.findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "登录状态已失效"));
    }

    public void requireAccess(Long userId) {
        User user = require();
        if (!"ADMIN".equals(user.getRole()) && !user.getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权访问其他用户的数据");
        }
    }
}
