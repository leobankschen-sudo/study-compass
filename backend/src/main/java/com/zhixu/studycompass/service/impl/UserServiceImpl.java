package com.zhixu.studycompass.service.impl;

import com.zhixu.studycompass.common.BusinessException;
import com.zhixu.studycompass.dto.UserRequest;
import com.zhixu.studycompass.mapper.UserMapper;
import com.zhixu.studycompass.mapper.GoalMapper;
import com.zhixu.studycompass.mapper.TaskMapper;
import com.zhixu.studycompass.mapper.StudyRecordMapper;
import com.zhixu.studycompass.model.User;
import com.zhixu.studycompass.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final GoalMapper goalMapper;
    private final TaskMapper taskMapper;
    private final StudyRecordMapper recordMapper;

    public UserServiceImpl(UserMapper userMapper, GoalMapper goalMapper, TaskMapper taskMapper,
                           StudyRecordMapper recordMapper) {
        this.userMapper = userMapper;
        this.goalMapper = goalMapper;
        this.taskMapper = taskMapper;
        this.recordMapper = recordMapper;
    }

    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public User findById(Long id) {
        return userMapper.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
    }

    @Override
    @Transactional
    public User create(UserRequest request) {
        ensureUnique(request, null);
        User user = toUser(new User(), request);
        userMapper.insert(user);
        return findById(user.getId());
    }

    @Override
    @Transactional
    public User update(Long id, UserRequest request) {
        User user = findById(id);
        ensureUnique(request, id);
        userMapper.update(toUser(user, request));
        return findById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        if (goalMapper.countByUserId(id) > 0 || taskMapper.countByUserId(id) > 0 || recordMapper.countByUserId(id) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "该用户仍有学习数据，不能直接删除");
        }
        userMapper.deleteById(id);
    }

    private void ensureUnique(UserRequest request, Long excludeId) {
        if (userMapper.existsByUsernameOrEmail(request.username(), request.email(), excludeId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "用户名或邮箱已被使用");
        }
    }

    private User toUser(User user, UserRequest request) {
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setRole(request.role() == null ? "USER" : request.role());
        user.setEnabled(request.enabled() == null || request.enabled());
        return user;
    }
}
