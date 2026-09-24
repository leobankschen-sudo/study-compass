package com.zhixu.studycompass.service;

import com.zhixu.studycompass.dto.UserRequest;
import com.zhixu.studycompass.model.User;

import java.util.List;

public interface UserService {
    List<User> findAll();
    User findById(Long id);
    User create(UserRequest request);
    User update(Long id, UserRequest request);
    void delete(Long id);
}

