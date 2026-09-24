package com.zhixu.studycompass.controller;

import com.zhixu.studycompass.common.ApiResponse;
import com.zhixu.studycompass.dto.UserRequest;
import com.zhixu.studycompass.model.User;
import com.zhixu.studycompass.service.UserService;
import com.zhixu.studycompass.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ApiResponse<List<User>> findAll() { return ApiResponse.ok(userService.findAll()); }

    @GetMapping("/{id}")
    public ApiResponse<User> findById(@PathVariable Long id) { currentUser.requireAccess(id); return ApiResponse.ok(userService.findById(id)); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<User> create(@Valid @RequestBody UserRequest request) {
        return ApiResponse.ok(userService.create(request), "用户创建成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        currentUser.requireAccess(id);
        return ApiResponse.ok(userService.update(id, request), "用户更新成功");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok(null, "用户删除成功");
    }
}
